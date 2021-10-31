package com.dongtronic.diabot.platforms.discord.commands.info

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import org.slf4j.LoggerFactory
import java.awt.Color

class AboutCommand(private val color: Color, private val description: String, private val features: Array<String>, vararg perms: Permission) : Command() {
    private val replacementIcon = "+"
    private val perms: Array<Permission>
    private var oauthLink: String? = null
    private val log = LoggerFactory.getLogger("OAuth2")

    override fun execute(event: CommandEvent) {
        if (oauthLink == null) {
            oauthLink = try {
                val info = event.jda.retrieveApplicationInfo().complete()
                if (info.isBotPublic) {
                    info.setRequiredScopes("applications.commands")
                    info.getInviteUrl(0L, *perms)
                } else ""
            } catch (e: Exception) {
                log.error("Could not generate invite link ", e)
                ""
            }
        }

        val builder = EmbedBuilder()
        builder.setColor(if (event.guild == null) color else event.guild.selfMember.color)
        builder.setAuthor("All about " + event.selfUser.name + "!", null, event.selfUser.avatarUrl)
        val join = !(event.client.serverInvite == null || event.client.serverInvite.isEmpty())
        val invite = oauthLink!!.isNotEmpty()

        val inviteMessage = if (join && invite) {
            "Join my server [`here`](${event.client.serverInvite}), or [`invite`](${oauthLink}) me to your server"
        } else if (join) {
            "Join my server [`here`](${event.client.serverInvite})"
        } else if (invite) {
            "Please [`invite`](${oauthLink}) me to your server"
        } else ""


        val author = if (event.jda.getUserById(event.client.ownerId) == null) "<@" + event.client.ownerId + ">" else event.jda.getUserById(event.client.ownerId)!!.name

        val description = StringBuilder()
                .append("Hello! I am **${event.selfUser.name}**, ")
                .append(description)
                .append("\nI was written in Kotlin by **$author**")
                .append("using ${JDAUtilitiesInfo.AUTHOR}'s [Commands Extension](${JDAUtilitiesInfo.GITHUB})")
                .append(" and the [JDA library](https://github.com/DV8FromTheWorld/JDA)")
                .append("\nType `${event.client.textualPrefix} ${event.client.helpWord}` to see my commands!")
                .append(inviteMessage)
                .append("\n\nSome of my features include: ```css")

        for (feature in features) {
            description.append("\n")
            if (event.client.success.startsWith("<")) {
                description.append(replacementIcon)
            } else {
                description.append(event.client.success)
            }
            description.append(" ").append(feature)
        }
        description.append(" ```")
        builder.setDescription(description)

        builder.addField("Stats", """${event.client.totalGuilds} Servers Shard ${event.jda.shardInfo.shardId + 1}/${event.jda.shardInfo.shardTotal}""", true)

        builder.addField("This shard", """${event.jda.users.size} Users ${event.jda.guilds.size} Servers""", true)
        builder.addField("", """${event.jda.textChannels.size} Text Channels ${event.jda.voiceChannels.size} Voice Channels""", true)
        builder.setFooter("Last restart", null)
        builder.setTimestamp(event.client.startTime)
        event.reply(builder.build())
    }

    init {
        name = "about"
        help = "shows info about the bot"
        guildOnly = false
        @Suppress("UNCHECKED_CAST")
        this.perms = perms as Array<Permission>
        botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS)
    }
}
