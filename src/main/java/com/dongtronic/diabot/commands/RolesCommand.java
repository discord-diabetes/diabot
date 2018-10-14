package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(
    name = {"Roles"},
    description = "Get all roles in the server"
)
public class RolesCommand extends Command {

  public RolesCommand(Category category) {
    this.name = "roles";
    this.help = "Get all roles in the server";
    this.guildOnly = true;
    this.category = category;
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  protected void execute(CommandEvent event) {

    Guild guild = event.getGuild();

    List<Role> roles = guild.getRoles();

    StringBuilder returned = new StringBuilder().append("```");

    for(Role role : roles) {
      returned.append("\n" + role.getId() + " - " + role.getName());
    }

    returned.append("\n```");

    event.reply(returned.toString());
  }
}
