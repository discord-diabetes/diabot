package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.CommandUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class AdminRewardListOptoutsCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

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
        RewardsDAO.instance.getOptOuts(event.guild.id)
                .map { it.optOut }
                .defaultIfEmpty(emptyList())
                .subscribe({ optouts ->
                    val builder = EmbedBuilder()

                    builder.setTitle("Reward opt-outs")

                    if (optouts.isEmpty()) {
                        builder.setDescription("No users are opted-out.")
                    }
                    optouts.forEach { optout ->
                        val user = event.guild.getMemberById(optout)

                        builder.appendDescription("**${user!!.effectiveName}** (`${user.user.id}`)")
                    }

                    event.reply(builder.build())
                }, {
                    logger.warn("Could not retrieve opt outs for guild ${event.guild.id}", it)
                    event.replyError("Could not retrieve list of opt outs for this guild")
                })
    }


}
