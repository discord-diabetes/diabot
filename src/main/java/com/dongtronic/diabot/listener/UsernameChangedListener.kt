package com.dongtronic.diabot.listener

import com.dongtronic.diabot.data.AdminDAO
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class UsernameChangedListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(UsernameChangedListener::class.java)

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val prevNick = if (event.oldNickname.isNullOrEmpty()) event.user.name else event.oldNickname
        val newNick = if (event.newNickname.isNullOrEmpty()) event.user.name else event.newNickname

        enforceRules(newNick!!, event)
    }

    private fun enforceRules(username: String, event: GuildMemberUpdateNicknameEvent) {
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

        event.user.openPrivateChannel().queue {
            it.sendMessage("You just changed your username in **${event.guild.name}** to $username. \n" +
                    "However, your new username does not match our naming rules. " +
                    hint).queue()
        }
    }
}
