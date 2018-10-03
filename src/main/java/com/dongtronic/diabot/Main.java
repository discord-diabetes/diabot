package com.dongtronic.diabot;

import com.dongtronic.diabot.commands.ConvertCommand;
import com.dongtronic.diabot.commands.PingCommand;
import com.dongtronic.diabot.commands.TestCommand;
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

    // define an eventwaiter, dont forget to add this to the JDABuilder!
    EventWaiter waiter = new EventWaiter();

    // define a command client
    CommandClientBuilder client = new CommandClientBuilder();

    // The default is "Type !!help" (or whatver prefix you set)
    client.useDefaultGame();

    // sets emojis used throughout the bot on successes, warnings, and failures
    client.setEmojis("", "", "");

    // sets the bot prefix
    client.setPrefix("diabot ");

    client.setOwnerId("125616270254014464");

    // adds commands
    client.addCommands(
        // command to show information about the bot
        new AboutCommand(Color.BLUE, "an example bot",
            new String[]{"BG conversions", "A1c estimations", "Secret admin features :blobcoy:"},
            new Permission[]{Permission.ADMINISTRATOR}),


        new TestCommand(),

        new ConvertCommand(),
        // command to check bot latency
        new PingCommand());

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

        // start it up!
        .build();


  }

}
