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

        var matched = false
        var modifiedMessageText = ""

        val channel = event.channel

        val message = event.message
        val messageText = message.contentRaw

        val inlineMatcher = Patterns.inlineBgPattern.matcher(messageText)
        val separateMatcher = Patterns.separateBgPattern.matcher(messageText)
        val unitMatcher = Patterns.unitBgPattern.matcher(messageText)
        unitMatcher.group()

        var numberString = ""
        var unitString = ""

        if (unitMatcher.matches()) {
            numberString = unitMatcher.group(4)
            unitString = unitMatcher.group(5)
            matched = true
            modifiedMessageText = removeGroup(messageText, inlineMatcher, 4)
        }

        if (inlineMatcher.matches()) {
            numberString = inlineMatcher.group(1)
            matched = true
            modifiedMessageText = removeGroup(messageText, inlineMatcher, 1)
        } else if (separateMatcher.matches()) {
            numberString = separateMatcher.group(1)
        }

        if (numberString.isEmpty()) {
            return
        }

        try {
            if (numberString.contains(',')) {
                numberString = numberString.replace(',', '.')
            }
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
            if (result.mmol == 6.9 || result.mgdl == 69) {
                event.message.addReaction("\uD83D\uDE0F").queue()
            }

            // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
            if (result.mmol == 5.5
                    || result.mmol == 10.0
                    || result.mgdl == 100) {
                event.message.addReaction("\uD83D\uDCAF").queue()
            }

        } catch (ex: IllegalArgumentException) {
            // Ignored on purpose
            logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }

        if (matched) {
            recursiveReading(event, modifiedMessageText)
        }
    }

    fun recursiveReading(event: GuildMessageReceivedEvent, messageText: String) {
        if (event.author.isBot) return

        val channel = event.channel

        var matched = false
        var modifiedMessageText = ""

        val inlineMatcher = Patterns.inlineBgPattern.matcher(messageText)
        val unitMatcher = Patterns.unitBgPattern.matcher(messageText)

        var numberString = ""
        var unitString = ""

        if (unitMatcher.matches()) {
            numberString = unitMatcher.group(4)
            unitString = unitMatcher.group(5)
            matched = true
            modifiedMessageText = removeGroup(messageText, unitMatcher, 4)
        }

        if (inlineMatcher.matches()) {
            numberString = inlineMatcher.group(1)
            matched = true
            modifiedMessageText = removeGroup(messageText, inlineMatcher, 1)
        }

        if (numberString.isEmpty()) {
            return
        }

        try {
            if (numberString.contains(',')) {
                numberString = numberString.replace(',', '.')
            }
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
            if (result.mmol == 6.9 || result.mgdl == 69) {
                event.message.addReaction("\uD83D\uDE0F").queue()
            }

            // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
            if (result.mmol == 5.5
                    || result.mmol == 10.0
                    || result.mgdl == 100) {
                event.message.addReaction("\uD83D\uDCAF").queue()
            }

        } catch (ex: IllegalArgumentException) {
            // Ignored on purpose
            logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }

        if (matched) {
            recursiveReading(event, modifiedMessageText)
        }

    }

    fun removeGroup(message: String, m: Matcher, group: Int): String {
        val start = m.start(group) //message.lastIndexOf(toRemove)
        val end = m.end(group)
        return message.substring(0, start) + message.substring(end)
    }
}
