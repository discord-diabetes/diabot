package com.dongtronic.diabot.logic.`fun`

import com.dongtronic.diabot.exceptions.NoSuchEpisodeException
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

object Diacast {
    val episodes: List<SyndEntry>
        @Throws(FeedException::class, IOException::class)
        get() {
            val feedSource = URL("https://diacast.cascer1.space/podcast.rss")
            val input = SyndFeedInput()
            val feed = input.build(XmlReader(feedSource))
            return feed.entries
        }

    @Throws(NoSuchEpisodeException::class, IOException::class, FeedException::class)
    fun getEpisode(episode: Int): SyndEntry {
        try {
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
        } catch (e: FileNotFoundException) {
            throw NoSuchEpisodeException()
        }

        throw NoSuchEpisodeException()
    }
}
