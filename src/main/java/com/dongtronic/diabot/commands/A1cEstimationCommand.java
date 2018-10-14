package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.converters.A1cConverter;
import com.dongtronic.diabot.data.A1cDTO;
import com.dongtronic.diabot.exceptions.UnknownUnitException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dongtronic.diabot.converters.GlucoseUnit.MGDL;
import static com.dongtronic.diabot.converters.GlucoseUnit.MMOL;

@CommandInfo(
    name = {"estimate a1c"},
    description = "estimate a1c from average blood glucose"
)

public class A1cEstimationCommand extends Command {

  private Logger logger = LoggerFactory.getLogger(A1cEstimationCommand.class);

  public A1cEstimationCommand() {
    this.name = "estimatea1c";
    this.help = "estimate a1c from average blood glucose";
    this.guildOnly = false;
    this.arguments = "<average> <unit>";
    this.aliases = new String[]{"estimate a1c", "estimate a1c from average"};
  }

  @Override
  protected void execute(CommandEvent event) {
    if (event.getArgs().isEmpty()) {
      event.replyWarning("You didn't give me a value!");
    } else {
      // split the arguments on all whitespaces
      String[] items = event.getArgs().split("\\s+");

      A1cDTO result;

      try {

        logger.info("Estimating A1c for average " + items[0]);

        if(items.length == 2) {
          result = A1cConverter.estimateA1c(items[0], items[1]);
        } else {
          result = A1cConverter.estimateA1c(items[0], null);
        }

        if (result.getOriginal().getInputUnit() == MMOL) {
          event.replySuccess(String.format("An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.getOriginal().getMmol(), result.getDcct(), result.getIfcc()));
//          event.replySuccess("suh");
        } else if (result.getOriginal().getInputUnit() == MGDL) {
          event.replySuccess(String.format("An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.getOriginal().getMgdl(), result.getDcct(), result.getIfcc()));
//          event.replySuccess("dude");
        } else {
          //TODO: Make arguments for result.getDcct and result.getIfcc less confusing
          //TODO: ie: not wrong
          String reply =
              String.format("An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC) %n", result.getOriginal().getOriginal(), result.getDcct(MGDL), result.getIfcc(MGDL)) +
              String.format("An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.getOriginal().getOriginal(), result.getDcct(MMOL), result.getIfcc(MMOL));
          event.replySuccess(reply);
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
