package com.dongtronic.diabot.commands.`fun`

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.NicknameUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class RewardsCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(RewardsCommand::class.java)

    init {
        this.name = "rewards"
        this.help = "Opt in or out of automatic role rewards"
        this.guildOnly = true
        this.aliases = arrayOf("reward", "r")
        this.examples = arrayOf("diabot rewards optout", "diabot rewards optin")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("must include operation")
        }

        val command = args[0].toUpperCase()

        try {
            when (command) {
                "OPTIN", "OI", "I" -> optIn(event)
                "OPTOUT", "OU", "O" -> optOut(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun optIn(event: CommandEvent) {
        val guildId = event.guild.id

        val user = event.author

        RewardDAO.getInstance().optIn(guildId, user.id)

        logger.info("User ${user.discriminator} opted in to rewards")
        event.reply("User ${NicknameUtils.determineDisplayName(event, user)} opted in to rewards")
    }

    private fun optOut(event: CommandEvent) {
        val guildId = event.guild.id

        val user = event.author

        RewardDAO.getInstance().optOut(guildId, user.id)

        logger.info("User ${user.discriminator} opted out of rewards")
        event.reply("User ${NicknameUtils.determineDisplayName(event, user)} opted out of rewards")
    }
}
