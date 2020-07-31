package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.exceptions.NoCommandFoundException
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.ErrorResponse
import java.awt.Color
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.collections.ArrayList

class HelpListener : Consumer<CommandEvent> {
    private val logger by Logger()

    /**
     * Executed when the attempt to send a DM to a user fails
     */
    private fun sendingError(exc: Throwable, event: CommandEvent) {
        if (exc is ErrorResponseException
                && exc.errorResponse != ErrorResponse.CANNOT_SEND_TO_USER) {
            // Print a warning in console if the error code was not related to DMs being blocked
            logger.warn("Unexpected error response when sending DM: ${exc.errorCode} - ${exc.meaning}")
        }

        event.replyError("Could not send you a DM, please adjust your privacy settings to allow DMs from server members.")
    }

    override fun accept(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            event.replyWarning("Couldn't check your server permissions, help output might display commands you don't have access to.")
        }

        val embedBuilder = EmbedBuilder()

        // event == com.jagrosh.jdautilities.command.CommandEvent
        val allCommands = event.client.commands

        if (event.args.isEmpty()) {
            // Show generic help card
            buildGeneralHelp(allCommands, event)
        } else {
            // Show extended help card
            buildSpecificHelp(embedBuilder, allCommands, event)
            try {
                // Open the DM channel and send the message
                event.author.openPrivateChannel().submit()
                        .thenCompose { it.sendMessage(embedBuilder.build()).submit() }
                        .whenComplete { message: Message?, exc: Throwable? ->
                            if (exc != null) {
                                // If there's a throwable then assume it failed
                                sendingError(exc, event)
                            }
                        }
            } catch (ex: InsufficientPermissionException) {
                event.replyError("Couldn't build help message due to missing permission: `${ex.permission}`")
            }
        }
    }

    private fun buildGeneralHelp(allCommands: List<Command>, event: CommandEvent) {
        val allowedCommands = filterAllowedCommands(allCommands, event)
        val categorizedCommands = groupCommands(allowedCommands)
        val channel = event.author.openPrivateChannel().submit()
        val messageQueue: Queue<CompletableFuture<Message>> = LinkedList()

        for (category in categorizedCommands.entries) {
            val categoryBuilder = EmbedBuilder()
            buildCategoryHelp(categoryBuilder, category)

            // Store the CompletableFuture in the queue so we can cancel it later
            val message = channel.thenCompose { it.sendMessage(categoryBuilder.build()).submit() }
                    .whenComplete { message: Message?, exc: Throwable? ->
                        if (exc != null) {
                            sendingError(exc, event)
                            // Cancel the other messages in the queue
                            messageQueue.forEach { it.cancel(true) }
                        }

                        messageQueue.clear()
                    }
            messageQueue.add(message)
        }
    }

    private fun buildSpecificHelp(builder: EmbedBuilder, allCommands: List<Command>, event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var currentCommand: Command? = null
        val commandIterator = args.asList().iterator()

        try {
            // Loop through arguments and search for commands matching them
            // Stores the parent command in `currentCommand` while searching
            while (commandIterator.hasNext()) {
                val commandName = commandIterator.next()

                currentCommand = getCommand(currentCommand, commandName, allCommands)
            }
        } catch (exception: NoCommandFoundException) {
            val givenCommand = exception.command

            builder.setTitle("error")
            builder.setColor(Color.red)

            if (currentCommand != null) {
                // Parent command exists, tell user the subcommand is nonexistent
                builder.setDescription("Subcommand `$givenCommand` for command `${currentCommand.name}` does not exist")
            } else {
                builder.setDescription("Command `$givenCommand` does not exist")
            }
            return
        }

        buildExtendedCommandHelp(builder, currentCommand!!)
    }

    private fun getCommand(currentCommand: Command?, commandName: String, allCommands: List<Command>): Command {
        // if parent command exists then search through the subcommands for it
        val commands = currentCommand?.children?.asList() ?: allCommands

        for (command in commands) {
            if (command.isCommandFor(commandName)) {
                return command
            }
        }

        throw NoCommandFoundException(commandName)
    }

    private fun buildCategoryHelp(builder: EmbedBuilder, category: Map.Entry<String, ArrayList<Command>>) {
        val categoryName = category.key
        val commands = category.value

        builder.appendDescription("**$categoryName**\n")

        for (command in commands) {
            buildCommandHelp(builder, command, null)
        }

        builder.appendDescription("\n")
    }

    /**
     * Build a basic command help line
     */
    private fun buildCommandHelp(builder: EmbedBuilder, command: Command, parentName: String?) {
        if (parentName != null) {
            builder.appendDescription(parentName)
            builder.appendDescription(" ")
        }

        builder.appendDescription(command.name)
        if (command.arguments != null) {
            builder.appendDescription(" " + command.arguments)
        }

        builder.appendDescription(" => ")
        builder.appendDescription(command.help)
        builder.appendDescription("\n")

        if (command.children.isNotEmpty()) {
            for (subcommand in command.children) {
                builder.appendDescription("   ")
                buildCommandHelp(builder, subcommand, command.name)
            }
        }
    }

    /**
     * Build extended command help card. This includes arguments, permissions, aliases, and examples
     */
    private fun buildExtendedCommandHelp(builder: EmbedBuilder, command: Command) {
        val isExtendedCommand = command is DiscordCommand
        var extendedCommand: DiscordCommand? = null
        if (isExtendedCommand) {
            extendedCommand = command as DiscordCommand
        }

        builder.setTitle("Help")
        builder.setColor(Color.magenta)
        builder.addField("Name", command.name, false)
        builder.addField("Description", command.help, false)

        if (command.arguments != null) {
            builder.addField("Arguments", command.arguments, false)
        }

        if (command.userPermissions.isNotEmpty()) {
            builder.addField("Required permissions", Arrays.toString(command.userPermissions), false)
        }

        if (command.aliases.isNotEmpty()) {
            builder.addField("Aliases", Arrays.toString(command.aliases), false)
        }

        if (isExtendedCommand && extendedCommand!!.parent != null) {
            builder.addField("Parent command", extendedCommand.parent!!.name, false)
        }

        if (command.children.isNotEmpty()) {
            builder.addField("Sub commands", Arrays.toString(command.children), false)
        }

        if (isExtendedCommand && extendedCommand!!.examples.isNotEmpty()) {
            val examples = StringBuilder()

            for (example in extendedCommand.examples) {
                examples.append("`").append(example).append("`\n")
            }

            builder.addField("Examples", examples.toString(), true)
        }
    }

    /**
     * Returns a list of only the commands a user has permission to use
     * @param commands list of all commands
     * @param event original CommandEvent. Used for checking permissions
     * @return list of commands the user is authorized to use
     */
    private fun filterAllowedCommands(commands: List<Command>, event: CommandEvent): ArrayList<Command> {
        val allowedCommands = ArrayList<Command>()

        for (command in commands) {
            if (command.isHidden) {
                continue
            }

            val requiredPermissions = command.userPermissions

            if (requiredPermissions.isEmpty()) {
                allowedCommands.add(command)
                continue
            }

            var userPermissions: EnumSet<Permission> = Permission.getPermissions(Permission.ALL_PERMISSIONS)
            if (event.member != null) {
                userPermissions = event.member.permissions
            }

            var userIsAllowedToUseCommand = true

            for (requiredPermission in requiredPermissions) {
                if (!userPermissions.contains(requiredPermission)) {
                    userIsAllowedToUseCommand = false
                }
            }

            if (userIsAllowedToUseCommand) {
                allowedCommands.add(command)
            }

        }

        return allowedCommands
    }

    /**
     * Returns a collection of commands grouped by category
     * @param commands list of commands that need to be sorted
     * @return collection of grouped and sorted commands
     */
    private fun groupCommands(commands: List<Command>): TreeMap<String, ArrayList<Command>> {
        val categorizedCommands = TreeMap<String, ArrayList<Command>>()

        for (command in commands) {
            var categoryName = "Misc"
            try {
                categoryName = command.category.name
            } catch (ex: IllegalStateException) {
                // Ignored on purpose
            }

            var categoryCommands: ArrayList<Command>? = categorizedCommands[categoryName]

            if (categoryCommands == null) {
                categoryCommands = ArrayList()
            }

            categoryCommands.add(command)

            categorizedCommands[categoryName] = categoryCommands
        }
        return categorizedCommands

    }
}
