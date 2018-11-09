package com.dongtronic.diabot.commands

import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory

class AdminUsernameCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernameCommand::class.java)

    init {
        this.name = "usernames"
        this.help = "Username rule enforcement"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("u")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.replyError("must include operation")
            return
        }

        val command = args[0].toUpperCase()

        try {
            when (command) {
                "SET", "S" -> setPattern(event)
                "ENABLE", "E" -> setEnabled(event)
                "DISABLE", "D" -> setDisabled(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }

        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun setPattern(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if(args.size < 2) {
            throw IllegalArgumentException("Command must contain pattern")
        }

        val patternString = args.drop(1).joinToString(" ")
        val pattern = patternString.toRegex()

        AdminDAO.getInstance().setUsernamePattern(event.guild.id, pattern.pattern)

        event.reply("Set username enforcement pattern to `$patternString`")
    }

    private fun setEnabled(event: CommandEvent) {
        AdminDAO.getInstance().setUsernameEnforcementEnabled(event.guild.id, true)

        event.reply("Enabled username enforcement for ${event.guild.name}")
    }

    private fun setDisabled(event: CommandEvent) {
        AdminDAO.getInstance().setUsernameEnforcementEnabled(event.guild.id, false)

        event.reply("Disabled username enforcement for ${event.guild.name}")
    }
}
