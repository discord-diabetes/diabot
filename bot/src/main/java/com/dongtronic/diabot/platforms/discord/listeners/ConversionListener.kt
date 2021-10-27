package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.util.Patterns
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.regex.Matcher

class ConversionListener : ListenerAdapter() {
    private val logger = logger()

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val message = event.message
        val messageText = message.contentRaw

        val separateMatcher = Patterns.separateBgPattern.matcher(messageText)

        if (separateMatcher.matches()) {
            sendMessage(getResult(separateMatcher.group(1), "", event), event)
        } else {
            sendMessage(recursiveReading(event, messageText), event)
        }
    }

    private fun recursiveReading(event: GuildMessageReceivedEvent, previousMessageText: String): String {
        if (event.author.isBot) return ""


        var matched = false
        var newMessageText = ""

        val inlineMatcher = Patterns.inlineBgPattern.matcher(previousMessageText)
        val unitMatcher = Patterns.unitBgPattern.matcher(previousMessageText)

        var numberString = ""
        var unitString = ""

        if (unitMatcher.matches()) {
            numberString = unitMatcher.group(4)
            unitString = unitMatcher.group(5)
            matched = true
            newMessageText = removeGroup(previousMessageText, unitMatcher, 4)
        }else if (inlineMatcher.matches()) {
            numberString = inlineMatcher.group(1)
            matched = true
            newMessageText = removeGroup(previousMessageText, inlineMatcher, 1)
        }

        if (numberString.isEmpty()) {
            return ""
        }

        if (matched) {
            return recursiveReading(event, newMessageText) + getResult(numberString, unitString, event)
        }
        return ""
    }

    private fun getResult(originalNumString: String, originalUnitString: String, event: GuildMessageReceivedEvent): String {

        var finalMessage = ""

        var numberString = originalNumString

        try {
            if (numberString.contains(',')) {
                numberString = numberString.replace(',', '.')
            }

            val result: ConversionDTO? = if (originalUnitString.length > 1) {
                BloodGlucoseConverter.convert(numberString, originalUnitString)
            } else {
                BloodGlucoseConverter.convert(numberString, null)
            }

            finalMessage += when {
                result!!.inputUnit === GlucoseUnit.MMOL -> String.format("%s mmol/L is %s mg/dL", result!!.mmol, result.mgdl)
                result!!.inputUnit === GlucoseUnit.MGDL -> String.format("%s mg/dL is %s mmol/L", result!!.mgdl, result.mmol)
                else -> {
                    val reply = arrayOf(
                            "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                            "%s mg/dL is **%s mmol/L**",
                            "%s mmol/L is **%s mg/dL**").joinToString(
                            "%n")

                    String.format(reply, numberString, result!!.mmol, numberString, result.mgdl)
                }
            }

            // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
            if (result.mmol == 6.9 || result.mgdl == 69) {
                event.message.addReaction("\uD83D\uDE0F").queue()
            }

            // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
            if (result.mmol == 5.5
                    || result.mmol == 10.0
                    || result.mgdl == 100) {
                event.message.addReaction("\uD83D\uDCAF").queue()
            }

            return finalMessage + "\n"

        } catch (ex: IllegalArgumentException) {
            // Ignored on purpose
            logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }
        return ""
    }

    private fun sendMessage(message: String, event: GuildMessageReceivedEvent) {
        val channel = event.channel
        if (message.isNotEmpty()) {
            channel.sendMessage(message).queue()
        }

    }

    private fun removeGroup(message: String, m: Matcher, group: Int): String {
        val start = m.start(group)
        val end = m.end(group)
        return message.substring(0, start) + message.substring(end)
    }
}
