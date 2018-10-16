package com.dongtronic.diabot.commands

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.converters.GlucoseUnit
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.doc.standard.CommandInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConvertCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(ConvertCommand::class.java)

    init {
        this.name = "convert"
        this.help = "convert blood glucose between mmol/L and mg/dL"
        this.guildOnly = false
        this.arguments = "<value> <unit>"
        this.category = category
        this.examples = arrayOf("diabot convert 5", "My BG this morning was _127_", "How much is 7 mmol?")
    }

    override fun execute(event: CommandEvent) {
        if(event.author.isBot) {
            return
        }

        if (event.args.isEmpty()) {
            event.replyWarning("You didn't give me a value!")
        } else {
            // split the arguments on all whitespaces
            val items = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val result: ConversionDTO?

            try {

                logger.info("converting BG value " + items[0])

                result = BloodGlucoseConverter.convert(items[0], if (items.size == 2) items[1] else null)

                when {
                    result!!.inputUnit === GlucoseUnit.MMOL -> event.reply(String.format("%s mmol/L is %s mg/dL", result!!.original, result.converted))
                    result!!.inputUnit === GlucoseUnit.MGDL -> event.reply(String.format("%s mg/dL is %s mmol/L", result!!.original, result.converted))
                    else -> {
                        val reply = arrayOf(
                                "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                                "%s mg/dL is **%s mmol/L**",
                                "%s mmol/L is **%s mg/dL**").joinToString(
                                "%n")

                        event.reply(String.format(reply, result!!.original, result.mmol, result.original,
                                result.mgdl))
                    }
                }
            } catch (ex: IllegalArgumentException) {
                // Ignored on purpose
                logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
            } catch (ex: UnknownUnitException) {
                event.replyError("I don't know how to convert from " + items[1])
                logger.warn("Unknown BG unit " + items[1])
            }

        }
    }
}
