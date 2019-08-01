package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.data.NightscoutDTO
import com.dongtronic.diabot.exceptions.NightscoutStatusException
import com.dongtronic.diabot.exceptions.NoNightscoutDataException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.util.NicknameUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
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
                NightscoutSetUrlCommand(category, this),
                NightscoutDeleteCommand(category, this),
                NightscoutPublicCommand(category, this),
                NightscoutSetTokenCommand(category, this),
                NightscoutSetDisplayCommand(category, this)
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

    private fun buildNightscoutResponse(endpoint: String, token: String?, displayOpts: String, avatarUrl: String?, event: CommandEvent) {
        val dto = NightscoutDTO()

        try {
            getSettings(endpoint, token, dto)
            getEntries(endpoint, token, dto)
            processPebble(endpoint, token, dto)
        } catch (exception: NoNightscoutDataException) {
            event.reactError()
            logger.info("No nightscout data from $endpoint")
            return
        } catch (exception: MalformedJsonException) {
            event.reactError()
            logger.warn("Malformed JSON from $endpoint")
            return
        } catch (exception: NightscoutStatusException) {
            if (exception.status == 401) {
                event.replyError("Could not authenticate to Nightscout. Please set an authentication token with `diabot nightscout token <token>`")
            } else {
                event.reactError()
                logger.warn("Connection status ${exception.status} from $endpoint")
            }

            return
        }

        val builder = EmbedBuilder()

        buildResponse(dto, avatarUrl, displayOpts, builder)

        val embed = builder.build()

        event.reply(embed) {
            // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
            if (dto.glucose!!.mgdl == 69 || dto.glucose!!.mmol == 6.9) {
                it.addReaction("\uD83D\uDE0F").queue()
            }
            // #36: Reply with :100: when value is 100 mg/dL or 5.5 mmol/L
            if (dto.glucose!!.mgdl == 100 || dto.glucose!!.mmol == 5.5) {
                it.addReaction("\uD83D\uDCAF").queue()
            }
        }
    }

    private fun getStoredData(event: CommandEvent) {
        val endpoint = getNightscoutHost(event.author) + "/api/v1/"

        val token = getToken(event.author)
        val displayOptions = getDisplayOptions(event.author)

        buildNightscoutResponse(endpoint, token, displayOptions, event.author.avatarUrl, event)
    }

    private fun getUnstoredData(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var avatarUrl: String? = null
        var token: String? = null
        var displayOptions: String = getDefaultDisplayOptions()
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

        if (event.message.mentionedUsers.size == 1 && !event.message.mentionsEveryone()) {
            token = getToken(event.message.mentionedUsers[0])
            displayOptions = getDisplayOptions(event.message.mentionedUsers[0])
            avatarUrl = event.message.mentionedUsers[0].avatarUrl
        }

        buildNightscoutResponse(endpoint, token, displayOptions, avatarUrl, event)
    }

    private fun processPebble(url: String, token: String?, dto: NightscoutDTO) {
        val endpoint = url.replace("/api/v1/", "/pebble")
        val json = getJson(endpoint, token, null)

        val bgsJson: JsonObject

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

    private fun buildResponse(dto: NightscoutDTO, avatarUrl: String?, displayOpts: String, builder: EmbedBuilder) {
        if(displayOpts.contains("trend")) builder.setTitle(dto.title)

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
        if(displayOpts.contains("trend")) builder.addField("trend", trendString, true)
        if (dto.iob != 0.0F && displayOpts.contains("iob")) {
            builder.addField("iob", dto.iob.toString(), true)
        }
        if (dto.cob != 0 && displayOpts.contains("cob")) {
            builder.addField("cob", dto.cob.toString(), true)
        }

        setResponseColor(dto, builder)

        if (avatarUrl != null && displayOpts.contains("avatar")) {
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

    private fun getJson(url: String, token: String?, query: String?): String {
        val client = HttpClient()
        val method = GetMethod(url)

        if (!query.isNullOrEmpty()) {
            method.queryString = query

            if (token != null) {
                method.queryString += "&token=$token"
            }
        } else if (token != null) {
            method.queryString = "token=$token"
        }

        val statusCode = client.executeMethod(method)

        if (statusCode != 200) {
            throw NightscoutStatusException(statusCode)
        }

        val body = method.responseBodyAsStream.bufferedReader().use { it.readText() }

        if (body.isEmpty()) {
            throw NoNightscoutDataException()
        }

        return body
    }

    @Throws(MalformedJsonException::class)
    private fun getGlucoseJson(url: String, token: String?): String {
        val endpoint = "$url/entries/sgv.json"

        val json = getJson(endpoint, token, "count=1")

        val jsonArray = JsonParser().parse(json).asJsonArray
        val arraySize = jsonArray.size()

        // SGV endpoint may be empty in some cases, fall back to entries endpoint
        if (arraySize == 0) {
            return getGlucoseJsonFallback(url, token)
        }

        return json
    }

    @Throws(MalformedJsonException::class)
    private fun getGlucoseJsonFallback(url: String, token: String?): String {
        val endpoint = "$url/entries.json"

        val json = getJson(endpoint, token, "count=1")

        val jsonArray = JsonParser().parse(json).asJsonArray
        val arraySize = jsonArray.size()

        // SGV endpoint may be empty in some cases, fall back to entries endpoint
        if (arraySize == 0) {
            throw NoNightscoutDataException()
        }

        return json
    }

    @Throws(IOException::class, UnknownUnitException::class)
    private fun getEntries(url: String, token: String?, dto: NightscoutDTO) {
        val json = getGlucoseJson(url, token)

        if (json.isEmpty()) {
            throw NoNightscoutDataException()
        }

        // Parse JSON and construct response
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

    private fun getSettings(url: String, token: String?, dto: NightscoutDTO) {
        val endpoint = "$url/status.json"
        val json = getJson(endpoint, token, null)

        val jsonObject = JsonParser().parse(json).asJsonObject
        val settings = jsonObject.get("settings").asJsonObject
        val ranges = settings.get("thresholds").asJsonObject

        val title = settings.get("customTitle").asString
        val units = settings.get("units").asString
        val low = ranges.get("bgLow").asInt
        val bottom = ranges.get("bgTargetBottom").asInt
        val top = ranges.get("bgTargetTop").asInt
        val high = ranges.get("bgHigh").asInt

        dto.title = title
        dto.low = low
        dto.bottom = bottom
        dto.top = top
        dto.high = high
        dto.units = units
    }

    private fun getTimestamp(epoch: Long?): ZonedDateTime {

        val i = Instant.ofEpochSecond(epoch!! / 1000)
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)
    }

    private fun getToken(user: User): String? {
        if (NightscoutDAO.getInstance().isNightscoutToken(user)) {
            return NightscoutDAO.getInstance().getNightscoutToken(user)
        }

        return null
    }

    private fun getDisplayOptions(user: User): String {
        if (NightscoutDAO.getInstance().isNightscoutDisplay(user)) {
            return NightscoutDAO.getInstance().getNightscoutDisplay(user)
        }
        // if there are no options set in redis, then have the default be all options
        return getDefaultDisplayOptions()
    }

    /**
     * Provides display options with everything enabled
     */
    private fun getDefaultDisplayOptions(): String {
        return NightscoutSetDisplayCommand.validOptions.joinToString(" ")
    }
}
