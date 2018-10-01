import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoleListener extends ListenerAdapter {
  Logger logger = LoggerFactory.getLogger(RoleListener.class);

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if (event.getAuthor().isBot()) return;

    User author = event.getAuthor();
    Guild guild = event.getGuild();
    GuildController mod = new GuildController(event.getGuild());
    String roleName = "cool role"; // TODO: make configurable with a command
    List<Role> roles = guild.getRolesByName(roleName, true);

    logger.debug("{} roles found", roles.size());

    if (roles.size() == 0) {
      logger.error("Couldn't find role {}", roleName);
      return;
    }

    Member member = guild.getMember(author);

    logger.trace("member.getEffectiveName() = " + member.getEffectiveName());

    mod.addSingleRoleToMember(member, roles.get(0)).queue();
  }

}
