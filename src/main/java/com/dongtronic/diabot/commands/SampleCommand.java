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
public class SampleCommand extends Command {

  public SampleCommand(Category category) {
    this.name = "test";
    this.help = "checks the bot's latency";
    this.guildOnly = true;
    this.ownerCommand = false;
    this.aliases = new String[] {"tast", "tost"};
    this.category = category;
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reply("You have Admin permission");
  }
}
