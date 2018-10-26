package com.dongtronic.diabot

import com.dongtronic.diabot.commands.*
import com.dongtronic.diabot.listener.ConversionListener
import com.dongtronic.diabot.listener.FeelListener
import com.dongtronic.diabot.listener.HelpListener
import com.dongtronic.diabot.listener.RoleListener
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import org.slf4j.LoggerFactory
import javax.security.auth.login.LoginException

object Main {

    private val logger = LoggerFactory.getLogger(Main::class.java)

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var token = System.getenv("discord-rolebot-token")
        if (System.getenv("DIABOTTOKEN") != null) {
            token = System.getenv("DIABOTTOKEN") // token on dokku
        }

        // create command categories
        val adminCategory = Category("Admin")
        val bgCategory = Category("BG conversions")
        val a1cCategory = Category("A1c estimations")
        val funCategory = Category("Fun")
        val utilitiesCategory = Category("Utilities")

        // define an eventwaiter, dont forget to add this to the JDABuilder!
        val waiter = EventWaiter()

        // define a command client
        val client = CommandClientBuilder()

        // The default is "Type !!help" (or whatver prefix you set)
        client.useDefaultGame()

        // sets emojis used throughout the bot on successes, warnings, and failures
        client.setEmojis("\uD83D\uDC4C", "\uD83D\uDE2E", "\uD83D\uDE22")


        // sets the bot prefix
        if (System.getenv("DIABOT_DEBUG") != null) {
            client.setPrefix("dl ")
        } else {
            client.setPrefix("diabot ")
        }

        client.setOwnerId("125616270254014464")

        // adds commands
        client.addCommands(
                // command to show information about the bot
                AboutCommand(java.awt.Color(0, 0, 255), "a diabetes bot",
                        arrayOf("BG conversions", "A1c estimations", "Secret admin features :blobcoy:"),
                        Permission.ADMINISTRATOR),


                // A1c
                EstimationCommand(a1cCategory),

                // BG
                ConvertCommand(bgCategory),
                NightscoutCommand(bgCategory),

                // Utility
                PingCommand(utilitiesCategory),
                RewardsCommand(utilitiesCategory),

                // Fun
                ExcuseCommand(funCategory),
                AwyissCommand(funCategory),
                DiacastCommand(funCategory),

                // Admin
                AdminCommand(adminCategory),
                ShutdownCommand(adminCategory),
                ReplyCommand(adminCategory),
                NightscoutAdminCommand(adminCategory),
                RolesCommand(adminCategory))


        // Custom help handler
        client.setHelpConsumer(HelpListener())


        // start getting a bot account set up
        JDABuilder(AccountType.BOT)
                // set the token
                .setToken(token)

                // set the game for when the bot is loading
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setGame(Game.playing("loading..."))

                // add the listeners
                .addEventListener(waiter)
                .addEventListener(client.build())
                .addEventListener(ConversionListener())
                .addEventListener(RoleListener())

                // fun listeners
                .addEventListener(FeelListener())

                // start it up!
                .build()


    }

}
