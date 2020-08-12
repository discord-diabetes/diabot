package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class ConvertCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val logger = logger()

    init {
        this.name = "convert"
        this.help = "convert blood glucose between mmol/L and mg/dL"
        this.guildOnly = false
        this.arguments = "<value> <unit>"
        this.examples = arrayOf("diabot convert 5", "My BG this morning was _127_", "How much is 7 mmol?")
    }

    override fun execute(event: CommandEvent) {
        if (event.author.isBot) {
            return
        }

        if (event.args.isEmpty()) {
            event.replyWarning("You didn't give me a value!")
        } else {
            // split the arguments on all whitespaces
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val result: ConversionDTO?

            try {
                result = BloodGlucoseConverter.convert(args[0], if (args.size == 2) args[1] else null)

                val reply = when {
                    result!!.inputUnit === GlucoseUnit.MMOL -> String.format("%s mmol/L is %s mg/dL", result!!.mmol, result.mgdl)
                    result!!.inputUnit === GlucoseUnit.MGDL -> String.format("%s mg/dL is %s mmol/L", result!!.mgdl, result.mmol)
                    else -> {
                        String.format(arrayOf(
                                "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                                "%s mg/dL is **%s mmol/L**",
                                "%s mmol/L is **%s mg/dL**").joinToString(
                                "%n"), args[0], result!!.mmol, args[0], result.mgdl)
                    }
                }

                event.reply(reply)

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
                event.replyError("I don't know how to convert from " + args[1])
                logger.warn("Unknown BG unit " + args[1])
            }

        }
    }
}
