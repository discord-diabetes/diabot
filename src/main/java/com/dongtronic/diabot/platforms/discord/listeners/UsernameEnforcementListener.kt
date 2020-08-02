package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class UsernameEnforcementListener : ListenerAdapter() {
    private val logger = logger()

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val prevNick = if (event.oldNickname.isNullOrEmpty()) event.user.name else event.oldNickname
        val newNick = if (event.newNickname.isNullOrEmpty()) event.user.name else event.newNickname

        enforceRules(newNick!!, event)
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val name = event.user.name
        enforceRules(name, event)
    }

    private fun enforceRules(username: String, event: GenericGuildMemberEvent) {
        val guildId = event.guild.id
        val enabled = AdminDAO.getInstance().getUsernameEnforcementEnabled(guildId)

        if (!enabled) {
            return
        }

        val patternString = AdminDAO.getInstance().getUsernamePattern(guildId)

        if (patternString == null) {
            logger.warn("Username enforcement enabled without configured pattern in guild $guildId")
            return
        }

        val pattern = patternString.toRegex()

        if (pattern.containsMatchIn(username)) {
            return
        }

        val hint = AdminDAO.getInstance().getUsernameHint(guildId)

        val message = when {
            (event is GuildMemberUpdateNicknameEvent) -> "You just changed your username in **${event.guild.name}** to $username. \n" +
                    "However, your new username does not match our naming rules. Please update your username to something that matches these rules: \n" +
                    hint
            (event is GuildMemberJoinEvent) -> "Thank you for joining **${event.guild.name}**. \n" +
                    "We have some guidelines regarding usernames so everyone can easily type your name. " +
                    "Unfortunately, your current nickname does not match our guidelines. Please update your username to something that matches these rules: \n" +
                    hint
            else -> "Thank you for joining **${event.guild.name}**"
        }

        if (event is GuildMemberUpdateNicknameEvent) {
            event.user.openPrivateChannel().queue {
                it.sendMessage(message).queue()
            }
        }
    }
}
