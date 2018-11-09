package com.dongtronic.diabot.commands.`fun`

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.exceptions.NoSuchEpisodeException
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils

import java.io.IOException
import java.net.URL

class DiacastCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val episodes: List<SyndEntry>
        @Throws(FeedException::class, IOException::class)
        get() {
            val feedSource = URL("https://diacast.xyz/?format=rss")
            val input = SyndFeedInput()
            val feed = input.build(XmlReader(feedSource))
            return feed.entries
        }

    init {
        this.name = "diacast"
        this.help = "Get information about a diacast episode"
        this.guildOnly = true
        this.examples = arrayOf("diabot diacast", "diabot diacast 6")
    }

    override fun execute(event: CommandEvent) {
        var episodeNumber = 0
        try {
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (args.isNotEmpty() && StringUtils.isNumeric(args[0])) {
                episodeNumber = Integer.valueOf(args[0])
            }

            val episode = getEpisode(episodeNumber)

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

    @Throws(NoSuchEpisodeException::class, IOException::class, FeedException::class)
    private fun getEpisode(episode: Int): SyndEntry {
        val episodes = episodes

        if (episode == 0) {
            return episodes[0]
        }


        for (entry in episodes) {
            for (element in entry.foreignMarkup) {
                if (element.name == "episode") {
                    val number = element.value
                    if (Integer.valueOf(number) == episode) {
                        return entry
                    }
                }
            }
        }

        throw NoSuchEpisodeException()
    }


}
