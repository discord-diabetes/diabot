package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.components.Button

class NightscoutApplicationCommand : ApplicationCommand {
    private val logger = logger()
    private val groupNameSet = "set"
    private val groupNameClear = "clear"
    private val groupNameGet = "get"
    private val groupNameDelete = "delete"
    private val commandModeUrl = "url"
    private val commandModeToken = "token"
    private val commandModeAll = "all"
    private val commandModePrivacy = "privacy"
    private val commandModeGlobalPrivacy = "globalprivacy"
    private val commandArgUrl = "url"
    private val commandArgToken = "token"
    private val commandArgPrivacy = "privacy"
    private val commandArgPublic = "public"
    private val commandArgPrivate = "private"
    private val commandButtonDeleteConfirm = "nsdeleteyes"
    private val commandButtonDeleteCancel = "nsdeleteno"

    override val commandName: String = "nightscout"

    override val buttonIds: Set<String> = setOf(commandButtonDeleteConfirm, commandButtonDeleteCancel)

    override fun execute(event: SlashCommandEvent) {
        when (event.subcommandGroup) {
            groupNameSet -> when (event.subcommandName) {
                commandModeToken -> setToken(event)
                commandModeUrl -> setUrl(event)
                commandModePrivacy -> setPrivacy(event)
                commandModeGlobalPrivacy -> setGlobalPrivacy(event)
            }
            groupNameClear -> when (event.subcommandName) {
                commandModeToken -> clearToken(event)
                commandModeUrl -> clearUrl(event)
                commandModeAll -> confirmDeleteData(event)
            }
            groupNameGet -> when (event.subcommandName) {
                commandModeUrl -> getUrl(event)
                commandModeToken -> getToken(event)
            }
            groupNameDelete -> confirmDeleteData(event)
        }
    }

    override fun execute(event: ButtonClickEvent) {
        when (event.componentId) {
            commandButtonDeleteConfirm -> deleteData(event)
            commandButtonDeleteCancel -> cancelDeleteData(event)
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

    private fun setPrivacy(event: SlashCommandEvent) {
        if (!event.isFromGuild) {
            warnGuildOnly(event)
            return
        }

        val privacy = event.getOption(commandArgPrivacy)!!.asString

        val public = commandArgPublic == privacy
        val visibility = if (public) "public" else "private"

        NightscoutFacade.setPublic(event.user, event.guild!!, public).subscribe({
            event.reply("Your Nightscout data was made $visibility in this server").setEphemeral(true).queue()
        }, {
            event.reply("There was an error while setting your Nightscout data to $visibility in this server. Please try again later.").setEphemeral(true).queue()
        })
    }

    private fun setGlobalPrivacy(event: SlashCommandEvent) {
        val privacy = event.getOption(commandArgPrivacy)!!.asString

        val public = commandArgPublic == privacy
        val visibility = if (public) "public" else "private"

        if (public) {
            event.reply("You must set your Nightscout data to public on a per-server basis.").setEphemeral(true).queue()
        }

        NightscoutFacade.setGlobalPublic(event.user, public).subscribe({
            event.reply("Your Nightscout data has been set to $visibility in all servers").setEphemeral(true).queue()
        }, {
            event.reply("There was an error setting your global Nightscout privacy setting. Please try again later").setEphemeral(true).queue()
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

    private fun confirmDeleteData(event: SlashCommandEvent) {
        event.reply("Are you sure you wish to **delete** your Nightscout data?\n**This will remove all your Nightscout settings**")
                .addActionRow(
                        Button.danger(commandButtonDeleteConfirm, "Yes, delete all settings"),
                        Button.secondary(commandButtonDeleteCancel, "Cancel")
                ).setEphemeral(true).queue()
    }

    private fun deleteData(event: ButtonClickEvent) {
        NightscoutFacade.clearAll(event.user).subscribe({
            event.editMessage("Your Nightscout settings have been deleted").queue()
        }, {
            event.editMessage("There was an error while removing your Nightscout settings. Please try again later.").queue()
            logger.error("Error while deleting Nightscout data", it)
        })
    }

    private fun cancelDeleteData(event: ButtonClickEvent) {
        event.editMessage("Your Nightscout settings were **not** deleted.").queue()
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
                        SubcommandData(commandModePrivacy, "Set Nightscout privacy setting in this server")
                                .addOptions(OptionData(OptionType.STRING, commandArgPrivacy, "Privacy setting", true)
                                        .addChoice(commandArgPrivate, commandArgPrivate)
                                        .addChoice(commandArgPublic, commandArgPublic)),
                        SubcommandData(commandModeGlobalPrivacy, "Set Nightscout privacy setting in all servers")
                                .addOptions(OptionData(OptionType.STRING, commandArgPrivacy, "Privacy setting", true)
                                        .addChoice(commandArgPrivate, commandArgPrivate))
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
