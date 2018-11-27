package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.data.NightscoutDTO
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.util.NicknameUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class NightscoutCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(NightscoutCommand::class.java)

    init {
        this.name = "nightscout"
        this.help = "Get the most recent info from any Nightscout site"
        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
        this.guildOnly = true
        this.aliases = arrayOf("ns", "bg")
        this.examples = arrayOf("diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com", "diabot ns public false")
        this.children = arrayOf(
                NightscoutSetCommand(category, this),
                NightscoutDeleteCommand(category, this),
                NightscoutPublicCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        try {
            if (args.isEmpty()) {
                getStoredData(event)
                return
            } else {
                getUnstoredData(event)
            }

        } catch (ex: UnconfiguredNightscoutException) {
            event.reply("Please set your Nightscout hostname using `diabot nightscout set <hostname>`")
        } catch (ex: IllegalArgumentException) {
            event.reply("Error: " + ex.message)
        } catch (ex: Exception) {
            event.reactError()
            logger.warn("Unexpected error: " + ex.message)
        }

    }

    private fun buildNightscoutResponse(endpoint: String, avatarUrl: String?, event: CommandEvent) {
        val dto = NightscoutDTO()

        getData(endpoint, dto, event)
        getRanges(endpoint, dto, event)
        processPebble(endpoint, dto, event)

        val builder = EmbedBuilder()

        buildResponse(dto, avatarUrl, builder)

        val embed = builder.build()

        event.reply(embed) {
            // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
            if (dto.glucose!!.mgdl == 69 || dto.glucose!!.mmol == 6.9) {
                it.addReaction("\uD83D\uDE0F").queue()
            }
            // #36: Reply with :100: when value is 100 mg/dL or 5.5 mmol/L
            if (dto.glucose!!.mgdl == 100 || dto.glucose!!.mmol == 5.5) {
                it.addReaction("\u1F4AF").queue()
            }
        }
    }

    private fun getStoredData(event: CommandEvent) {
        val endpoint = getNightscoutHost(event.author) + "/api/v1/"

        buildNightscoutResponse(endpoint, event.author.avatarUrl, event)
    }

    private fun getUnstoredData(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var avatarUrl: String? = null
        val endpoint = when {
            event.event.message.mentionedUsers.size == 1 -> {
                val user = event.event.message.mentionedMembers[0].user
                try {
                    if (!getNightscoutPublic(user)) {
                        event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is private")
                        return
                    }
                    getNightscoutHost(user) + "/api/v1/"
                } catch (ex: UnconfiguredNightscoutException) {
                    throw IllegalArgumentException("User does not have a configured Nightscout URL.")
                }
            }
            event.event.message.mentionedUsers.size > 1 -> throw IllegalArgumentException("Too many mentioned users.")
            event.event.message.mentionsEveryone() -> throw IllegalArgumentException("Cannot handle mentioning everyone.")
            else -> {
                val hostname = args[0]
                "https://$hostname.herokuapp.com/api/v1/"
            }
        }

        if(event.message.mentionedUsers.size == 1 && !event.message.mentionsEveryone()) {
            avatarUrl = event.message.mentionedUsers[0].avatarUrl
        }

        buildNightscoutResponse(endpoint, avatarUrl, event)
    }

    private fun processPebble(url: String, dto: NightscoutDTO, event: CommandEvent) {
        val client = HttpClient()
        val urlBase = url.replace("/api/v1/", "/pebble")
        val method = GetMethod(urlBase)
        val statusCode = client.executeMethod(method)

        if (statusCode == -1) {
            event.reactError()
            logger.warn("Got -1 Status code attempting to get $urlBase")
        }

        val bgsJson: JsonObject
        val json = method.responseBodyAsStream.bufferedReader().use { it.readText() }
        if (JsonParser().parse(json).asJsonObject.has("bgs"))
            bgsJson = JsonParser().parse(json).asJsonObject.get("bgs").asJsonArray.get(0).asJsonObject
        else {
            logger.warn("Failed to get bgs Object from pebbleEndpoint JSON:\n$json")
            return
        }

        if (bgsJson.has("cob")) {
            dto.cob = bgsJson.get("cob").asInt
        }
        if (bgsJson.has("iob")) {
            dto.iob = bgsJson.get("iob").asFloat
        }
        val bgDelta = bgsJson.get("bgdelta").asString
        if (dto.delta == null) {
            dto.deltaIsNegative = bgDelta.contains("-")
            dto.delta = BloodGlucoseConverter.convert(bgDelta.replace("-".toRegex(), ""), dto.units)
        }
    }

    private fun buildResponse(dto: NightscoutDTO, avatarUrl: String?, builder: EmbedBuilder) {
        builder.setTitle("Nightscout")

        val mmolString: String
        val mgdlString: String
        if (dto.delta != null) {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), dto.delta!!.mmol.toString(), dto.deltaIsNegative)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), dto.delta!!.mgdl.toString(), dto.deltaIsNegative)
        } else {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), "999.0", false)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), "999.0", false)
        }
        val trendArrows: Array<String> = arrayOf("", "↟", "↑", "↗", "→", "↘", "↓", "↡", "↮", "↺")
        val trendString = trendArrows[dto.trend]
        builder.addField("mmol/L", mmolString, true)
        builder.addField("mg/dL", mgdlString, true)
        builder.addField("trend", trendString, true)
        if (dto.iob != 0.0F) {
            builder.addField("iob", dto.iob.toString(), true)
        }
        if (dto.cob != 0) {
            builder.addField("cob", dto.cob.toString(), true)
        }

        setResponseColor(dto, builder)

        if(avatarUrl != null) {
            builder.setThumbnail(avatarUrl)
        }

        builder.setTimestamp(dto.dateTime)
        builder.setFooter("measured", "https://github.com/nightscout/cgm-remote-monitor/raw/master/static/images/large.png")

        if (dto.dateTime!!.plusMinutes(15).toInstant().isBefore(ZonedDateTime.now().toInstant())) {
            builder.setDescription("**BG data is more than 15 minutes old**")
        }


    }

    private fun buildGlucoseString(glucose: String, delta: String, negative: Boolean): String {
        val builder = StringBuilder()

        builder.append(glucose)

        if (delta != "999.0") {
            // 999L is placeholder for absent delta
            builder.append(" (")

            if (negative) {
                builder.append("-")
            } else {
                builder.append("+")
            }

            builder.append(delta)
            builder.append(")")
        }

        return builder.toString()
    }

    private fun setResponseColor(dto: NightscoutDTO, builder: EmbedBuilder) {
        val glucose = dto.glucose!!.mgdl.toDouble()

        if (glucose >= dto.high || glucose <= dto.low) {
            builder.setColor(Color.red)
        } else if (glucose >= dto.top && glucose < dto.high || glucose > dto.low && glucose <= dto.bottom) {
            builder.setColor(Color.orange)
        } else {
            builder.setColor(Color.green)
        }
    }


    private fun getNightscoutHost(user: User): String {
        val url = NightscoutDAO.getInstance().getNightscoutUrl(user)

        if (url == null) {
            throw UnconfiguredNightscoutException()
        } else {
            return url
        }
    }

    private fun getNightscoutPublic(user: User): Boolean {
        return NightscoutDAO.getInstance().isNightscoutPublic(user)
    }

    @Throws(IOException::class)
    private fun getRanges(url: String, dto: NightscoutDTO, event: CommandEvent) {
        val client = HttpClient()
        val method = GetMethod("$url/status.json")

        val statusCode = client.executeMethod(method)

        if (statusCode == -1) {
            event.reactError()
        }

        val json = method.responseBodyAsString

        val jsonObject = JsonParser().parse(json).asJsonObject
        val units = jsonObject.get("settings").asJsonObject.get("units").asString
        val ranges = jsonObject.get("settings").asJsonObject.get("thresholds").asJsonObject
        val low = ranges.get("bgLow").asInt
        val bottom = ranges.get("bgTargetBottom").asInt
        val top = ranges.get("bgTargetTop").asInt
        val high = ranges.get("bgHigh").asInt

        dto.low = low
        dto.bottom = bottom
        dto.top = top
        dto.high = high
        dto.units = units
    }


    @Throws(IOException::class, UnknownUnitException::class)
    private fun getData(url: String, dto: NightscoutDTO, event: CommandEvent) {
        val endpoint = "$url/entries.json"
        val client = HttpClient()
        val method = GetMethod(endpoint)

        method.queryString = "count=1"

        val statusCode = client.executeMethod(method)

        if (statusCode == -1) {
            logger.warn("Got -1 Status code attempting to get $endpoint")
            event.reactError()
        }

        val json = method.responseBodyAsString

        val jsonObject = JsonParser().parse(json).asJsonArray.get(0).asJsonObject
        val sgv = jsonObject.get("sgv").asString
        val timestamp = jsonObject.get("date").asLong
        var trend = 0
        val direction: String
        if (jsonObject.has("trend")) {
            trend = jsonObject.get("trend").asInt
        } else if (jsonObject.has("direction")) {
            direction = jsonObject.get("direction").asString
            trend = when (direction.toUpperCase()) {
                "NONE" -> 0
                "DOUBLEUP" -> 1
                "SINGLEUP" -> 2
                "FORTYFIVEUP" -> 3
                "FLAT" -> 4
                "FORTYFIVEDOWN" -> 5
                "SINGLEDOWN" -> 6
                "DOUBLEDOWN" -> 7
                "NOT COMPUTABLE" -> 8
                "RATE OUT OF RANGE" -> 9
                else -> {
                    throw IllegalArgumentException("Unknown direction $direction")
                }
            }
        }

        var delta = ""
        if (jsonObject.has("delta")) {
            delta = jsonObject.get("delta").asString
        }
        val dateTime = getTimestamp(timestamp)

        val convertedBg = BloodGlucoseConverter.convert(sgv, "mg")

        if (delta.isNotEmpty()) {
            val convertedDelta = BloodGlucoseConverter.convert(delta.replace("-".toRegex(), ""), "mg")
            dto.delta = convertedDelta
        }

        dto.glucose = convertedBg
        dto.deltaIsNegative = delta.contains("-")
        dto.dateTime = dateTime
        dto.trend = trend
    }

    private fun getTimestamp(epoch: Long?): ZonedDateTime {

        val i = Instant.ofEpochSecond(epoch!! / 1000)
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)

    }
}
