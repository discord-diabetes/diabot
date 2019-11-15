package com.dongtronic.diabot.commands.admin.rewards

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Role
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

class AdminRewardListOptoutsCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminRewardListOptoutsCommand::class.java)

    init {
        this.name = "listoptouts"
        this.help = "Add role reward"
        this.guildOnly = true
        this.aliases = arrayOf("lo")
    }

    override fun execute(event: CommandEvent) {
        if (!CommandUtils.requireAdminChannel(event)) {
            return
        }

        logger.info("Listing all reward opt-outs for ${event.author.name}")
        val optouts = RewardDAO.getInstance().getOptOuts(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Reward opt-outs")

        optouts?.forEach { optout ->
            val user = event.guild.getMemberById(optout)

            builder.appendDescription("**${user!!.effectiveName}** (`${user.user.id}`)")
        }

        event.reply(builder.build())
    }


}
