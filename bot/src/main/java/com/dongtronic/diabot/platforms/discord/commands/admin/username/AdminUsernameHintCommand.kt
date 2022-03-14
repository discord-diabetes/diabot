package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
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
        this.userPermissions = this.parent!!.userPermissions
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

        NameRuleDAO.instance.setHint(event.guild.id, helpString).subscribe({
            event.reply("Set username enforcement hint to `$helpString`")
        }, {
            logger.warn("Could not set username enforcement hint for guild ${event.guild.id}", it)
            event.replyError("Could not set username enforcement hint for ${event.guild.name}")
        })
    }

    private fun getHint(event: CommandEvent) {
        NameRuleDAO.instance.getGuild(event.guild.id).subscribe({
            event.reply("Current username hint: `${it.hintMessage}`")
        }, {
            if (it is NoSuchElementException) {
                event.reply("There is no username enforcement hint set")
            } else {
                logger.warn("Could not get username enforcement hint for guild ${event.guild.id}", it)
                event.replyError("Could not get username enforcement hint for ${event.guild.name}")
            }
        })
    }
}
