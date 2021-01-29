package com.dongtronic.diabot.commands

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.arguments.StaticArgument
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.permission.AndPermission
import cloud.commandframework.permission.CommandPermission
import cloud.commandframework.permission.OrPermission
import cloud.commandframework.permission.Permission
import cloud.commandframework.services.types.ConsumerService
import com.dongtronic.diabot.commands.annotations.*
import com.dongtronic.diabot.util.logger
import io.leangen.geantyref.TypeToken
import java.util.*

/**
 * Utility class which provides features unique to Diabot for the Cloud framework.
 */
@Suppress("MemberVisibilityCanBePrivate")
object ParserUtils {
    private val logger = logger()
    val META_AUTOPERM_ALLOWED = CommandMeta.Key.of(TypeToken.get(Boolean::class.javaObjectType), "autopermission")
    val META_CATEGORY = CommandMeta.Key.of(TypeToken.get(Category::class.java), "category")
    val META_EXAMPLE = CommandMeta.Key.of(TypeToken.get(Array<String>::class.java), "example")
    val META_GUILDONLY = CommandMeta.Key.of(TypeToken.get(Boolean::class.javaObjectType), "guildonly")

    /**
     * Registers two builder modifiers:
     * - one which adds `META_AUTOPERM_ALLOWED` to commands that have the `NoAutoPermission` annotation
     * - another which applies the automatic permission to commands which are permitted to use automatic permissions
     *
     * @param C Command sender type
     * @param parser The annotation parser to register the builder modifiers with
     */
    fun <C> registerAutoPermission(parser: AnnotationParser<C>) {
        parser.registerBuilderModifier(NoAutoPermission::class.java) { _: NoAutoPermission, builder: Command.Builder<C> ->
            builder.meta(META_AUTOPERM_ALLOWED, false)
        }

        parser.registerBuilderModifier(CommandMethod::class.java) { _: CommandMethod, builder: Command.Builder<C> ->
            val command = builder.build()

            if (!command.commandMeta.get(META_AUTOPERM_ALLOWED).orElseGet { true }) {
                return@registerBuilderModifier builder
            }

            val components = command.components.filter { it.argument is StaticArgument }
            val permissionString = components.joinToString(separator = ".") {
                it.argument.name
            }
            var permission: CommandPermission = Permission.of(permissionString)

            // don't replace permission
            if (builder.commandPermission() != Permission.empty()) {
                permission = OrPermission.of(
                        listOf(
                                builder.commandPermission(),
                                permission
                        ))
            }

            logger.debug("Applied automatic permission $permission")
            builder.permission(permission)
        }
    }

    /**
     * Registers a builder modifier for `CommandCategory` annotations that adds `META_CATEGORY` to the command meta containing
     * the command's specified category.
     *
     * @param C Command sender type
     * @param parser The annotation parser to register the builder modifier with
     */
    fun <C> registerCategories(parser: AnnotationParser<C>) {
        parser.registerBuilderModifier(CommandCategory::class.java) { annotation: CommandCategory, builder: Command.Builder<C> ->
            builder.meta(META_CATEGORY, annotation.category)
        }
    }

    /**
     * Registers a builder modifier for `Example` annotations that adds `META_EXAMPLE` to the command meta containing
     * all of the command's examples.
     *
     * @param C Command sender type
     * @param parser The annotation parser to register the builder modifier with
     */
    fun <C> registerExamples(parser: AnnotationParser<C>) {
        parser.registerBuilderModifier(Example::class.java) { annotation: Example, builder: Command.Builder<C> ->
            builder.meta(META_EXAMPLE, annotation.examples)
        }
    }

    /**
     * Registers a builder modifier for `DiscordPermission` annotations that converts and adds Discord permissions
     * to command permissions.
     *
     * @param C Command sender type
     * @param parser The annotation parser to register the builder modifier with
     */
    fun <C> registerDiscordPermissions(parser: AnnotationParser<C>) {
        parser.registerBuilderModifier(DiscordPermission::class.java) { annotation: DiscordPermission, builder: Command.Builder<C> ->
            val permissions = annotation.permissions.map {
                Permission.of(
                        PermissionRegistry.convertPermission(it)
                )
            }
            var permission = AndPermission.of(permissions)

            // (ALL of the discord permissions) OR (existing permission)
            val existingPermission = builder.commandPermission()
            if (existingPermission != Permission.empty()) {
                permission = OrPermission.of(
                        listOf(
                                existingPermission,
                                permission
                        )
                )
            }

            builder.permission(permission)
        }
    }

