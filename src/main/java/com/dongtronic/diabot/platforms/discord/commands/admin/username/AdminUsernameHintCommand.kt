package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminUsernameHintCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernameHintCommand::class.java)

    init {
        this.name = "hint"
        this.help = "Set or view username enforcement hint"
        this.guildOnly = true
        this.aliases = arrayOf("h", "help")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            getHint(event)
        } else {
            setHint(event)
        }
    }

    private fun setHint(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("Command must contain pattern")
        }

        val helpString = args.joinToString(" ")

        AdminDAO.getInstance().setUsernameHint(event.guild.id, helpString)

        event.reply("Set username enforcement hint to `$helpString`")
    }

    private fun getHint(event: CommandEvent) {
        val pattern = AdminDAO.getInstance().getUsernameHint(event.guild.id)

        event.reply("Current username hint: `$pattern`")
    }
}
