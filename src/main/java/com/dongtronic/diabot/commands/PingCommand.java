package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;

import java.time.temporal.ChronoUnit;

@CommandInfo(
    name = {"Ping", "Pong"},
    description = "Checks the bot's latency"
)

public class PingCommand extends DiabotCommand {

  public PingCommand(Category category) {
    this.name = "ping";
    this.help = "checks the bot's latency";
    this.guildOnly = true;
    this.aliases = new String[]{"pong"};
    this.category = category;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reply("Ping: ...", m -> {
      long ping = event.getMessage().getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS);
      m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getPing() + "ms").queue();
    });
  }
}
