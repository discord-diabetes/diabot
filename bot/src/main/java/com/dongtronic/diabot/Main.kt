package com.dongtronic.diabot

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.platforms.discord.commands.admin.AdminCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.OwnerCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.RolesCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.ShutdownCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.ConvertCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.EstimationCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.EstimationSlashCommand
import com.dongtronic.diabot.platforms.discord.commands.info.InfoCommand
import com.dongtronic.diabot.platforms.discord.commands.misc.*
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutAdminCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.platforms.discord.commands.rewards.RewardsCommand
import com.dongtronic.diabot.platforms.discord.listeners.*
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_A1C
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_AVG
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_UNIT
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_MODE_A1C
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_MODE_AVG
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_NAME
import com.dongtronic.diabot.util.CommandValues.NIGHTSCOUT_COMMAND_ARG_URL
import com.dongtronic.diabot.util.CommandValues.NIGHTSCOUT_COMMAND_GROUP_CLEAR
import com.dongtronic.diabot.util.CommandValues.NIGHTSCOUT_COMMAND_GROUP_SET
import com.dongtronic.diabot.util.CommandValues.NIGHTSCOUT_COMMAND_MODE_URL
import com.dongtronic.diabot.util.CommandValues.NIGHTSCOUT_COMMAND_NAME
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import com.jagrosh.jdautilities.examples.command.GuildlistCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*
import javax.security.auth.login.LoginException


object Main {

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Database migration
        MigrationManager().initialize()

        val token = System.getenv("DIABOTTOKEN") // token on dokku

        // create command categories
        val adminCategory = Category("Admin")
        val bgCategory = Category("BG conversions")
        val a1cCategory = Category("A1c estimations")
        val funCategory = Category("Fun")
        val utilitiesCategory = Category("Utilities")
        val infoCategory = Category("Informative")

        // define a command client
        val client = CommandClientBuilder()
        val waiter = EventWaiter()

        // The default is "Type !!help" (or whatver prefix you set)
        client.useDefaultGame()
        client.useHelpBuilder(true)

        // sets emojis used throughout the bot on successes, warnings, and failures
        client.setEmojis("\uD83D\uDC4C", "\uD83D\uDE2E", "\uD83D\uDE22")


        // sets the bot prefix
        if (System.getenv("DIABOT_DEBUG") != null) {
            client.setPrefix("dl ")
        } else {
            client.setPrefix("diabot ")
        }

        client.setOwnerId("189436077793083392") // Cas
        client.setCoOwnerIds("125616270254014464", "319371513159614464") // Adi, Garlic

        client.setServerInvite("https://discord.gg/diabetes")

        // adds commands
        client.addCommands(
                // command to show information about the bot
                AboutCommand(java.awt.Color(0, 0, 255), "a diabetes bot",
                        arrayOf("Converting between mmol/L and mg/dL", "Performing A1c estimations", "Showing Nightscout information"),
                        Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS, Permission.MANAGE_ROLES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.NICKNAME_MANAGE),


                // A1c
                EstimationCommand(a1cCategory),

                // BG
                ConvertCommand(bgCategory),
                NightscoutCommand(bgCategory),

                // Utility
                PingCommand(utilitiesCategory),
                RewardsCommand(utilitiesCategory),
                GithubCommand(utilitiesCategory),
                DisclaimerCommand(utilitiesCategory),
                NutritionCommand(utilitiesCategory),

                // Info
                InfoCommand(infoCategory),
                SupportCommand(infoCategory),

                // Fun
                ExcuseCommand(funCategory),
                AwyissCommand(funCategory),
                DiacastCommand(funCategory),
                OwnerCommand(funCategory),
                QuoteCommand(funCategory),

                // Admin
                AdminCommand(adminCategory),
                ShutdownCommand(adminCategory),
                NightscoutAdminCommand(adminCategory),
                RolesCommand(adminCategory),
                GuildlistCommand(waiter)
        )


        // Custom help handler
        client.setHelpConsumer(HelpListener())

        val builtClient = client.build()

        val shardManager = DefaultShardManagerBuilder.createDefault(token)
                .setEnabledIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .disableCache(EnumSet.allOf(CacheFlag::class.java)) // We don't need any cached data
                .setShardsTotal(-1) // Let Discord decide how many shards we need
                .addEventListeners(
                        waiter,
                        builtClient,
                        ConversionListener(),
                        RewardListener(),
                        UsernameEnforcementListener(),
                        OhNoListener(),
                        QuoteListener(builtClient),
                ).build()

        registerSlashCommands(shardManager)
    }

    private fun registerSlashCommands(shardManager: ShardManager) {

        shardManager.addEventListener(SlashCommandListener(
                EstimationSlashCommand()
        ))


        val jda = shardManager.shards.first()

        // Global commands
        jda.updateCommands().queue()

        shardManager.shards.forEach { shard ->
            shard.awaitReady()
        }

        val guild = shardManager.getGuildById("646619457694728192")!!

        guild.updateCommands().addCommands(
                CommandData(ESTIMATE_COMMAND_NAME, "Perform A1c and average glucose estimations").addSubcommands(
                        SubcommandData(ESTIMATE_COMMAND_MODE_A1C, "Estimate A1c from average glucose")
                                .addOption(OptionType.NUMBER, ESTIMATE_COMMAND_ARG_AVG, "Average glucose", true)
                                .addOption(OptionType.STRING, ESTIMATE_COMMAND_ARG_UNIT, "Glucose unit (mmol/L, mg/dL)"),
                        SubcommandData(ESTIMATE_COMMAND_MODE_AVG, "Estimate average glucose from A1c")
                                .addOption(OptionType.NUMBER, ESTIMATE_COMMAND_ARG_A1C, "A1c value", true)
                ),
                CommandData(NIGHTSCOUT_COMMAND_NAME, "Manage your nightscout settings").addSubcommandGroups(
                        SubcommandGroupData(NIGHTSCOUT_COMMAND_GROUP_SET, "Set nightscout settings").addSubcommands(
                                SubcommandData(NIGHTSCOUT_COMMAND_MODE_URL, "Set nightscout url").addOption(OptionType.STRING, NIGHTSCOUT_COMMAND_ARG_URL, "URL of your nightscout instance", true)
                        ),
                        SubcommandGroupData(NIGHTSCOUT_COMMAND_GROUP_CLEAR, "Clear nightscout settings").addSubcommands(
                                SubcommandData(NIGHTSCOUT_COMMAND_MODE_URL, "Clear nightscout url")
                        )
                )
        ).queue()

    }

}
