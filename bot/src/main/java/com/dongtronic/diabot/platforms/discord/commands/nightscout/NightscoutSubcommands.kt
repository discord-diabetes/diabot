package com.dongtronic.diabot.platforms.discord.commands.nightscout

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.Flag
import com.dongtronic.diabot.commands.Category
import com.dongtronic.diabot.commands.ReplyType
import com.dongtronic.diabot.commands.annotations.CommandCategory
import com.dongtronic.diabot.commands.annotations.Example
import com.dongtronic.diabot.commands.annotations.GuildOnly
import com.dongtronic.diabot.commands.annotations.NoAutoPermission
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.platforms.discord.JDACommandUser
import com.dongtronic.diabot.util.logger
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import kotlin.reflect.KProperty

class NightscoutSubcommands {
    private val logger = logger()

    @NoAutoPermission
    @CommandMethod("nightscout delete|del|d")
    @CommandDescription("Deletes your Nightscout data from Diabot")
    @CommandCategory(Category.BG)
    @Example(["[delete]", "[del] --url --token", "[del] -u -t -d"])
    fun delete(
            e: JDACommandUser,
            @Flag("url", aliases = ["u"])
            url: Boolean,
            @Flag("token", aliases = ["t"])
            token: Boolean,
            @Flag("displayOptions", aliases = ["d"])
            displayOptions: Boolean
    ) {
        val toDelete = mutableListOf<KProperty<*>>()

        if (url) toDelete.add(NightscoutUserDTO::url)
        if (token) toDelete.add(NightscoutUserDTO::token)
        if (displayOptions) toDelete.add(NightscoutUserDTO::displayOptions)

        val humanString = toDelete.joinToString(
                prefix = "`",
                postfix = "`",
                separator = "`, `",
                transform = { it.name }
        )

        NightscoutDAO.instance.deleteUser(e.getAuthorUniqueId(), *toDelete.toTypedArray()).subscribe({
            if (it is UpdateResult) {
                e.replySuccessS("Removed the following Nightscout data: $humanString")
            } else if (it is DeleteResult) {
                e.replySuccessS("Removed all Nightscout data")
            }
        }, {
            logger.warn("Could not delete NS data", it)
            // todo: error
            e.replyErrorS("An error occurred while deleting data")
        })
    }

    @CommandMethod("nightscout token|t [token]")
    @CommandDescription("Sets a token to authenticate with Nightscout")
    @CommandCategory(Category.BG)
    fun setToken(e: JDACommandUser, @Argument("token") token: String?) {
        val replyType = if (token == null) ReplyType.NATIVE_REPLY else ReplyType.MENTION
        if (token != null) {
            deleteMessage(e)
        }

        NightscoutDAO.instance.setToken(e.getAuthorUniqueId(), token).subscribe({
            if (token != null) {
                e.replySuccessS("Set Nightscout token", replyType)
            } else {
                e.replySuccessS("Deleted Nightscout token", replyType)
            }
        }, {
            logger.warn("Could not set Nightscout token", it)
            e.replyErrorS("An error occurred while setting Nightscout token", replyType)
        })
    }

    @CommandMethod("nightscout set|seturl|url|s <url>")
    @CommandDescription("Sets your Nightscout URL")
    @CommandCategory(Category.BG)
    fun setUrl(e: JDACommandUser, @Argument("url") url: String) {
        deleteMessage(e)

        NightscoutDAO.instance.setUrl(e.getAuthorUniqueId(), url).subscribe({
            e.replySuccessS("Set Nightscout URL", ReplyType.MENTION)
        }, {
            logger.warn("Could not set NS URL", it)
            e.replyErrorS("An error occurred while setting Nightscout URL", ReplyType.MENTION)
        })
    }

    @NoAutoPermission
    @GuildOnly
    @CommandMethod("nightscout public|pub|p [visibility]")
    @CommandDescription("Sets your Nightscout data as public or private in a guild")
    @CommandCategory(Category.BG)
    fun setPublic(e: JDACommandUser, @Argument("visibility") visibility: String?) {
        val guild = e.event.guild
        val enables = arrayOf("T", "TRUE", "Y", "YES", "PUBLIC", "ON")
        val isPublic: Boolean? = when {
            visibility.isNullOrBlank() -> null
            enables.any { visibility.equals(it, true) } -> true
            else -> false
        }

        NightscoutDAO.instance.changePrivacy(
                e.getAuthorUniqueId(),
                guild.id,
                isPublic
        ).subscribe({ newPrivacy ->
            val formattedPrivacy = if (newPrivacy) "public" else "private"
            val switched = if (isPublic == null) "toggled" else "set"
            e.replySuccessS("Nightscout privacy was $switched to **$formattedPrivacy** in **${guild.name}**")
        }, {
            logger.warn("Could not change Nightscout privacy", it)
            e.replyErrorS("An error occurred while setting Nightscout privacy in **${guild.name}**")
        })
    }

    private fun deleteMessage(event: JDACommandUser) {
        // there is no need to delete the author's message in a DM
        if (event.event.channelType == ChannelType.PRIVATE) {
            return
        }

        event.deleteAuthorMessage("privacy").subscribe(null, {
            when (it) {
                is InsufficientPermissionException -> {
                    logger.info("Could not remove command message due to missing permission: ${it.permission}")
                    event.replyWarningS("Could not remove command message due to missing `${it.permission}` " +
                            "permission. Please remove the message yourself to protect your privacy.")
                }
                is IllegalStateException -> {
                    logger.info("Could not delete command message. Probably in a DM")
                }
                else -> {
                    logger.warn("Uncaught exception when deleting command message", it)
                }
            }
        })
    }
}