package com.dongtronic.diabot.platforms.discord.commands.nightscout

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import com.dongtronic.diabot.commands.Category
import com.dongtronic.diabot.commands.annotations.CommandCategory
import com.dongtronic.diabot.commands.annotations.NoAutoPermission
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.JDACommandUser
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder

class NightscoutDisplayCommands {
    private val logger = logger()

    @NoAutoPermission
    @CommandMethod("nightscout display list")
    @CommandDescription("Shows the display options available and the ones you have activated")
    @CommandCategory(Category.BG)
    fun listOptions(e: JDACommandUser) {
        val options = DisplayOptions.values().joinToString(
                prefix = "`",
                separator = "`, `",
                postfix = "`"
        ) { it.name.toLowerCase() }
        val message = "You can add the following options to your display preferences: $options"
        val builder = MessageBuilder()
        val embed = EmbedBuilder()
        embed.setTitle("Valid Display Options")
        // todo: clean up
        embed.addField("__`title`__", "Your Nightscout display title. [This can be changed on your Nightscout instance through the `CUSTOM_TITLE` environment variable](https://github.com/nightscout/cgm-remote-monitor#predefined-values-for-your-browser-settings-optional). You may also add custom emotes to your Nightscout title by embedding the emote's raw value in the `CUSTOM_TITLE` variable. The emote's raw value can be found by typing the emote in Discord and prefixing it with a backslash `\\`, like `\\:youremote:`.", false)
        embed.addField("__`trend`__", "The current BG trend arrow in Nightscout.", false)
        embed.addField("__`cob`__", "The amount of carbs on board. This value is reported by either `devicestatus` (OpenAPS / AndroidAPS) or Nightscout's careportal. The `cob` plugin must be enabled on the Nightscout instance for this to be displayed.", false)
        embed.addField("__`iob`__", "The amount of insulin on board. This value is reported by either `devicestatus` (OpenAPS / AndroidAPS) or Nightscout's careportal. The `iob` plugin must be enabled on the Nightscout instance for this to be displayed.", false)
        embed.addField("__`avatar`__", "Adds your Discord profile picture to the embed.", false)
        embed.addField("__`simple`__", "Removes your Discord profile picture from the embed. (idk why this is an option)", false)
        builder.setEmbed(embed.build())

        NightscoutDAO.instance.getUser(e.getAuthorUniqueId()).subscribe({
            val usersOptions = it.displayOptions.joinToString()
            builder.append("Your current display preferences are:").appendCodeBlock(usersOptions, "")
//            builder.append(message)
            e.replyS(builder.build())
        }, {
            if (it !is NoSuchElementException) {
                logger.warn("Could not retrieve Nightscout user", it)
                builder.append("Your display preferences were not able to be retrieved, but the list of possible display preferences you can add are: $options")
                e.replyS(builder.build())
            } else {
                builder.append("You currently do not have any display preferences set. The list of possible display preferences you can add are: $options")
                e.replyS(builder.build())
            }
        })
    }

    @NoAutoPermission
    @CommandMethod("nightscout display set <options>")
    @CommandDescription("Sets your display preferences")
    @CommandCategory(Category.BG)
    fun setOptions(e: JDACommandUser, @Argument("options") options: Array<String>) {
        val newOptions = options.map {
            DisplayOptions.valueOf(it.toUpperCase()).name.toLowerCase()
        }

        NightscoutDAO.instance.updateDisplay(
                e.getAuthorUniqueId(),
                null,
                *newOptions.toTypedArray()
        ).subscribe({ updatedOptions ->
            val formattedOptions = updatedOptions.joinToString(
                    prefix = "`",
                    separator = "`, `",
                    postfix = "`"
            )
            e.replySuccessS("Nightscout display options were set to $formattedOptions")
        }, {
            logger.warn("Could not change Nightscout display options", it)
            e.replyErrorS("An error occurred while setting Nightscout display options")
        })
    }

    @NoAutoPermission
    @CommandMethod("nightscout display add|a <options>")
    @CommandDescription("Adds display options to your preferences")
    @CommandCategory(Category.BG)
    fun addOptions(e: JDACommandUser, @Argument("options") options: Array<String>) {
        val newOptions = options.map {
            DisplayOptions.valueOf(it.toUpperCase()).name.toLowerCase()
        }

        NightscoutDAO.instance.updateDisplay(
                e.getAuthorUniqueId(),
                true,
                *newOptions.toTypedArray()
        ).subscribe({ updatedOptions ->
            val formattedOptions = updatedOptions.joinToString(
                    prefix = "`",
                    separator = "`, `",
                    postfix = "`"
            )
            e.replySuccessS("Nightscout display options were updated to $formattedOptions")
        }, {
            logger.warn("Could not change Nightscout display options", it)
            e.replyErrorS("An error occurred while updating Nightscout display options")
        })
    }

    @NoAutoPermission
    @CommandMethod("nightscout display remove|rem|del|d <options>")
    @CommandDescription("Removes display options from your preferences")
    @CommandCategory(Category.BG)
    fun removeOptions(e: JDACommandUser, @Argument("options") options: Array<String>) {
        val newOptions = options.map {
            DisplayOptions.valueOf(it.toUpperCase()).name.toLowerCase()
        }

        NightscoutDAO.instance.updateDisplay(
                e.getAuthorUniqueId(),
                false,
                *newOptions.toTypedArray()
        ).subscribe({ updatedOptions ->
            val formattedOptions = updatedOptions.joinToString(
                    prefix = "`",
                    separator = "`, `",
                    postfix = "`"
            )
            e.replySuccessS("Nightscout display options were updated to $formattedOptions")
        }, {
            logger.warn("Could not change Nightscout display options", it)
            e.replyErrorS("An error occurred while updating Nightscout display options")
        })
    }

    @NoAutoPermission
    @CommandMethod("nightscout display reset")
    @CommandDescription("Resets your display preferences to default")
    @CommandCategory(Category.BG)
    fun resetOptions(e: JDACommandUser) {
        NightscoutDAO.instance.updateDisplay(
                e.getAuthorUniqueId(),
                false
        ).subscribe({
            e.replySuccessS("Nightscout display options were reset")
        }, {
            logger.warn("Could not reset Nightscout display options", it)
            e.replyErrorS("An error occurred while resetting Nightscout display options")
        })
    }

    enum class DisplayOptions {
        // special option
        ALL,
        TITLE,
        TREND,
        COB,
        IOB,
        AVATAR,
        SIMPLE
    }
}