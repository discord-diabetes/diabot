package com.dongtronic.diabot.commands

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.data.NightscoutDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.google.gson.JsonParser
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
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
        this.help = "Get the most recent info from any nightscout site"
        this.arguments = "Partial nightscout url (part before .herokuapp.com)"
        this.guildOnly = true
        this.aliases = arrayOf("ns", "bg")
        this.category = category
        this.examples = arrayOf("diabot nightscout casscout", "diabot ns casscout")
    }

    override fun execute(event: CommandEvent) {

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args[0] == null) {
            event.reply("Please pass a partial heroku url (eg: casscout)")
            return
        }

        try {
            val urlTemplate = "https://%s.herokuapp.com/api/v1/"
            val endpoint = String.format(urlTemplate, args[0])

            val dto = NightscoutDTO()

            getData(endpoint, dto, event)
            getRanges(endpoint, dto, event)

            val builder = EmbedBuilder()

            buildResponse(dto, builder)

            val embed = builder.build()

            event.reply(embed)
        } catch (e: Exception) {
            event.reactError()
            logger.error("Error while responding to Nightscout request")
            e.printStackTrace()
        }

    }

    private fun buildResponse(dto: NightscoutDTO, builder: EmbedBuilder) {
        builder.setTitle("Nightscout")

        val mmolString: String
        val mgdlString: String
        if (dto.delta != null) {
            mmolString = buildGlucoseString(dto.glucose!!.mmol, dto.delta!!.mmol, dto.deltaIsNegative)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toDouble(), dto.delta!!.mgdl.toDouble(), dto.deltaIsNegative)
        } else {
            mmolString = buildGlucoseString(dto.glucose!!.mmol, 999.0, false)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toDouble(), 999.0, false)
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

    private fun buildGlucoseString(glucose: Double, delta: Double, negative: Boolean): String {
        val builder = StringBuilder()

        builder.append(glucose)

        if (delta != 999.0) {
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
