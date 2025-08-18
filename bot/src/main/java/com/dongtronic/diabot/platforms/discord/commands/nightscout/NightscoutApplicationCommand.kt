package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.graph.GraphTheme
import com.dongtronic.diabot.graph.PlottingStyle
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import com.github.kaktushose.jda.commands.annotations.constraints.Max
import com.github.kaktushose.jda.commands.annotations.constraints.Min
import com.github.kaktushose.jda.commands.annotations.interactions.*
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@Interaction("nightscout")
class NightscoutApplicationCommand : ApplicationCommand {
    private val logger = logger()

    @Command("set token", desc = "Set Nightscout token")
    fun setToken(event: CommandEvent, @Param("Authentication token from your Nightscout instance") token: String) {
        NightscoutFacade.setToken(event.user, token).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout token was set")
        }, {
            replyError(event, it, "There was an error setting your Nightscout token, please try again later.")
        })
    }

    @Command("set url", desc = "Set Nightscout URL")
    fun setUrl(event: CommandEvent, @Param("URL of your Nightscout instance") url: String) {
        NightscoutFacade.setUrl(event.user, url).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout URL was set to $url")
        }, {
            replyError(event, it, "There was an error while setting your Nightscout URL. Please try again later.")
        })
    }

    @CommandConfig(scope = CommandScope.GUILD)
    @Command("set privacy", desc = "Set Nightscout privacy setting in this server")
    fun setPrivacy(
        event: CommandEvent,
        @Param("Privacy setting")
        @Choices("private", "public")
        privacy: String
    ) {
        val public = privacy == "public"
        NightscoutFacade.setPublic(event.user, event.guild!!, public).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout data was made $privacy in this server")
        }, {
            replyError(event, it, "There was an error while setting your Nightscout data to $privacy in this server. Please try again later.")
        })
    }

    @Command("set globalprivacy", desc = "Set Nightscout privacy setting in all servers")
    fun setGlobalPrivacy(
        event: CommandEvent,
        @Param("Privacy setting")
        @Choices("private", "public")
        privacy: String
    ) {
        val public = privacy == "public"

        if (public) {
            event.with().ephemeral(true).reply("You must set your Nightscout data to public on a per-server basis.")
        }

        NightscoutFacade.setGlobalPublic(event.user, public).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout data has been set to $privacy in all servers")
        }, {
            replyError(event, it, "There was an error setting your global Nightscout privacy setting. Please try again later")
        })
    }

    @Command("set graphmode", desc = "Set the plotting style for Nightscout graphs")
    fun setGraphMode(
        event: CommandEvent,
        @Param("Plotting style")
        @Choices("scatter", "line")
        mode: String
    ) {
        val plottingStyle = PlottingStyle.entries.first { it.name.startsWith(mode, true) }

        NightscoutDAO.instance.getUser(event.user.id)
            .map { it.graphSettings }
            .map { it.copy(plotMode = plottingStyle) }
            .flatMap { NightscoutDAO.instance.updateGraphSettings(event.user.id, it) }
            .subscribe({
                event.with().ephemeral(true).reply("Plotting style changed to `${it.plotMode.name}`")
            }, {
                replyError(event, it, "Could not update plotting style: ${it.javaClass.simpleName}")
                logger.warn("Unexpected error when changing graph mode for ${event.user}", it)
            })
    }

    @Command("set graphhours", desc = "Set the number of hours displayed in Nightscout graphs")
    fun setGraphHours(
        event: CommandEvent,
        @Param("The number of hours to display in graphs")
        @Max(24)
        @Min(1)
        hours: Long
    ) {
        if (hours !in 1..24) {
            event.with().ephemeral(true).reply("The number of hours must be between 1 and 24")
            return
        }

        NightscoutDAO.instance.getUser(event.user.id)
            .map { it.graphSettings.copy(hours = hours) }
            .flatMap { NightscoutDAO.instance.updateGraphSettings(event.user.id, it) }
            .subscribe({
                val plural = if (it.hours != 1L) "s" else ""
                event.with().ephemeral(true).reply("Your future graphs will now display ${it.hours} hour$plural of data")
            }, {
                replyError(event, it, "Could not change the graph hours: ${it.javaClass.simpleName}")
                logger.warn("Unexpected error when changing graph hours for ${event.user}", it)
            })
    }

    @Command("set graphanimals", desc = "Toggle the April Fools' joke in Nightscout graphs")
    fun setGraphAnimals(event: CommandEvent, @Param(optional = true) enabled: Boolean?) {
        NightscoutDAO.instance.getUser(event.user.id)
            .map {
                val newState = enabled ?: (it.graphSettings.theme != GraphTheme.ANIMALS)
                val theme = if (newState) GraphTheme.ANIMALS else GraphTheme.DARK
                it.graphSettings.copy(theme = theme)
            }
            .flatMap { NightscoutDAO.instance.updateGraphSettings(event.user.id, it) }
            .subscribe({
                val msg = if (it.theme == GraphTheme.ANIMALS) {
                    "Congratulations, you've successfully unlocked the purr-fect 'Furry Friends' mode! From now on, your graphs will " +
                        "have an extra dose of cuteness. It's like having a virtual pet on your screen. We hope you're feline good " +
                        "about your decision! Meow!"
                } else {
                    "Oh no, you've decided to paws our 'Furry Friends' mode. We'll miss those cute little noses and wagging tails in " +
                        "your graphs, but we understand that not everyone can handle so much adorableness at once. Thanks for " +
                        "playing along, and happy graphing without our furry companions!"
                }

                event.with().ephemeral(true).reply(msg)
            }, {
                replyError(event, it, "Oops! It looks like there's been an error (`${it.javaClass.simpleName}`) while trying to " +
                    "change the 'Furry Friends' mode. Maybe the cats and dogs got too excited? Report this issue to Diabot's GitHub " +
                    "and we'll get this fixed faster than you can say 'pawsome'. Thanks for your patience and " +
                    "happy graphing without any technical ruff-les!")
                logger.warn("Unexpected error when changing graph theme for ${event.user}", it)
            })
    }

    @Command("clear token", desc = "Clear Nightscout token")
    fun clearToken(event: CommandEvent) {
        NightscoutFacade.clearToken(event.user).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout token has been deleted")
        }, {
            replyError(event, it, "There was an error deleting your Nightscout token")
        })
    }

    @Command("clear url", desc = "Clear Nightscout URL")
    fun clearUrl(event: CommandEvent) {
        NightscoutFacade.clearUrl(event.user).subscribe({
            event.with().ephemeral(true).reply("Your Nightscout URL has been deleted")
        }, {
            replyError(event, it, "There was an error while removing your Nightscout URL. Please try again later.")
        })
    }

    @Command("clear all", desc = "Clear all Nightscout data")
    fun clearAllData(event: CommandEvent) {
        event.with()
            .ephemeral(true)
            .components("confirmClearAll", "cancelClearAll")
            .reply("Are you sure you wish to **delete** your Nightscout settings?\n**This will remove all your Nightscout settings**")
    }

    @Button(value = "Yes, delete all settings", style = ButtonStyle.DANGER)
    fun confirmClearAll(event: ComponentEvent) {
        NightscoutFacade.clearAll(event.user).subscribe({
            event.with().keepComponents(false).reply("Your Nightscout settings have been deleted")
        }, {
            replyError(event, it, "There was an error while removing your Nightscout settings. Please try again later.")
        })
    }

    @Button(value = "Cancel", style = ButtonStyle.SECONDARY)
    fun cancelClearAll(event: ComponentEvent) {
        event.with().keepComponents(false).reply("Your Nightscout settings were **not** deleted.")
    }

    @Command("get url", desc = "Get your Nightscout URL")
    fun getUrl(event: CommandEvent) {
        val missingDataMessage = "You have not configured a Nightscout URL. Use `/nightscout set url` to configure it."
        val errorMessage = "There was an error while getting your Nightscout URL. Please try again later."
        NightscoutFacade.getUser(event.user).subscribe({
            if (it.url != null) {
                event.with().ephemeral(true).reply("Your configured Nightscout URL is `${it.url}`")
            } else {
                event.with().ephemeral(true).reply(missingDataMessage)
            }
        }, {
            if (it is NoSuchElementException) {
                event.with().ephemeral(true).reply(missingDataMessage)
            } else {
                replyError(event, it, errorMessage)
            }
        })
    }

    @Command("get token", desc = "Get your Nightscout token")
    fun getToken(event: CommandEvent) {
        val missingDataMessage = "You have not configured a Nightscout token. Use `/nightscout set token` to configure it."
        val errorMessage = "There was an error while getting your Nightscout token. Please try again later."
        NightscoutFacade.getUser(event.user).subscribe({
            if (it.token != null) {
                event.with().ephemeral(true).reply("Your configured Nightscout token is `${it.token}`")
            } else {
                event.with().ephemeral(true).reply(missingDataMessage)
            }
        }, {
            if (it is NoSuchElementException) {
                event.with().ephemeral(true).reply(missingDataMessage)
            } else {
                replyError(event, it, errorMessage)
            }
        })
    }
}
