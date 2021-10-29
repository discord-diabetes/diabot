package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.SlashCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

class NightscoutSlashCommand : SlashCommand {

    private val groupNameSet = "set"
    private val groupNameClear = "clear"
    private val groupNameGet = "get"
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
                commandModeToken -> clearToken(event)
                commandModeUrl -> clearUrl(event)
                commandModeAll -> clearAll(event)
            }
            groupNameGet -> when (event.subcommandName) {
                commandModeUrl -> getUrl(event)
                commandModeToken -> getToken(event)
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
            return
        }

        val public = event.getOption(commandArgPublic)?.asBoolean ?: true
        val visibility = if (public) "public" else "private"

        NightscoutFacade.setPublic(event.user, event.guild!!, public).subscribe({
            event.reply("Your Nightscout data was made $visibility in this server").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout data to $visibility in this server. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun setPrivate(event: SlashCommandEvent) {
        if (!event.isFromGuild) {
            warnGuildOnly(event)
            return
        }

        NightscoutFacade.setPublic(event.user, event.guild!!, false).subscribe({
            event.reply("Your Nightscout data was made private in this server").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout data to private in this server. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun clearToken(event: SlashCommandEvent) {
        NightscoutFacade.clearToken(event.user).subscribe({
            event.reply("Your Nightscout token has been deleted").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while removing your Nightscout token. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun clearUrl(event: SlashCommandEvent) {
        NightscoutFacade.clearUrl(event.user).subscribe({
            event.reply("Your Nightscout URL has been deleted").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while removing your Nightscout URL. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun clearAll(event: SlashCommandEvent) {
        NightscoutFacade.clearAll(event.user).subscribe({
            event.reply("Your Nightscout data has been deleted").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while removing your Nightscout data. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun getUrl(event: SlashCommandEvent) {
        NightscoutFacade.getUser(event.user).subscribe {
            if (it.url != null) {
                event.reply("Your configured Nightscout URL is `${it.url}`").setEphemeral(true).queue()
            } else {
                event.reply("You do not have a configured Nightscout URL. Use `/nightscout set url` to configure it.").setEphemeral(true).queue()
            }
        }
    }

    private fun getToken(event: SlashCommandEvent) {
        NightscoutFacade.getUser(event.user).subscribe {
            if (it.token != null) {
                event.reply("Your configured Nightscout token is `${it.token}`").setEphemeral(true).queue()
            } else {
                event.reply("You do not have a configured Nightscout token. Use `/nightscout set token` to configure it.").setEphemeral(true).queue()
            }
        }
    }

    private fun warnGuildOnly(event: SlashCommandEvent) {
        event.reply("You must use this command in a server").setEphemeral(true).queue()
    }

    override fun config(): CommandData {
        return CommandData(commandName, "Manage your Nightscout settings").addSubcommandGroups(
                SubcommandGroupData(groupNameSet, "Set Nightscout settings").addSubcommands(
                        SubcommandData(commandModeUrl, "Set Nightscout url")
                                .addOption(OptionType.STRING, commandArgUrl, "URL of your Nightscout instance", true),
                        SubcommandData(commandModeToken, "Set Nightscout token")
                                .addOption(OptionType.STRING, commandArgToken, "The authentication token of your Nightscout instance", true),
                        SubcommandData(commandModePublic, "Set Nightscout to public in the current server")
                                .addOption(OptionType.BOOLEAN, commandArgPublic, "Whether to make your Nightscout data public. If false, it will be set to private."),
                        SubcommandData(commandModePrivate, "Set Nightscout to private in the current server")
                ),
                SubcommandGroupData(groupNameClear, "Clear Nightscout settings").addSubcommands(
                        SubcommandData(commandModeUrl, "Clear Nightscout url"),
                        SubcommandData(commandModeToken, "Clear Nightscout token"),
                        SubcommandData(commandModeAll, "Clear all Nightscout data")
                ),
                SubcommandGroupData(groupNameGet, "Get Nightscout settings (private)").addSubcommands(
                        SubcommandData(commandModeUrl, "Get Nightscout URL"),
                        SubcommandData(commandModeToken, "Get Nightscout token")
                )
        )
    }
}
