package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.data.NightscoutDTO
import com.dongtronic.diabot.data.NightscoutUserDTO
import com.dongtronic.diabot.exceptions.NightscoutStatusException
import com.dongtronic.diabot.exceptions.NoNightscoutDataException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.LimitedSystemDnsResolver
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NightscoutCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(NightscoutCommand::class.java)
    private val httpClient: HttpClient
    private val requestConfig: RequestConfig
    private val trendArrows: Array<String> = arrayOf("", "↟", "↑", "↗", "→", "↘", "↓", "↡", "↮", "↺")
    private val defaultQuery: Array<NameValuePair> = arrayOf(
            BasicNameValuePair("find[sgv][\$exists]", ""),
            BasicNameValuePair("count", "1"))

    init {
        this.name = "nightscout"
        this.help = "Get the most recent info from any Nightscout site"
        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
        this.guildOnly = false
        this.aliases = arrayOf("ns", "bg")
        this.examples = arrayOf("diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com", "diabot ns public false")
        this.children = arrayOf(
                NightscoutSetUrlCommand(category, this),
                NightscoutDeleteCommand(category, this),
                NightscoutPublicCommand(category, this),
                NightscoutSetTokenCommand(category, this),
                NightscoutSetDisplayCommand(category, this)
        )

        httpClient = HttpClientBuilder
                .create()
                // Limit to two IP addresses per hostname
                .setDnsResolver(LimitedSystemDnsResolver(2))
                .build()

        requestConfig = RequestConfig
                .custom()
                // Set timeouts to 8 seconds
                .setSocketTimeout(8000)
                .setConnectionRequestTimeout(8000)
                .setConnectTimeout(8000)
                .build()
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
        } catch (ex: InsufficientPermissionException) {
            logger.info("Couldn't reply with nightscout data due to missing permission: ${ex.permission}")
            event.replyError("Couldn't perform requested action due to missing permission: `${ex.permission}`")
        } catch (ex: UnknownHostException) {
            event.reactError()
            logger.info("No host found: ${ex.message}")
        } catch (ex: Exception) {
            event.reactError()
            logger.warn("Unexpected error: " + ex.message)
        }

    }

    private fun buildNightscoutResponse(endpoint: String, userDTO: NightscoutUserDTO, event: CommandEvent) {
        val dto = NightscoutDTO()

        try {
            getSettings(endpoint, userDTO.token, dto)
            getEntries(endpoint, userDTO.token, dto)
            processPebble(endpoint, userDTO.token, dto)
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

        val channelType = event.channelType;
        var shortReply = false
        if (channelType == ChannelType.TEXT) {
            shortReply = NightscoutDAO.getInstance().listShortChannels(event.guild.id).contains(event.channel.id) ||
                    userDTO.displayOptions.contains("simple")
        }

        if (shortReply) {
            val message = buildShortResponse(dto, userDTO.displayOptions)
            event.reply(message) { replyMessage -> addReactions(dto, replyMessage) }
        } else {
            val builder = EmbedBuilder()
            buildResponse(dto, userDTO.avatarUrl, userDTO.displayOptions, builder)
            val embed = builder.build()
            event.reply(embed) { replyMessage -> addReactions(dto, replyMessage) }
        }
    }


    private fun getStoredData(event: CommandEvent) {
        val endpoint = getNightscoutHost(event.author) + "/api/v1/"
        val userDTO = NightscoutUserDTO()

        getUserDto(event.author, userDTO)
        buildNightscoutResponse(endpoint, userDTO, event)
    }

    private fun getUnstoredData(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val userDTO = NightscoutUserDTO()
        val endpoint = when {
            event.event.message.mentionedUsers.size == 1 -> {
                val user = event.event.message.mentionedMembers[0].user
                try {
                    if (!getNightscoutPublic(user, event.guild.id)) {
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
                if (hostname.contains("http://") || hostname.contains("https://")) {
                    var domain = hostname
                    if (domain.endsWith("/")) {
                        domain = domain.trimEnd('/')
                    }

                    // If Nightscout data cannot be retrieved from this domain then stop
                    if (!getUnstoredDataForDomain(domain, event, userDTO))
                        return

                    "$domain/api/v1/"
                } else {
                    "https://$hostname.herokuapp.com/api/v1/"
                }
            }
        }

        if (event.message.mentionedUsers.size == 1 && !event.message.mentionsEveryone()) {
            getUserDto(event.message.mentionedUsers[0], userDTO)
        }

        buildNightscoutResponse(endpoint, userDTO, event)
    }

    private fun processPebble(url: String, token: String?, dto: NightscoutDTO) {
        val endpoint = url.replace("/api/v1/", "/pebble")
        val json = getJson(endpoint, token)

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

    private fun buildShortResponse(dto: NightscoutDTO, displayOptions: Array<String>): String {
        // Name: mmol/L: 6.1(+0.3) | mg/dL: 109(+5) | trend: → | iob: 0.56 | cob: 3 | Today at 8:01 PM

        // title: mmol/L: value(delta) | mg/dL: value(delta) | trend: <trend>

        val response: StringBuilder = StringBuilder()

        if (displayOptions.contains("title")) {
            response.append(dto.title).append(": ")
        }

        val (mmolString: String, mgdlString: String) = buildGlucoseStrings(dto)

        response.append("mmol/L: ").append(mmolString).append(" | ").append("mg/dL: ").append(mgdlString).append(" | ")

        if (displayOptions.contains("trend")) {
            val trendString = trendArrows[dto.trend]
            response.append("trend: ").append(trendString).append(" | ")
        }

        if (displayOptions.contains("iob") && dto.iob != 0.0F) {
            response.append("iob: ").append(dto.iob).append(" | ")
        }

        if (displayOptions.contains("cob") && dto.cob != 0) {
            response.append("cob: ").append(dto.cob).append(" | ")
        }


        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (O)")
        response.append(dto.dateTime!!.format(formatter))

        return response.toString()
    }

    private fun buildResponse(dto: NightscoutDTO, avatarUrl: String?, displayOptions: Array<String>, builder: EmbedBuilder) {
        if (displayOptions.contains("title")) builder.setTitle(dto.title)

        val (mmolString: String, mgdlString: String) = buildGlucoseStrings(dto)

        val trendString = trendArrows[dto.trend]
        builder.addField("mmol/L", mmolString, true)
        builder.addField("mg/dL", mgdlString, true)
        if (displayOptions.contains("trend")) builder.addField("trend", trendString, true)
        if (dto.iob != 0.0F && displayOptions.contains("iob")) {
            builder.addField("iob", dto.iob.toString(), true)
        }
        if (dto.cob != 0 && displayOptions.contains("cob")) {
            builder.addField("cob", dto.cob.toString(), true)
        }

        setResponseColor(dto, builder)

        if (avatarUrl != null && displayOptions.contains("avatar")) {
            builder.setThumbnail(avatarUrl)
        }

        builder.setTimestamp(dto.dateTime)
        builder.setFooter("measured", "https://github.com/nightscout/cgm-remote-monitor/raw/master/static/images/large.png")

        if (dto.dateTime!!.plusMinutes(15).toInstant().isBefore(ZonedDateTime.now().toInstant())) {
            builder.setDescription("**BG data is more than 15 minutes old**")
        }
    }

    private fun buildGlucoseStrings(dto: NightscoutDTO): Pair<String, String> {
        val mmolString: String
        val mgdlString: String
        if (dto.delta != null) {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), dto.delta!!.mmol.toString(), dto.deltaIsNegative)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), dto.delta!!.mgdl.toString(), dto.deltaIsNegative)
        } else {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), "999.0", false)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), "999.0", false)
        }
        return Pair(mmolString, mgdlString)
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

    private fun addReactions(dto: NightscoutDTO, response: Message) {
        // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
        if (dto.glucose!!.mgdl == 69 || dto.glucose!!.mmol == 6.9) {
            response.addReaction("\uD83D\uDE0F").queue()
        }
        // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
        if (dto.glucose!!.mgdl == 100
                || dto.glucose!!.mmol == 5.5
                || dto.glucose!!.mmol == 10.0) {
            response.addReaction("\uD83D\uDCAF").queue()
        }
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

    /**
     * Gets token, display options, and an avatar URL for the domain given.
     * Replies with an error and returns false if no user is found, nightscout data is private, or if no token is found.
     *
     * @return if data can be retrieved from the given domain
     */
    private fun getUnstoredDataForDomain(domain: String, event: CommandEvent, userDTO: NightscoutUserDTO): Boolean {
        // Test if the Nightscout hosted at the given domain requires a token
        // If a token is not needed, skip grabbing data
        if (testNightscoutForToken(domain)) {
            // Get user ID from domain. If no user is found, respond with an error
            val userId = getUserIdForDomain(domain)
            val user: User?

            if (userId == null) {
                event.replyError("Token secured Nightscout does not belong to any user.")
                return false
            }

            user = event.jda.getUserById(userId)

            if (user == null) {
                event.replyError("Couldn't find user $userId")
                return false
            }

            if (!NightscoutDAO.getInstance().isNightscoutPublic(user, event.guild.id)) {
                // Nightscout data is private
                event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is private.")
                return false
            }

            if (!NightscoutDAO.getInstance().isNightscoutToken(user)) {
                // No token found
                event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is unreadable due to missing token.")
                return false
            }

            getUserDto(user, userDTO)
        }
        return true
    }

    /**
     * Tests whether a Nightscout requires a token to be read
     *
     * @return true if a token is required, false if not
     */
    private fun testNightscoutForToken(domain: String): Boolean {
        try {
            getJson("$domain/api/v1/status", null)
        } catch (exception: NightscoutStatusException) {
            // If an unauthorized error occurs when trying to retrieve the status page, a token is needed
            if (exception.status == 401) {
                return true
            }
        }

        return false
    }

    /**
     * Finds a user ID in the redis database matching with the Nightscout domain given
     */
    private fun getUserIdForDomain(domain: String): String? {
        val users = NightscoutDAO.getInstance().listUsers()

        // Loop through database and find a userId that matches with the domain provided
        for ((uid, value) in users) {
            if (value == domain) {
                return uid.substring(0, uid.indexOf(":"))
            }
        }

        // If no users are found with the given domain, return null
        return null
    }

    /**
     * Sets the data inside the given NightscoutUserDTO for the given user
     */
    private fun getUserDto(user: User, userDTO: NightscoutUserDTO) {
        userDTO.token = getToken(user)
        userDTO.displayOptions = getDisplayOptions(user)
        userDTO.avatarUrl = user.avatarUrl
    }

    private fun getNightscoutPublic(user: User, guildId: String): Boolean {
        return NightscoutDAO.getInstance().isNightscoutPublic(user, guildId)
    }

    private fun getJson(url: String, token: String?, vararg query: NameValuePair): String {
        val request = RequestBuilder.get()

        if (token != null) {
            request.addParameter("token", token)
        }

        request.addParameters(*query)
        request.setUri(url)
        request.config = requestConfig

        val response = httpClient.execute(request.build())
        val statusCode = response.statusLine.statusCode

        if (statusCode != 200) {
            throw NightscoutStatusException(statusCode)
        }

        val body = EntityUtils.toString(response.entity)

        if (body.isEmpty()) {
            throw NoNightscoutDataException()
        }

        return body
    }

    @Throws(MalformedJsonException::class)
    private fun getGlucoseJson(url: String, token: String?): String {
        val endpoint = "$url/entries.json"
        val json = getJson(endpoint, token, *defaultQuery)

        val jsonArray = JsonParser().parse(json).asJsonArray
        val arraySize = jsonArray.size()

        // Throw an exception if the endpoint is empty of SGV entries
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
        val json = getJson(endpoint, token)

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

    private fun getDisplayOptions(user: User): Array<String> {
        if (NightscoutDAO.getInstance().isNightscoutDisplay(user)) {
            return NightscoutDAO.getInstance().getNightscoutDisplay(user).split(" ").toTypedArray()
        }
        // if there are no options set in redis, then have the default be all options
        return getDefaultDisplayOptions()
    }

    /**
     * Provides display options with everything enabled
     */
    private fun getDefaultDisplayOptions(): Array<String> {
        return NightscoutSetDisplayCommand.enabledOptions
    }
}
