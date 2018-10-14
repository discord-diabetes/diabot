package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.Permission;

@CommandInfo(
    name = {"Reply"},
    description = "Replies in a bunch of weird ways"
)
public class ReplyCommand extends Command {

  public ReplyCommand(Category category) {
    this.name = "reply";
    this.help = "replies in a bunch of weird ways";
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    this.category = category;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reply("normal reply");
    event.replySuccess("success");
    event.replyWarning("warning");
    event.replyError("error");
  }
}
