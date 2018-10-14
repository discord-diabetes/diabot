package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandInfo(
    name = {"estimate"},
    description = "pls work"
)

public class A1cTestClass extends Command {

  private Logger logger = LoggerFactory.getLogger(A1cTestClass.class);

  public A1cTestClass() {
    this.name = "estimate";
    this.help = "how bout now";
    this.guildOnly = false;
    this.arguments = "<average> <unit>";
    this.aliases = new String[]{"estimate a1c", "estimate a1c from average"};
  }

  @Override
  protected void execute(CommandEvent event) {
    logger.info("doing the thing");
  }
}
