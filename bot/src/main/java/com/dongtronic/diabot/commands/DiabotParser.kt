package com.dongtronic.diabot.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext
import cloud.commandframework.meta.CommandMeta

/**
 * Wrapper class for [AnnotationParser] which provides features unique to Diabot.
 *
 * @param C Command sender type
 * @property cmdManager The command manager
 * @property parser The annotation parser instance associated with this [DiabotParser] object
 * @property syntaxFormatter A custom syntax formatter for `DisplayName` annotations
 */
class DiabotParser<C>(
        private val cmdManager: CommandManager<C>,
        senderClass: Class<C>
) {
    val parser = createAnnotationParser(cmdManager, senderClass)
    val syntaxFormatter = CustomSyntaxFormatter<C>()

    init {
        cmdManager.commandSyntaxFormatter = syntaxFormatter
    }

    /**
     * Adds support for automatic permission setting to this parser.
     *
     * @return this [DiabotParser] instance
     */
    fun addAutoPermissionSupport() = apply { ParserUtils.registerAutoPermission(parser) }

    /**
     * Adds support for CommandCategory annotations to this parser.
     *
     * @return this [DiabotParser] instance
     */
    fun addCategorySupport() = apply { ParserUtils.registerCategories(parser) }

    /**
     * Adds support for Example annotations to this parser.
     *
     * @return this [DiabotParser] instance
     */
    fun addExampleSupport() = apply { ParserUtils.registerExamples(parser) }

    /**
     * Adds support for GuildOnly annotations to this parser.
     *
     * @param notifier Notifier that gets called when a command is executed in a non-guild environment
     * @return this [DiabotParser] instance
     */
    fun addGuildOnlySupport(notifier: (CommandPostprocessingContext<C>) -> Unit) = apply {
        ParserUtils.registerGuildOnly(parser, cmdManager, notifier)
    }

    /**
     * Adds support for DiscordPermission annotations to this parser.
     *
     * @return this [DiabotParser] instance
     */
    fun addDiscordPermissionSupport() = apply { ParserUtils.registerDiscordPermissions(parser) }

    /**
     * Parse class instances for DisplayName annotations and commands.
     *
     * @param instances Array of classes to parse
     * @return this [DiabotParser] instance
     */
    fun parse(instances: Array<Any>) = apply {
        ParserUtils.parseDisplayNames(syntaxFormatter, instances)
        instances.forEach { parser.parse(it) }
    }

    companion object {
        /**
         * Creates an [AnnotationParser] instance with a command meta containing the command's description.
         *
         * @param cmdManager The command manager in use
         * @param senderClass The class of the command sender type
         * @return An annotation parser instance
         */
        fun <C> createAnnotationParser(
                cmdManager: CommandManager<C>,
                senderClass: Class<C>
        ) = AnnotationParser(cmdManager, senderClass) {
            CommandMeta.simple()
                    .with(CommandMeta.DESCRIPTION, it.get(StandardParameters.DESCRIPTION, "No description"))
                    .build()
        }
    }
}