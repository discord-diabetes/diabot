package com.dongtronic.diabot;

import com.dongtronic.diabot.commands.*;
import com.dongtronic.diabot.listener.ConversionListener;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws LoginException {
    String token = System.getenv("discord-rolebot-token");
    if(System.getenv("DIABOTTOKEN") != null) {
      token = System.getenv("DIABOTTOKEN"); // token on dokku
    }

    // create command categories
    Category adminCategory = new Category("Admin");
    Category bgCategory = new Category("BG conversions");
    Category a1cCategory = new Category("A1c estimations");
    Category funCategory = new Category("Fun");
    Category utilitiesCategory = new Category("Utilities");

    // define an eventwaiter, dont forget to add this to the JDABuilder!
    EventWaiter waiter = new EventWaiter();

    // define a command client
    CommandClientBuilder client = new CommandClientBuilder();

    // The default is "Type !!help" (or whatver prefix you set)
    client.useDefaultGame();

    // sets emojis used throughout the bot on successes, warnings, and failures
    client.setEmojis("\uD83D\uDC4C", "\uD83D\uDE2E", "\u274C");


    // sets the bot prefix
    if(System.getenv("DIABOT_DEBUG") != null) {
      client.setPrefix("dl ");
    } else {
      client.setPrefix("diabot ");
    }

    client.setOwnerId("125616270254014464");

    // adds commands

    // A1c
    client.addCommands(
        // command to show information about the bot
        new AboutCommand(Color.BLUE, "a diabetes bot",
            new String[]{"BG conversions", "A1c estimations", "Secret admin features :blobcoy:"},
            new Permission[]{Permission.ADMINISTRATOR}),


        // A1c
        new EstimationCommand(a1cCategory),

        // BG
        new ConvertCommand(bgCategory),

        // Utility
        new PingCommand(utilitiesCategory),

        // Fun
        new ExcuseCommand(funCategory),
        new AwyissCommand(funCategory),

        // Admin
        new ShutdownCommand(adminCategory),
        new ReplyCommand(adminCategory),
        new RolesCommand(adminCategory));



    // start getting a bot account set up
    new JDABuilder(AccountType.BOT)
        // set the token
        .setToken(token)

        // set the game for when the bot is loading
        .setStatus(OnlineStatus.DO_NOT_DISTURB)
        .setGame(Game.playing("loading..."))

        // add the listeners
        .addEventListener(waiter)
        .addEventListener(client.build())
        .addEventListener(new ConversionListener())

        // start it up!
        .build();


  }

}
