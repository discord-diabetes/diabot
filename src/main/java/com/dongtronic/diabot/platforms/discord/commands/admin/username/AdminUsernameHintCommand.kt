package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernameHintCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

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
