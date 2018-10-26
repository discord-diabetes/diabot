package com.dongtronic.diabot.commands

import com.dongtronic.diabot.data.AdminDAO
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.CommandUtils
import com.dongtronic.diabot.util.RoleUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Role
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

class AdminCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(AdminCommand::class.java)

    init {
        this.name = "admin"
        this.help = "Administrator commands"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.category = category
        this.examples = arrayOf("diabot admin channels add <channelId>", "diabot admin channels delete <channelId>", "diabot admin channels list", "diabot admin rewards list", "diabot admin rewards add <required role ID> <reward role ID>")
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("must include operation")
        }

        val category = args[0].toUpperCase()
        val command = args[1].toUpperCase()

        try {
            when (category) {
                "C", "CHANNELS" -> when (command) {
                    "LIST", "L" -> listChannels(event)
                    "DELETE", "REMOVE", "D", "R" -> deleteChannel(event)
                    "ADD", "A" -> addChannel(event)
                    else -> {
                        throw IllegalArgumentException("unknown command $category $command")
                    }
                }
                "R", "ROLES", "REWARDS" -> when (command) {
                    "LIST", "L" -> listRewards(event)
                    "ADD", "A" -> addReward(event)
                    "DELETE", "REMOVE", "D", "R" -> deleteReward(event)
                    "OPTIN", "OI", "I" -> optIn(event)
                    "OPTOUT", "OO", "O" -> optOut(event)
                    "LISTOPTOUT", "LO" -> listOptOuts(event)
                    else -> {
                        throw IllegalArgumentException("unknown command $category $command")
                    }
                }
                else -> {
                    throw IllegalArgumentException("unknown command $category $command")
                }
            }


        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }

    }

    private fun listChannels(event: CommandEvent) {
        val channels = AdminDAO.getInstance().listAdminChannels(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Admin channels")

        for (channelId in channels!!) {
            val channel = event.jda.getTextChannelById(channelId)

            builder.appendDescription("**${channel.name}** (`${channel.id}`)\n")
        }

        event.reply(builder.build())
    }

    private fun addChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[2]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().addAdminChannel(event.guild.id, channelId)

        event.reply("Added admin channel ${channel.name} (`$channelId`)")
    }

    private fun deleteChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[2]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().removeAdminChannel(event.guild.id, channelId)

        event.reply("Removed admin channel ${channel.name} (`$channelId`)")

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

        if (args.size != 4) {
            throw IllegalArgumentException("Required and reward role IDs are required")
        }

        if (!StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
            throw IllegalArgumentException("Role IDs must be numeric")
        }

        val requiredId = args[2]
        val rewardId = args[3]

        val requiredRole = event.jda.getRoleById(requiredId)
                ?: throw IllegalArgumentException("Role $requiredId does not exist")
        val rewardRole = event.jda.getRoleById(rewardId)
                ?: throw IllegalArgumentException("Role $rewardId does not exist")

        RewardDAO.getInstance().addSimpleReward(event.guild.id, requiredId, rewardId)

        event.reply("Added reward **${rewardRole.name}** for **${requiredRole.name}**")
    }

    private fun deleteReward(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 4) {
            throw IllegalArgumentException("Required and reward role IDs are required")
        }

        if (!StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
            throw IllegalArgumentException("Role IDs must be numeric")
        }

        val requiredId = args[2]
        val rewardId = args[3]

        val requiredRole = event.jda.getRoleById(requiredId)
                ?: throw IllegalArgumentException("Role $requiredId does not exist")
        val rewardRole = event.jda.getRoleById(rewardId)
                ?: throw IllegalArgumentException("Role $rewardId does not exist")

        RewardDAO.getInstance().removeSimpleReward(event.guild.id, requiredId, rewardId)

        event.reply("Removed reward **${rewardRole.name}** for **${requiredRole.name}**")
    }

    private fun optIn(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("UserID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("UserID must be numeric")
        }

        val userId = args[2]
        val guildId = event.guild.id

        val user = event.guild.getMemberById(userId) ?: throw IllegalArgumentException("User `$userId` does not exist")

        RewardDAO.getInstance().optIn(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted in for rewards")
    }

    private fun optOut(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("UserID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("UserID must be numeric")
        }

        val userId = args[2]
        val guildId = event.guild.id

        val user = event.guild.getMemberById(userId) ?: throw IllegalArgumentException("User `$userId` does not exist")

        RewardDAO.getInstance().optOut(guildId, userId)

        event.reply("User ${user.effectiveName} (`$userId`) opted out of rewards")
    }

    private fun listOptOuts(event: CommandEvent) {
        if(!CommandUtils.requireAdminChannel(event)) {
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
