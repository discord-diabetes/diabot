package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.Permission;

import java.time.temporal.ChronoUnit;

@CommandInfo(
    name = {"Test"},
    description = "Only admins should be allowed to run this"
)

public class TestCommand extends Command {

  public TestCommand() {
    this.name = "ping";
    this.help = "checks the bot's latency";
    this.guildOnly = false;
    this.aliases = new String[]{"pong"};
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reply("You have Admin permission");
  }
}
