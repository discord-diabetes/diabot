package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.exceptions.NoSuchEpisodeException
import com.dongtronic.diabot.logic.`fun`.Diacast
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.rometools.rome.feed.synd.SyndEntry
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils

class DiacastCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val episodes = Diacast.episodes

    init {
        this.name = "diacast"
        this.help = "Get information about a diacast episode"
        this.guildOnly = false
        this.examples = arrayOf("diabot diacast", "diabot diacast 6")
    }

    override fun execute(event: CommandEvent) {
        var episodeNumber = 0
        try {
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (args.isNotEmpty() && StringUtils.isNumeric(args[0])) {
                episodeNumber = Integer.valueOf(args[0])
            }

            val episode = Diacast.getEpisode(episodeNumber)

            val builder = EmbedBuilder()

            buildEpisodeCard(episode, builder)

            event.reply(builder.build())

        } catch (ex: NoSuchEpisodeException) {
            event.replyError("Episode $episodeNumber does not exist")
        }
        catch (ex: Exception) {
            event.replyError("Something went wrong: " + ex.message)
        }

    }

    private fun buildEpisodeCard(episode: SyndEntry, builder: EmbedBuilder) {
        builder.setTitle(episode.title, episode.link)
        builder.setAuthor("Diacast")

        for (element in episode.foreignMarkup) {
            if (element.name == "summary") {
                builder.setDescription(element.value)
            }

            if (element.name == "image") {
                val imageUrl = element.getAttributeValue("href")
                builder.setThumbnail(imageUrl)
            }

        }
    }


}