    /**
     * Registers a:
     * - builder modifier that adds `META_GUILDONLY` to the command meta
     * - command post processor that checks if the command was executed in a guild
     *
     * From the command postprocessor, if the command was executed in a guild then the command pipeline will be interrupted
     * and the `notifier` parameter will be called.
     *
     * @param C Command sender type
     * @param parser The annotation parser to register the builder modifier with
     * @param cmdManager The command manager to register the command post processor with
     * @param notifier Notifier that gets called when a command is executed in a non-guild environment
     */
    fun <C> registerGuildOnly(parser: AnnotationParser<C>, cmdManager: CommandManager<C>, notifier: (CommandPostprocessingContext<C>) -> Unit) {
        registerCommandLimitation(
                parser,
                cmdManager,
                GuildOnly::class.java,
                META_GUILDONLY,
                limitation = { !it.commandContext.contains("Guild") },
                notifier = notifier
        )
    }

    /**
     * Generic annotation-based command execution limitation.
     *
     * Behind the scenes, this function registers a:
     * - builder modifier that adds the `key` parameter to the command meta with the value `true`
     * - command post processor that checks whether the `key` meta is on a command, and if so then whether the `limitation`
     * function parameter returns true.
     *
     * If the `limitation` function returns true at the time of calling, then the command execution will be aborted
     * and the `notifier` function will be called to notify the sender of the limitation.
     *
     * @param C Command sender type
     * @param A Annotation type
     * @param parser The annotation parser
     * @param cmdManager The command manager
     * @param annotationClass The annotation class linked to this limitation
     * @param key Command meta key used for marking a command with this limitation
     * @param builderModifier Modifies commands with the specified annotation (provided by `annotationClass`). By default,
     * this parameter applies the `key` to the command meta with the value `true`. This is done to mark the command
     * that this limitation should apply to it.
     * @param limitation Checks whether a command execution should be blocked
     * @param notifier Notifier that gets called when a command is blocked by this limitation
     */
    fun <C, A : Annotation> registerCommandLimitation(
            parser: AnnotationParser<C>,
            cmdManager: CommandManager<C>,
            annotationClass: Class<A>,
            key: CommandMeta.Key<Boolean>,
            builderModifier: (A, Command.Builder<C>) -> Command.Builder<C> =
                    { _: A, builder: Command.Builder<C> ->
                        builder.meta(key, true)
                    },
            limitation: (CommandPostprocessingContext<C>) -> Boolean,
            notifier: (CommandPostprocessingContext<C>) -> Unit
    ) {
        parser.registerBuilderModifier(annotationClass, builderModifier)

        cmdManager.registerCommandPostProcessor {
            if (it.command.commandMeta.getOrDefault(key, false)
                    && limitation(it)) {
                notifier(it)
                ConsumerService.interrupt()
            }
        }
    }

    /**
     * Parse [DisplayName] annotations for arguments in class instance(s) and add the <argument hashcode = display name>
     * entries to a [CustomSyntaxFormatter].
     *
     * @param formatter The syntax formatter to store display name data
     * @param instances Instances of class(es) to parse
     */
    fun parseDisplayNames(formatter: CustomSyntaxFormatter<*>, instances: Array<Any>) {
        instances.flatMap { it.javaClass.declaredMethods.toList() }
                .filter { it.isAnnotationPresent(CommandMethod::class.java) }
                .flatMap { it.parameters.toList() }
                .filter {
                    it.isAnnotationPresent(Argument::class.java)
                            && it.isAnnotationPresent(DisplayName::class.java)
                }
                .forEach {
                    val argument = it.getAnnotation(Argument::class.java)
                    val displayName = it.getAnnotation(DisplayName::class.java)
                    val type = it.annotatedType
                    val argumentHash = Objects.hash(argument.value, it.type)
                    logger.debug("Hashed [${argument.value}] [${type.type.typeName}] -> $argumentHash")

                    formatter.displayNameMap[argumentHash] = displayName.name
                }
    }
}