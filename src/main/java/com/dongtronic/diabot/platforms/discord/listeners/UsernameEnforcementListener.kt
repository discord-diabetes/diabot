package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.data.mongodb.NameRuleDTO
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class UsernameEnforcementListener : ListenerAdapter() {
    private val logger = logger()

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val prevNick = event.oldNickname ?: event.user.name
        val newNick = event.newNickname ?: event.user.name

        NameRuleDAO.instance.getGuild(event.guild.id).subscribe({
            enforceRules(newNick, it, event)
        }, {
            if (it !is NoSuchElementException) {
                logger.warn("Could not access name rules for ${event.guild.id}")
            }
        })
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val name = event.user.name

        NameRuleDAO.instance.getGuild(event.guild.id).subscribe({
            enforceRules(name, it, event)
        }, {
            if (it !is NoSuchElementException) {
                logger.warn("Could not access name rules for ${event.guild.id}")
            }
        })
    }

    private fun enforceRules(username: String, rules: NameRuleDTO, event: GenericGuildMemberEvent) {
        val guildId = event.guild.id

        if (!rules.enforce) return
        if (rules.pattern.isBlank()) {
            logger.warn("Username enforcement enabled without configured pattern in guild $guildId")
            return
        }

        val pattern = rules.pattern.toRegex()

        if (pattern.containsMatchIn(username)) {
            return
        }

        val message = when {
            (event is GuildMemberUpdateNicknameEvent) -> "You just changed your username in **${event.guild.name}** to $username. \n" +
                    "However, your new username does not match our naming rules. Please update your username to something that matches these rules: \n" +
                    rules.hintMessage
            (event is GuildMemberJoinEvent) -> "Thank you for joining **${event.guild.name}**. \n" +
                    "We have some guidelines regarding usernames so everyone can easily type your name. " +
                    "Unfortunately, your current nickname does not match our guidelines. Please update your username to something that matches these rules: \n" +
                    rules.hintMessage
            else -> "Thank you for joining **${event.guild.name}**"
        }

        if (event is GuildMemberUpdateNicknameEvent) {
            event.user.openPrivateChannel().queue {
                it.sendMessage(message).queue()
            }
        }
    }
}
