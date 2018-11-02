package com.dongtronic.diabot.commands

import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.CommandUtils
import com.dongtronic.diabot.util.RoleUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Role
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

class AdminRewardsCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminRewardsCommand::class.java)

    init {
        this.name = "rewards"
        this.help = "Rewards settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("r", "roles")
        this.examples = arrayOf("diabot admin rewards list", "diabot admin rewards add <required role ID> <reward role ID>")
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
                "LIST", "L" -> listRewards(event)
                "ADD", "A" -> addReward(event)
                "DELETE", "REMOVE", "D", "R" -> deleteReward(event)
                "OPTIN", "OI", "I" -> optIn(event)
                "OPTOUT", "OO", "O" -> optOut(event)
                "LISTOPTOUT", "LO" -> listOptOuts(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }

        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun listRewards(event: CommandEvent) {
        val configuredRewards = RewardDAO.getInstance().getSimpleRewards(event.guild.id)

        val rewards = RoleUtils.buildRewardsMap(configuredRewards, event.guild)
        val builder = EmbedBuilder()

        buildRewardResponse(builder, rewards, event)

        event.reply(builder.build())
    }

    private fun addReward(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Required and reward role IDs are required")
        }

        if (!StringUtils.isNumeric(args[1]) || !StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Role IDs must be numeric")
        }

        val requiredId = args[1]
        val rewardId = args[2]

        val requiredRole = event.jda.getRoleById(requiredId)
                ?: throw IllegalArgumentException("Role $requiredId does not exist")
        val rewardRole = event.jda.getRoleById(rewardId)
                ?: throw IllegalArgumentException("Role $rewardId does not exist")

        RewardDAO.getInstance().addSimpleReward(event.guild.id, requiredId, rewardId)

        event.reply("Added reward **${rewardRole.name}** for **${requiredRole.name}**")
    }

    private fun deleteReward(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Required and reward role IDs are required")
        }

        if (!StringUtils.isNumeric(args[1]) || !StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Role IDs must be numeric")
        }

        val requiredId = args[1]
        val rewardId = args[2]

        val requiredRole = event.jda.getRoleById(requiredId)
                ?: throw IllegalArgumentException("Role $requiredId does not exist")
        val rewardRole = event.jda.getRoleById(rewardId)
                ?: throw IllegalArgumentException("Role $rewardId does not exist")

        RewardDAO.getInstance().removeSimpleReward(event.guild.id, requiredId, rewardId)

        event.reply("Removed reward **${rewardRole.name}** for **${requiredRole.name}**")
    }

    private fun optIn(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 2) {
            throw IllegalArgumentException("UserID is required")
        }

        if (!StringUtils.isNumeric(args[1])) {
            throw IllegalArgumentException("UserID must be numeric")
        }

        val userId = args[1]
        val guildId = event.guild.id

        val user = event.guild.getMemberById(userId) ?: throw IllegalArgumentException("User `$userId` does not exist")

        RewardDAO.getInstance().optIn(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted in for rewards")
    }

    private fun optOut(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 2) {
            throw IllegalArgumentException("UserID is required")
        }

        if (!StringUtils.isNumeric(args[1])) {
            throw IllegalArgumentException("UserID must be numeric")
        }

        val userId = args[1]
        val guildId = event.guild.id

        val user = event.guild.getMemberById(userId) ?: throw IllegalArgumentException("User `$userId` does not exist")

        RewardDAO.getInstance().optOut(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted out of rewards")
    }

    private fun listOptOuts(event: CommandEvent) {
        if (!CommandUtils.requireAdminChannel(event)) {
            return
        }

        logger.info("Listing all reward opt-outs for ${event.author.name}")
        val optouts = RewardDAO.getInstance().getOptOuts(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Reward opt-outs")

        optouts?.forEach { optout ->
            val user = event.guild.getMemberById(optout)

            builder.appendDescription("**${user.effectiveName}** (`${user.user.id}`)")
        }

        event.reply(builder.build())
    }

    private fun buildRewardResponse(builder: EmbedBuilder, rewards: TreeMap<Role, MutableList<Role>>, event: CommandEvent) {
        builder.setTitle("${rewards.size} Rewards for ${event.guild.name}")
        builder.setColor(java.awt.Color.ORANGE)

        for ((required, rewardList) in rewards) {
            val rewardString = StringBuilder()
            for (reward in rewardList) {
                rewardString.append(reward.name)
                rewardString.append(", ")
            }
            rewardString.trimEnd(',', ' ')

            builder.appendDescription("**")
            builder.appendDescription(required.name)
            builder.appendDescription("** => ")
            builder.appendDescription(rewardString.toString())
            builder.appendDescription("\n")
        }
    }
}
