package com.dongtronic.diabot.commands

import com.dongtronic.diabot.converters.BloodGlucoseConverter
import com.dongtronic.diabot.converters.GlucoseUnit
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
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
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val result: ConversionDTO?

            try {

                logger.info("converting BG value " + args[0])

                result = BloodGlucoseConverter.convert(args[0], if (args.size == 2) args[1] else null)

                when {
                    result!!.inputUnit === GlucoseUnit.MMOL -> event.reply(String.format("%s mmol/L is %s mg/dL", result!!.mmol, result.mgdl))
                    result!!.inputUnit === GlucoseUnit.MGDL -> event.reply(String.format("%s mg/dL is %s mmol/L", result!!.mgdl, result.mmol))
                    else -> {
                        val reply = arrayOf(
                                "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                                "%s mg/dL is **%s mmol/L**",
                                "%s mmol/L is **%s mg/dL**").joinToString(
                                "%n")

                        event.reply(String.format(reply, args[0], result!!.mmol, args[0],
                                result.mgdl))
                    }
                }

                // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
                if(args[0] == "6.9" || args[0] == "69") {
                    event.message.addReaction("\uD83D\uDE0F").queue()
                }

            } catch (ex: IllegalArgumentException) {
                // Ignored on purpose
                logger.warn("IllegalArgumentException occurred but was ignored in BG conversion")
            } catch (ex: UnknownUnitException) {
                event.replyError("I don't know how to convert from " + args[1])
                logger.warn("Unknown BG unit " + args[1])
            }

        }
    }
}
