package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.util.Patterns
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ConversionListener(private val prefix: String) : ListenerAdapter() {
    private val logger = logger()

    companion object {
        private const val MAX_MATCHES = 5

        val monospacePattern = Regex("`[^`]+`")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (event.message.contentRaw.startsWith(prefix, true)) return

        val message = event.message
        val messageText = stripMonospace(message.contentRaw)

        val separateMatcher = Patterns.separateBgPattern.matcher(messageText)

        if (separateMatcher.matches()) {
            sendMessage(getResult(separateMatcher.group(1), null, event), event)
        } else {
            sendMessage(recursiveReading(event, messageText), event)
        }
    }

    private fun recursiveReading(event: MessageReceivedEvent, previousMessageText: String): String {
        if (event.author.isBot) return ""

        val inlineMatches = Patterns.inlineBgPattern.findAll(previousMessageText)
        val unitMatches = Patterns.unitBgPattern.findAll(previousMessageText)

        val getNumberUnit: (MatchResult) -> (Pair<String, String?>) = {
            val number = it.groups["value"]!!.value
            val unit = kotlin.runCatching { it.groups["unit"]?.value }.getOrNull()

            number to unit
        }

        val sortedMatches = unitMatches
                .plus(inlineMatches)
                .take(MAX_MATCHES)
                .filter { it.groups["value"] != null }
                .sortedBy { it.range.first }
                .distinctBy {
                    val pair = getNumberUnit(it)
                    pair.first + pair.second
                }

        val multipleMatches = sortedMatches.count() > 1

        return sortedMatches.joinToString("\n") {
            val resultPair = getNumberUnit(it)

            getResult(resultPair.first, resultPair.second, event, multipleMatches)
        }
    }

    private fun getResult(originalNumString: String,
                          originalUnitString: String?,
                          event: MessageReceivedEvent,
                          multipleMatches: Boolean = false): String {
        val separator = if (multipleMatches) "─ " else ""
        val numberString = originalNumString.replace(',', '.')

        val result: ConversionDTO = BloodGlucoseConverter.convert(numberString, originalUnitString)
                .getOrElse { return "" }

        BloodGlucoseConverter.getReactions(result).forEach {
            event.message.addReaction(Emoji.fromUnicode(it)).queue()
        }

        return when {
            result.inputUnit === GlucoseUnit.MMOL -> String.format("$separator%s mmol/L is %s mg/dL", result.mmol, result.mgdl)
            result.inputUnit === GlucoseUnit.MGDL -> String.format("$separator%s mg/dL is %s mmol/L", result.mgdl, result.mmol)
            else -> {
                val reply = arrayOf(
                        "$separator*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                        "┌%s mg/dL is **%s mmol/L**",
                        "└%s mmol/L is **%s mg/dL**").joinToString(
                        "%n")

                String.format(reply, numberString, result.mmol, numberString, result.mgdl)
            }
        }
    }

    private fun stripMonospace(inputText: String): String {
        var thisMessage = inputText.replace("\\`", "")
        thisMessage = monospacePattern.replace(thisMessage, "")

        var inBlock = false
        val newLines = ArrayList<String>()

        thisMessage.lines().forEach { line ->
            val thisLine = line.replace("\\`", "")

            if (thisLine.contains("```")) {
                if (!inBlock) {
                    // we're starting a new block, ignore everything after the ticks
                    newLines.add(line.substringBefore("```"))
                } else {
                    newLines.add(line.substringAfter("```"))
                }

                inBlock = !inBlock
            } else {
                if (!inBlock) {
                    newLines.add(thisLine)
                }
            }
        }

        return newLines.joinToString("\n")
    }

    private fun sendMessage(message: String, event: MessageReceivedEvent) {
        val channel = event.channel
        if (message.isNotEmpty()) {
            channel.sendMessage(message).queue()
        }

    }
}
