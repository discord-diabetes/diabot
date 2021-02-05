package com.dongtronic.diabot

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext
import cloud.commandframework.jda.JDA4CommandManager
import com.dongtronic.diabot.commands.DiabotHelp
import com.dongtronic.diabot.commands.DiabotParser
import com.dongtronic.diabot.commands.PermissionRegistry
import com.dongtronic.diabot.commands.ReplyType
import com.dongtronic.diabot.commands.annotations.CommandCategory
import com.dongtronic.diabot.commands.cooldown.CooldownIds
import com.dongtronic.diabot.commands.cooldown.CooldownMeta
import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.platforms.discord.commands.JDACommandUser
import com.dongtronic.diabot.platforms.discord.commands.admin.AdminCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.OwnerCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.RolesCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.ShutdownCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.ConvertCommand
import com.dongtronic.diabot.platforms.discord.commands.diabetes.EstimationCommand
import com.dongtronic.diabot.platforms.discord.commands.info.InfoCommand
import com.dongtronic.diabot.platforms.discord.commands.misc.*
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutAdminCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutGraphCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.platforms.discord.commands.rewards.RewardsCommand
import com.dongtronic.diabot.platforms.discord.listeners.*
import com.dongtronic.diabot.util.logger
import com.github.ygimenez.method.Pages
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import javax.security.auth.login.LoginException


object Main {
    private val logger = logger()

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Migrate data from Redis to MongoDB
        MigrationManager().migrateIfNecessary()

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
//                NightscoutCommand(bgCategory),
//                NightscoutGraphCommand(bgCategory),

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
                RolesCommand(adminCategory))


        // Custom help handler
        client.setHelpConsumer(HelpListener())

        val builtClient = client.build()

        val jda = JDABuilder.createLight(token)
                .setEnabledIntents(
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL) // Cache all members regardless of their online state
                .setChunkingFilter(ChunkingFilter.ALL) // Cache all guilds on initialisation
                .disableCache(EnumSet.allOf(CacheFlag::class.java)) // We don't need any JDA cache services
                .addEventListeners(
                        waiter,
                        builtClient,
                        ConversionListener(),
                        RewardListener(),
                        UsernameEnforcementListener(),
                        OhNoListener(),
                        QuoteListener(builtClient)
                ).build()

        // Pagination
        Pages.activate(jda)

        // this will be changed later once the jda-utilities command framework is removed
        val prefix = "!"
        val permissionRegistry = PermissionRegistry()
        val commandManager: JDA4CommandManager<JDACommandUser> = JDA4CommandManager(
                jda,
                Function { prefix },
                BiFunction { sender: JDACommandUser, permission: String ->
                    permissionRegistry.hasPermission(sender, permission)
                },
                CommandExecutionCoordinator.simpleCoordinator(),
                Function {
                    JDACommandUser.of(it)
                },
                Function {
                    it.toJdaCommandSender()
                }
        )

        val diabotHelp = DiabotHelp(
                commandManager,
                prefix,
                { sender: JDACommandUser, message: Message ->
                    sender.reply(message, ReplyType.NONE)
                            .subscribeOn(Schedulers.boundedElastic())
                },
                { sender: JDACommandUser, user: User ->
                    user.id == sender.getAuthorUniqueId()
                }
        )

        DiabotParser(commandManager, JDACommandUser::class.java)
                .addAutoPermissionSupport()
                .addCategorySupport()
                .addExampleSupport()
                .addGuildOnlySupport {
                    it.commandContext.sender.replyErrorS("This command can only be executed in a server.")
                }
                .addCooldownSupport(
                        {
                            val guildId = if (it.event.isFromGuild) it.event.guild.id else ""
                            CooldownIds(
                                    userId = it.getAuthorUniqueId(),
                                    channelId = it.event.channel.id,
                                    guildId = guildId,
                                    shardId = it.event.jda.shardInfo.shardId.toString()
                            )
                        },
                        { millisRemaining: Long, _: CooldownMeta, context: CommandPostprocessingContext<JDACommandUser> ->
                            val secsLeft = (millisRemaining / 1000).toInt()
                            val s = if (secsLeft == 1) "" else "s"
                            context.commandContext.sender.replyErrorS("This command is currently in cooldown for $secsLeft more second$s")
                        }
                )
                .addDiscordPermissionSupport()
                .parse(arrayOf(
                        TestCommand(),
                        HelpCommand(diabotHelp),
                        NightscoutCommand(),
                        NightscoutGraphCommand()
                ))

        commandManager.commandHelpHandler.allCommands.forEach {
            // debug code
            logger.debug("Syntax: ${it.syntaxString}; Desc: ${it.description}")
        }
    }

    class TestCommand {
        @CommandPermission("testcommand")
        @CommandDescription("Test command stuff")
        @CommandMethod("test|tst|tast <channel> [user]")
        @CommandCategory(com.dongtronic.diabot.commands.Category.FUN)
        fun testCommand(
                sender: JDACommandUser,
                @Argument("channel") channel: MessageChannel?,
                @Argument("user") user: User?
        ) {
            val b = StringBuilder()

            b.append("hi ${sender.getAuthorDisplayName()}!")
            if (channel != null) {
                b.append(" (channel ${channel.name})")
            }
            if (user != null) {
                b.append(" (user ${user.name})")
            }

            sender.reply(b.toString())
        }
    }
}
