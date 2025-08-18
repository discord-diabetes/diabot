package com.dongtronic.diabot

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.platforms.discord.commands.admin.AdminCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.OwnerCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.RolesCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.ShutdownCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.ConvertCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.EstimationCommand
import com.dongtronic.diabot.platforms.discord.commands.info.AboutCommand
import com.dongtronic.diabot.platforms.discord.commands.info.InfoCommand
import com.dongtronic.diabot.platforms.discord.commands.misc.*
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutAdminCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.platforms.discord.commands.rewards.RewardsCommand
import com.dongtronic.diabot.platforms.discord.listeners.*
import com.github.kaktushose.jda.commands.JDACommands
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.GuildlistCommand
import dev.minn.jda.ktx.jdabuilder.injectKTX
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.awt.Color
import java.util.*
import javax.security.auth.login.LoginException

object Main {
    private val debug = System.getenv("DIABOT_DEBUG") != null
    private val permissions = arrayOf(
        // General Permissions
        Permission.MANAGE_ROLES,
        Permission.NICKNAME_MANAGE,
        Permission.VIEW_CHANNEL,
        // Text Permissions
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_SEND_IN_THREADS,
        Permission.MESSAGE_MANAGE,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MESSAGE_HISTORY,
        Permission.MESSAGE_EXT_EMOJI,
        Permission.MESSAGE_ADD_REACTION,
        Permission.USE_APPLICATION_COMMANDS,
    )

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Database migration
        MigrationManager().initialize()

        val token = System.getenv("DIABOTTOKEN") // token on dokku

        // define a command client
        val client = CommandClientBuilder()
        val waiter = EventWaiter()

        // The default is "Type !!help" (or whatver prefix you set)
        client.useDefaultGame()
        client.useHelpBuilder(true)

        // sets emojis used throughout the bot on successes, warnings, and failures
        client.setEmojis("\uD83D\uDC4C", "\uD83D\uDE2E", "\uD83D\uDE22")

        // sets the bot prefix
        val prefix = if (debug) "dl " else "diabot "
        client.setPrefix(prefix)

        client.setOwnerId("189436077793083392") // Cas
        client.setCoOwnerIds("125616270254014464", "319371513159614464") // Adi, Garlic

        client.setServerInvite("https://discord.gg/diabetes")

        // application commands are handled outside this library, so tell it not to upsert commands
        client.setManualUpsert(true)

        // add commands
        addClientCommands(client, waiter)

        // Custom help handler
        client.setHelpConsumer(HelpListener())

        val builtClient = client.build()

        val shardManager = DefaultShardManagerBuilder.createDefault(token)
            .setEnabledIntents(
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.MESSAGE_CONTENT,
            )
            .disableCache(EnumSet.allOf(CacheFlag::class.java)) // We don't need any cached data
            .setShardsTotal(-1) // Let Discord decide how many shards we need
            .injectKTX() // Add coroutine event support
            .addEventListeners(
                waiter,
                builtClient,
                ConversionListener(prefix),
                RewardListener(),
                UsernameEnforcementListener(),
                OhNoListener(),
                QuoteListener(builtClient),
            )
            .build()

        JDACommands.start(shardManager, Main::class.java)
    }

    private fun addClientCommands(
        client: CommandClientBuilder,
        waiter: EventWaiter
    ) {
        // create command categories
        val adminCategory = Category("Admin")
        val bgCategory = Category("BG conversions")
        val a1cCategory = Category("A1c estimations")
        val funCategory = Category("Fun")
        val utilitiesCategory = Category("Utilities")
        val infoCategory = Category("Informative")

        client.addCommands(
            // command to show information about the bot
            AboutCommand(
                utilitiesCategory, Color(0, 0, 255), "a diabetes bot",
                arrayOf("Converting between mmol/L and mg/dL", "Performing A1c estimations", "Showing Nightscout information"),
                permissions
            ),

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
            OwnerCommand(funCategory),
            QuoteCommand(funCategory),

            // Admin
            AdminCommand(adminCategory),
            ShutdownCommand(adminCategory),
            NightscoutAdminCommand(adminCategory),
            RolesCommand(adminCategory),
            GuildlistCommand(waiter)
        )
    }
}
