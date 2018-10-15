package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.converters.BloodGlucoseConverter;
import com.dongtronic.diabot.converters.GlucoseUnit;
import com.dongtronic.diabot.data.ConversionDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandInfo(
    name = {"Convert"},
    description = "Convert blood glucose between mmol/L and mg/dL"
)

public class ConvertCommand extends DiabotCommand {

  private Logger logger = LoggerFactory.getLogger(ConvertCommand.class);

  public ConvertCommand(Category category) {
    this.name = "convert";
    this.help = "convert blood glucose between mmol/L and mg/dL";
    this.guildOnly = false;
    this.arguments = "<value> <unit>";
    this.category = category;
    this.examples = new String[]{"diabot convert 5", "My BG this morning was _127_", "How much is 7 mmol?"};
  }

  @Override
  protected void execute(CommandEvent event) {
    if (event.getArgs().isEmpty()) {
      event.replyWarning("You didn't give me a value!");
    } else {
      // split the arguments on all whitespaces
      String[] items = event.getArgs().split("\\s+");

      ConversionDTO result;

      try {

        logger.info("converting BG value " + items[0]);

        result = BloodGlucoseConverter.convert(items[0], (items.length == 2 ? items[1] : null));

        if (result.getInputUnit() == GlucoseUnit.MMOL) {
          event.reply(String.format("%s mmol/L is %s mg/dL", result.getOriginal(), result.getConverted()));
        } else if (result.getInputUnit() == GlucoseUnit.MGDL) {
          event.reply(String.format("%s mg/dL is %s mmol/L", result.getOriginal(), result.getConverted()));
        } else {
          String reply = String.join(
              "%n",
              "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
              "%s mg/dL is **%s mmol/L**",
              "%s mmol/L is **%s mg/dL**");

          event.reply(String.format(reply, result.getOriginal(), result.getMmol(), result.getOriginal(),
              result.getMgdl()));
        }
      } catch (IllegalArgumentException ex) {
        // Ignored on purpose
        logger.warn("IllegalArgumentException occurred but was ignored in BG conversion");
      } catch (UnknownUnitException ex) {
        event.replyError("I don't know how to convert from " + items[1]);
        logger.warn("Unknown BG unit " + items[1]);
      }

    }
  }
}
