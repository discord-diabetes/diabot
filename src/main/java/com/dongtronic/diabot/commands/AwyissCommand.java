package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.ServerRoles;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.awt.*;

@CommandInfo(
    name = {"Awyiss"},
    description = "aww yissss"
)
public class AwyissCommand extends Command {

  public AwyissCommand(Category category) {
    this.name = "awyiss";
    this.help = "muther f'in breadcrumbs";
    this.guildOnly = true;
    this.aliases = new String[]{"duck", "breadcrumbs"};
    this.requiredRole = ServerRoles.required;
    this.category = category;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reactSuccess();

    String url = "http://awyisser.com/api/generator";

    try {
      HttpClient client = new HttpClient();
      PostMethod method = new PostMethod(url);

      //Add any parameter if u want to send it with Post req.
      method.addParameter("phrase", event.getArgs());

      int statusCode = client.executeMethod(method);

      if (statusCode == -1) {
        event.reactError();
      }

      String json = method.getResponseBodyAsString();

      JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
      String imageUrl = jsonObject.get("link").getAsString();

      EmbedBuilder builder = new EmbedBuilder();

      builder.setTitle("Awyiss - " + event.getArgs());
      builder.setAuthor(event.getAuthor().getName());
      builder.setImage(imageUrl);
      builder.setColor(Color.white);

      MessageEmbed embed = builder.build();

      event.reply(embed);
    } catch (Exception e) {
      event.replyError("Something went wrong: " + e.getMessage());
    }


  }
}
