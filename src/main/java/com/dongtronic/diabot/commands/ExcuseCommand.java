package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.ServerRoles;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.Permission;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@CommandInfo(
    name = {"Excuse"},
    description = "Gib excus"
)
public class ExcuseCommand extends Command {

  public ExcuseCommand(Category category) {
    this.name = "excuse";
    this.help = "gibs excus";
    this.category = category;
    this.requiredRole = ServerRoles.required;
    this.guildOnly = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    try {
      Document html = Jsoup.connect("http://programmingexcuses.com/").get();

      String excuse = html.select("a").first().text();

      event.reply(excuse);
    } catch (IOException e) {
      event.replyError("Oops");
    }
  }
}
