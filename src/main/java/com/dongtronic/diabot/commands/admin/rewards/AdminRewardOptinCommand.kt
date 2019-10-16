package com.dongtronic.diabot.commands.admin.rewards

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.RewardDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Role
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

class AdminRewardOptinCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminRewardOptinCommand::class.java)

    init {
        this.name = "optin"
        this.help = "Opt user in to rewards"
        this.guildOnly = true
        this.aliases = arrayOf("i", "oi")
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

        RewardDAO.getInstance().optIn(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted in for rewards")
    }


}
