package com.dongtronic.diabot

import com.dongtronic.diabot.platforms.discord.commands.admin.AdminCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.RolesCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.ShutdownCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.ConvertCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.EstimationCommand
import com.dongtronic.diabot.platforms.discord.commands.info.InfoCommand
import com.dongtronic.diabot.platforms.discord.commands.misc.*
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutAdminCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutCommand
import com.dongtronic.diabot.platforms.discord.commands.rewards.RewardsCommand
import com.dongtronic.diabot.platforms.discord.listeners.*
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import javax.security.auth.login.LoginException

object Main {

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val token = System.getenv("DIABOTTOKEN") // token on dokku

        // create command categories
        val adminCategory = Category("Admin")
        val bgCategory = Category("BG conversions")
        val a1cCategory = Category("A1c estimations")
        val funCategory = Category("Fun")
        val utilitiesCategory = Category("Utilities")
        val infoCategory = Category("Informative")

        // define an eventwaiter, dont forget to add this to the JDABuilder!
        val waiter = EventWaiter()

        // define a command client
        val client = CommandClientBuilder()

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

                // Admin
                AdminCommand(adminCategory),
                ShutdownCommand(adminCategory),
                NightscoutAdminCommand(adminCategory),
                RolesCommand(adminCategory))


        // Custom help handler
        client.setHelpConsumer(HelpListener())

        // start getting a bot account set up
        JDABuilder(token)
                // set the game for when the bot is loading
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "Loading..."))

                // add the listeners
                .addEventListeners(
                        waiter,
                        client.build(),
                        ConversionListener(),
                        RewardListener(),
                        UsernameEnforcementListener(),
                        OhNoListener()
                )
                // start it up!
                .build()


    }

}
