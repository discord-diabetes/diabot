package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;

@CommandInfo(
    name = {"Reply"},
    description = "Replies in a bunch of weird ways",
    requirements = {"You must be cool"},
    usage = "what is this even for"
)
public class ReplyCommand extends DiabotCommand {

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

    EmbedBuilder builder = new EmbedBuilder();

    builder.setTitle("cool title");
    builder.setAuthor("Cas Eliëns", "https://dongtronic.com");
    builder.addField("field 1", "the first field value, inline `false`", false);
    builder.addField("Field 2", "the second field value, inline `true`", true);

    builder.setImage("https://i.eliens.co/1539536815557.jpg");
    builder.setThumbnail("https://i.eliens.co/1539536815557.jpg");

    builder.setDescription("the description");
    builder.appendDescription("\nSome more description");
    builder.appendDescription("\nLorem ipsum dolor sit amet, consectetur adipiscing elit.\n");
    builder.appendDescription("שלום חבר שלי");

    builder.setFooter("this is the footer", "https://i.eliens.co/1539537165227.gif");

    builder.setColor(Color.magenta);

    MessageEmbed embed = builder.build();

    event.reply(embed);
  }
}
