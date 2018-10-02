package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.converters.BloodGlucoseConverter;
import com.dongtronic.diabot.converters.GlucoseUnit;
import com.dongtronic.diabot.exceptions.AmbiguousUnitException;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;

import java.time.temporal.ChronoUnit;

@CommandInfo(
    name = {"Convert"},
    description = "Convert blood glucose between mmol/L and mg/dL"
)

public class ConvertCommand extends Command {

  public ConvertCommand() {
    this.name = "convert";
    this.help = "convert blood glucose between mmol/L and mg/dL";
    this.guildOnly = false;
    this.arguments = "<value>";
  }

  @Override
  protected void execute(CommandEvent event) {
    if (event.getArgs().isEmpty()) {
      event.replyWarning("You didn't give me a value!");
    } else {
      // split the arguments on all whitespaces
      String[] items = event.getArgs().split("\\s+");

      Double bg = null;
      GlucoseUnit inputUnit = null;
      GlucoseUnit outputUnit = null;

      try {
        if (items.length == 1) {
          bg = BloodGlucoseConverter.convert(items[0]);
          inputUnit = BloodGlucoseConverter.detectUnit(items[0]);
        } else if (items.length == 2) {
          bg = BloodGlucoseConverter.convert(items[0], items[1]);
          if (items[1].toUpperCase().contains("MMOL")) {
            inputUnit = GlucoseUnit.MMOL;
          } else if (items[1].toUpperCase().contains("MG")) {
            inputUnit = GlucoseUnit.MGDL;
          }
        }

        if (inputUnit == GlucoseUnit.MGDL){
          outputUnit = GlucoseUnit.MMOL;
        } else {
          outputUnit = GlucoseUnit.MGDL;
        }

        event.replySuccess(String.format("%s %s is %s %s", items[0], inputUnit, bg, outputUnit));

      } catch (AmbiguousUnitException ex) {
        Double[] conversions = BloodGlucoseConverter.convertAmbiguous(items[0]);
        event.replySuccess(String.format("*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*%n%s mmol/L is **%s mg/dL**%n%s mg/dL is **%s mmol/L**", items[0], conversions[0], items[0], conversions[1]));
      } catch (UnknownUnitException ex) {
        event.replyWarning(String.format("Unknown Unit: %s", items[1]));
      } catch (IllegalArgumentException ex) {
        //Purposely ignored
      }

    }
  }
}
