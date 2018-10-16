package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.converters.BloodGlucoseConverter;
import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.data.NightscoutDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class NightscoutCommand extends DiabotCommand {

  private Logger logger = LoggerFactory.getLogger(NightscoutCommand.class);

  public NightscoutCommand(Category category) {
    this.name = "nightscout";
    this.help = "Get the most recent info from any nightscout site";
    this.arguments = "Partial nightscout url (part before .herokuapp.com)";
    this.guildOnly = true;
    this.aliases = new String[]{"ns", "bg"};
    this.category = category;
    this.examples = new String[]{"diabot nightscout casscout", "diabot ns casscout"};
  }

  @Override
  protected void execute(CommandEvent event) {

    String[] args = event.getArgs().split("\\s+");

    if (args[0] == null) {
      event.reply("Please pass a partial heroku url (eg: casscout)");
      return;
    }

    try {
      String urlTemplate = "https://%s.herokuapp.com/api/v1/";
      String endpoint = String.format(urlTemplate, args[0]);

      NightscoutDTO dto = new NightscoutDTO();

      getData(endpoint, dto, event);
      getRanges(endpoint, dto, event);

      EmbedBuilder builder = new EmbedBuilder();

      buildResponse(dto, builder);

      MessageEmbed embed = builder.build();

      event.reply(embed);
    } catch (Exception e) {
      event.reactError();
      logger.error("Error while responding to Nightscout request");
      e.printStackTrace();
    }
  }

  private void buildResponse(NightscoutDTO dto, EmbedBuilder builder) {
    builder.setTitle("Nightscout");

    String mmolString;
    String mgdlString;
    if(dto.getDelta() != null) {
      mmolString = buildGlucoseString(dto.getGlucose().getMmol(), dto.getDelta().getMmol(), dto.getDeltaIsNegative());
      mgdlString = buildGlucoseString(dto.getGlucose().getMgdl(), dto.getDelta().getMgdl(), dto.getDeltaIsNegative());
    } else {
      mmolString = buildGlucoseString(dto.getGlucose().getMmol(), 999L, false);
      mgdlString = buildGlucoseString(dto.getGlucose().getMgdl(), 999L, false);
    }

    builder.addField("mmol/L", mmolString, true);
    builder.addField("mg/dL", mgdlString, true);

    setResponseColor(dto, builder);

    builder.setTimestamp(dto.getDateTime());
    builder.setFooter("measured", "https://github.com/nightscout/cgm-remote-monitor/raw/master/static/images/large.png");

    if(dto.getDateTime().plusMinutes(15).toInstant().isBefore(ZonedDateTime.now().toInstant())) {
      builder.setDescription("**BG data is more than 15 minutes old**");
    }


  }

  private String buildGlucoseString(double glucose, @Nullable double delta, boolean negative) {
    StringBuilder builder = new StringBuilder();

    builder.append(glucose);

    if(delta != 999L) {
      // 999L is placeholder for absent delta
      builder.append(" (");

      if (negative) {
        builder.append("-");
      } else {
        builder.append("+");
      }

      builder.append(delta);
      builder.append(")");
    }

    return builder.toString();
  }

  private void setResponseColor(NightscoutDTO dto, EmbedBuilder builder) {
    double glucose = dto.getGlucose().getMgdl();

    if(glucose >= dto.getHigh() || glucose <= dto.getLow()) {
      builder.setColor(Color.red);
    } else if ((glucose >= dto.getTop() && glucose < dto.getHigh()) || (glucose > dto.getLow() && glucose <= dto.getBottom())) {
      builder.setColor(Color.orange);
    } else {
      builder.setColor(Color.green);
    }
  }

  private void getRanges(String url, NightscoutDTO dto, CommandEvent event) throws IOException {
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(url + "/status.json");

    int statusCode = client.executeMethod(method);

    if (statusCode == -1) {
      event.reactError();
    }

    String json = method.getResponseBodyAsString();

    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    JsonObject ranges = jsonObject.get("settings").getAsJsonObject().get("thresholds").getAsJsonObject();

    int low = ranges.get("bgLow").getAsInt();
    int bottom = ranges.get("bgTargetBottom").getAsInt();
    int top = ranges.get("bgTargetTop").getAsInt();
    int high = ranges.get("bgHigh").getAsInt();

    dto.setLow(low);
    dto.setBottom(bottom);
    dto.setTop(top);
    dto.setHigh(high);
  }

  private void getData(String url, NightscoutDTO dto, CommandEvent event) throws IOException, UnknownUnitException {
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(url + "/entries.json");

    method.setQueryString("count=1");

    int statusCode = client.executeMethod(method);

    if (statusCode == -1) {
      event.reactError();
    }

    String json = method.getResponseBodyAsString();

    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonArray().get(0).getAsJsonObject();
    String sgv = jsonObject.get("sgv").getAsString();
    long timestamp = jsonObject.get("date").getAsLong();

    String delta = "";
    if(jsonObject.has("delta")) {
      delta = jsonObject.get("delta").getAsString();
    }
    ZonedDateTime dateTime = getTimestamp(timestamp);

    ConversionDTO convertedBg = BloodGlucoseConverter.convert(sgv, "mg");

    if(delta.length() > 0) {
      ConversionDTO convertedDelta = BloodGlucoseConverter.convert(delta.replaceAll("-", ""), "mg");
      dto.setDelta(convertedDelta);
    }

    dto.setGlucose(convertedBg);
    dto.setDeltaIsNegative(delta.contains("-"));
    dto.setDateTime(dateTime);
  }

  private ZonedDateTime getTimestamp(Long epoch) {

    Instant i = Instant.ofEpochSecond(epoch / 1000);
    return ZonedDateTime.ofInstant(i, ZoneOffset.UTC);

  }
}
