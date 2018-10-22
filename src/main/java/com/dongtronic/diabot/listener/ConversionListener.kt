package com.dongtronic.diabot.listener

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.converters.GlucoseUnit
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.util.Patterns
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.managers.GuildController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

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

        var number = ""
        var unit = ""

        if (unitMatcher.matches()) {
            number = unitMatcher.group(4)
            unit = unitMatcher.group(5)
        }

        if (inlineMatcher.matches()) {
            number = inlineMatcher.group(1)
        } else if (separateMatcher.matches()) {
            number = separateMatcher.group(1)
        }

        if (number.isEmpty()) {
            return
        }

        try {
            val result: ConversionDTO? = if (unit.length > 1) {
                BloodGlucoseConverter.convert(number, unit)
            } else {
                BloodGlucoseConverter.convert(number, null)
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

                    channel.sendMessage(String.format(reply, result!!.mgdl, result.mmol, result.mmol,
                            result.mgdl)).queue()
                }
            }

        } catch (ex: IllegalArgumentException) {
            // Ignored on purpose
            logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
        } catch (ex: UnknownUnitException) {
            // Ignored on purpose
        }

    }

}
