package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
    name = "Shutdown",
    description = "Safely shuts down the bot."
)
public class ShutdownCommand extends Command {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownCommand.class);

  public ShutdownCommand() {
    this.name = "shutdown";
    this.help = "safely shuts off the bot";
    this.guildOnly = false;
    this.ownerCommand = false;
    this.aliases = new String[] {"heckoff", "fuckoff", "removethyself", "remove"};
  }

  @Override
  protected void execute(CommandEvent event) {
    String userId = event.getAuthor().getId();
    boolean allowed = false;

    //TODO: replace list of allowed admins with config file
    String[] allowedUsers = {
        "125616270254014464", //Adi
        "189436077793083392"  //Cas
    };


    for (String user : allowedUsers) {
      if (user.equals(userId)) {
        allowed = true;
        break;
      }
    }

    if (allowed) {
      logger.info("Shutting down bot (requested by " + event.getAuthor().getName() + " - " + userId + ")");
      event.replyWarning("Shutting down (requested by " + event.getAuthor().getName() + ")");
      event.reactWarning();
      event.getJDA().shutdown();
    }
  }

}