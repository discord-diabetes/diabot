package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.SlashCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

class NightscoutGlobalSlashCommand : SlashCommand {

    private val groupNameSet = "set"
    private val groupNameClear = "clear"
    private val commandModeUrl = "url"
    private val commandModeToken = "token"
    private val commandModeAll = "all"
    private val commandModePublic = "public"
    private val commandModePrivate = "private"
    private val commandArgUrl = "url"
    private val commandArgToken = "token"
    private val commandArgPublic = "public"

    override val commandName: String = "nightscout"

    override fun execute(event: SlashCommandEvent) {
        when (event.subcommandGroup) {
            groupNameSet -> when (event.subcommandName) {
                commandModeToken -> setToken(event)
                commandModeUrl -> setUrl(event)
                commandModePublic -> setPublic(event)
                commandModePrivate -> setPrivate(event)
            }
            groupNameClear -> when (event.subcommandName) {
                commandModeToken -> NightscoutFacade.clearToken(event.user)
                commandModeUrl -> NightscoutFacade.clearUrl(event.user)
                commandModeAll -> NightscoutFacade.clearAll(event.user)
            }
        }
    }

    private fun setToken(event: SlashCommandEvent) {
        NightscoutFacade.setToken(event.user, event.getOption(commandArgToken)!!.asString).subscribe({
            event.reply("Your Nightscout token was set").setEphemeral(true).queue()
        }, {
            event.reply("There was an error setting your Nightscout token, please try again later.").setEphemeral(true).queue()
        })
    }

    private fun setUrl(event: SlashCommandEvent) {
        val url = event.getOption(commandArgUrl)!!.asString
        NightscoutFacade.setUrl(event.user, url).subscribe({
            event.reply("Your Nightscout URL was set to $url").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout URL. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun setPublic(event: SlashCommandEvent) {
        if (!event.isFromGuild) {
            warnGuildOnly(event)
        }

        val public = event.getOption(commandArgPublic)?.asBoolean ?: true
        val visibility = if (public) "public" else "private"

        NightscoutFacade.setPublic(event.user, event.guild!!, public).subscribe({
            event.reply("Your nightscout data was made $visibility in this server").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout data to $visibility in this server. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun setPrivate(event: SlashCommandEvent) {
        if (!event.isFromGuild) {
            warnGuildOnly(event)
        }

        NightscoutFacade.setPublic(event.user, event.guild!!, false).subscribe({
            event.reply("Your nightscout data was made private in this server").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout data to private in this server. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun warnGuildOnly(event: SlashCommandEvent) {
        event.reply("You must use this command in a server").setEphemeral(true).queue()
    }

    override fun config(): CommandData {
        return CommandData(commandName, "Manage your nightscout settings").addSubcommandGroups(
                SubcommandGroupData(groupNameSet, "Set nightscout settings").addSubcommands(
                        SubcommandData(commandModeUrl, "Set nightscout url")
                                .addOption(OptionType.STRING, commandArgUrl, "URL of your nightscout instance", true),
                        SubcommandData(commandModeToken, "Set nightscout token")
                                .addOption(OptionType.STRING, commandArgToken, "The authentication token of your nightscout instance", true),
                        SubcommandData(commandModePublic, "Set nightscout to public in the current server")
                                .addOption(OptionType.BOOLEAN, commandArgPublic, "Whether to make your nightscout data public. If false, it will be set to private."),
                        SubcommandData(commandModePrivate, "Set nightscout to private in the current server")
                ),
                SubcommandGroupData(groupNameClear, "Clear nightscout settings").addSubcommands(
                        SubcommandData(commandModeUrl, "Clear nightscout url"),
                        SubcommandData(commandModeToken, "Clear nightscout token"),
                        SubcommandData(commandModeAll, "Clear all nightscout data")
                )
        )
    }
}
