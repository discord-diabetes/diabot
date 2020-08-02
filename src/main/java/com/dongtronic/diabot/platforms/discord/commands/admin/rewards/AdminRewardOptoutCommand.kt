package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.redis.RewardDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminRewardOptoutCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "optout"
        this.help = "Opt user out of rewards"
        this.guildOnly = true
        this.aliases = arrayOf("o", "oo")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 1) {
            throw IllegalArgumentException("UserID is required")
        }

        if (!StringUtils.isNumeric(args[0])) {
            throw IllegalArgumentException("UserID must be numeric")
        }

        val userId = args[0]
        val guildId = event.guild.id

        val user = event.guild.getMemberById(userId) ?: throw IllegalArgumentException("User `$userId` does not exist")

        RewardDAO.getInstance().optOut(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted out of rewards")
    }


}
