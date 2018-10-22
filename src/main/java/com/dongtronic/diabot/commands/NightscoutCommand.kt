package com.dongtronic.diabot.commands

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.data.NightscoutDTO
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.exceptions.UnknownUnitException
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

class NightscoutCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(NightscoutCommand::class.java)

    init {
        this.name = "nightscout"
        this.help = "Get the most recent info from any Nightscout site"
        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
        this.guildOnly = true
        this.aliases = arrayOf("ns", "bg")
        this.category = category
        this.examples = arrayOf("diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com")
    }

    override fun execute(event: CommandEvent) {

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        try {
            if (args.isEmpty()) {
                getStoredData(event)
                return
            }

            val command = args[0].toUpperCase()

            when (command) {
                "SET", "S" -> setNightscoutUrl(event)
                "DELETE", "REMOVE", "DELET", "D", "R" -> deleteNightscoutUrl(event)
                else -> getUnstoredData(event)
            }
        } catch (ex: UnconfiguredNightscoutException) {
            event.reply("Please set your Nightscout hostname using `diabot nightscout set <hostname>`")
        } catch (ex: IllegalArgumentException) {
            event.reply("Error: " + ex.message)
        } catch (ex: Exception) {
            event.reactError()
        }

    }

    private fun buildNightscoutResponse(endpoint: String, event: CommandEvent) {
        val dto = NightscoutDTO()

        getData(endpoint, dto, event)
        getRanges(endpoint, dto, event)

        val builder = EmbedBuilder()

        buildResponse(dto, builder)

        val embed = builder.build()

        event.reply(embed)
    }

    private fun getStoredData(event: CommandEvent) {
        val endpoint = getNightscoutHost(event.author) + "/api/v1/"

        buildNightscoutResponse(endpoint, event)
    }

    private fun setNightscoutUrl(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        setNightscoutUrl(event.author, args[1])

        event.message.delete().reason("privacy").queue()
        event.reply("Set Nightscout URL for ${event.author.name}")
    }

    private fun deleteNightscoutUrl(event: CommandEvent) {
        removeNightscoutUrl(event.author)
        event.reply("Removed Nightscout URL for ${event.author.name}")
    }

    private fun getUnstoredData(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val hostname = args[0]
        val endpoint = "https://$hostname.herokuapp.com/api/v1/"

        buildNightscoutResponse(endpoint, event)
    }

    private fun buildResponse(dto: NightscoutDTO, builder: EmbedBuilder) {
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

        builder.addField("mmol/L", mmolString, true)
        builder.addField("mg/dL", mgdlString, true)

        setResponseColor(dto, builder)

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

    private fun validateNightscoutUrl(url: String): String {
        var finalUrl = url
        if (!finalUrl.contains("http://") && !finalUrl.contains("https://")) {
            throw IllegalArgumentException("Url must contain scheme")
        }

        if (finalUrl.endsWith("/")) {
            finalUrl = finalUrl.trimEnd('/')
        }

        return finalUrl
    }

    private fun setNightscoutUrl(user: User, url: String) {
        val finalUrl = validateNightscoutUrl(url)
        NightscoutDAO.getInstance().setNightscoutUrl(user, finalUrl)
    }

    private fun getNightscoutHost(user: User): String {
        val url = NightscoutDAO.getInstance().getNightscoutUrl(user)

        if (url == null) {
            throw UnconfiguredNightscoutException()
        } else {
            return url
        }
    }

    private fun removeNightscoutUrl(user: User) {
        NightscoutDAO.getInstance().removeNIghtscoutUrl(user)
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
        val ranges = jsonObject.get("settings").asJsonObject.get("thresholds").asJsonObject

        val low = ranges.get("bgLow").asInt
        val bottom = ranges.get("bgTargetBottom").asInt
        val top = ranges.get("bgTargetTop").asInt
        val high = ranges.get("bgHigh").asInt

        dto.low = low
        dto.bottom = bottom
        dto.top = top
        dto.high = high
    }

    @Throws(IOException::class, UnknownUnitException::class)
    private fun getData(url: String, dto: NightscoutDTO, event: CommandEvent) {
        val client = HttpClient()
        val method = GetMethod("$url/entries.json")

        method.queryString = "count=1"

        val statusCode = client.executeMethod(method)

        if (statusCode == -1) {
            event.reactError()
        }

        val json = method.responseBodyAsString

        val jsonObject = JsonParser().parse(json).asJsonArray.get(0).asJsonObject
        val sgv = jsonObject.get("sgv").asString
        val timestamp = jsonObject.get("date").asLong

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
    }

    private fun getTimestamp(epoch: Long?): ZonedDateTime {

        val i = Instant.ofEpochSecond(epoch!! / 1000)
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)

    }
}
