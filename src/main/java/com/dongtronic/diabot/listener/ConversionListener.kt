package com.dongtronic.diabot.listener

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.converters.GlucoseUnit
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.util.Patterns
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class ConversionListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(ConversionListener::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val channel = event.channel

        val message = event.message
        val messageText = message.contentRaw

        val inlineMatcher = Patterns.inlineBgPattern.matcher(messageText)
        val separateMatcher = Patterns.separateBgPattern.matcher(messageText)
        val unitMatcher = Patterns.unitBgPattern.matcher(messageText)

        var numberString = ""
        var unitString = ""

        if (unitMatcher.matches()) {
            numberString = unitMatcher.group(4)
            unitString = unitMatcher.group(5)
        }

        if (inlineMatcher.matches()) {
            numberString = inlineMatcher.group(1)
        } else if (separateMatcher.matches()) {
            numberString = separateMatcher.group(1)
        }

        if (numberString.isEmpty()) {
            return
        }

        try {
            val result: ConversionDTO? = if (unitString.length > 1) {
                BloodGlucoseConverter.convert(numberString, unitString)
            } else {
                BloodGlucoseConverter.convert(numberString, null)
            }

            when {
                result!!.inputUnit === GlucoseUnit.MMOL -> channel.sendMessage(String.format("%s mmol/L is %s mg/dL", result!!.mmol, result.mgdl)).queue()
                result!!.inputUnit === GlucoseUnit.MGDL -> channel.sendMessage(String.format("%s mg/dL is %s mmol/L", result!!.mgdl, result.mmol)).queue()
                else -> {
                    val reply = arrayOf(
                            "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                            "%s mg/dL is **%s mmol/L**",
                            "%s mmol/L is **%s mg/dL**").joinToString(
                            "%n")

                    channel.sendMessage(String.format(reply, numberString, result!!.mmol, numberString,
                            result.mgdl)).queue()
                }
            }

            // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
            if (numberString == "6.9" || numberString == "69") {
                event.message.addReaction("\uD83D\uDE0F").queue()
            }

            // #36: Reply with :100: when value is 100 mg/dL or 5.5 mmol/L
            if (numberString == "5.5" || numberString == "100") {
                event.message.addReaction("\uD83D\uDCAF").queue()
            }

        } catch (ex: IllegalArgumentException) {
            // Ignored on purpose
            logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }

    }

}
