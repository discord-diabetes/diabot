package com.dongtronic.diabot.listener

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.managers.GuildController
import org.slf4j.LoggerFactory

class RoleListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(RoleListener::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent?) {
        if (event!!.author.isBot) return

        val author = event.author
        val guild = event.guild
        val mod = GuildController(event.guild)
        val roleName = "cool role" // TODO: make configurable with a command
        val roles = guild.getRolesByName(roleName, true)

        logger.debug("{} roles found", roles.size)

        if (roles.size == 0) {
            logger.error("Couldn't find role {}", roleName)
            return
        }

        val member = guild.getMember(author)

        logger.trace("member.getEffectiveName() = " + member.effectiveName)

        mod.addSingleRoleToMember(member, roles[0]).queue()
    }

}
