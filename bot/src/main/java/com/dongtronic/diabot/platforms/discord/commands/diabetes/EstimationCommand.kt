package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.data.A1cFromBgDTO
import com.dongtronic.diabot.data.A1cToBgDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.A1cConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit.MGDL
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit.MMOL
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class EstimationCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val logger = logger()

    init {
        this.name = "estimate"
        this.help = "estimate A1c from average blood glucose, or average blood glucose from A1c"
        this.guildOnly = false
        this.arguments = "<a1c/average> <number> [unit]"
        this.examples = arrayOf("diabot estimate a1c 120", "diabot estimate a1c 5.7", "diabot estimate a1c 120 mg/dL", "diabot estimate average 6.7", "diabot estimate average 42")
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty()) {
            event.replyWarning("You didn't give me a value!")
        } else {
            // split the arguments on all whitespaces
            val items = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (items.size < 2) {
                event.replyWarning("Required arguments: `mode` & `value`\nexample: diabot estimate a1c 6.9")
            }

            when (items[0].toUpperCase()) {
                "A1C" -> estimateA1c(event)
                "AVERAGE" -> estimateAverage(event)
                else -> event.replyError("Unknown mode. Choose either `a1c` or `average`")
            }


        }
    }

    private fun estimateAverage(event: CommandEvent) {
        val items = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val pattern = "[^0-9\\.]"
        val number = items[1].replace(pattern.toRegex(), "").toDoubleOrNull()

        if (number == null) {
            event.replyError("You did not input a proper number")
            return
        }

        val result: A1cToBgDTO?

        result = A1cConverter.a1cToBg(number)

        event.reply(String.format("An A1c of **%s%%** (DCCT) or **%s mmol/mol** (IFCC) is about **%s mg/dL** or **%s mmol/L**", result.dcct, result.ifcc, result.bgAverage.mgdl, result.bgAverage.mmol))

    }

    private fun estimateA1c(event: CommandEvent) {
        // split the arguments on all whitespaces
        val items = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val result: A1cFromBgDTO

        try {
            result = A1cConverter.a1cFromBg(items[1], items.getOrNull(2))

            when {
                result.inputGlucose.inputUnit === MMOL -> event.reply(String.format("An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.inputGlucose.mmol, result.dcct, result.ifcc))
                result.inputGlucose.inputUnit === MGDL -> event.reply(String.format("An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.inputGlucose.mgdl, result.dcct, result.ifcc))
                else -> {
                    //TODO: Make arguments for result.getDcct and result.getIfcc less confusing. ie: not wrong
                    val reply = String.format(
                            "An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC) %n",
                            result.inputGlucose.original,
                            result.getDcct(MMOL),
                            result.getIfcc(MMOL)
                    ) + String.format(
                            "An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)",
                            result.inputGlucose.original,
                            result.getDcct(MGDL),
                            result.getIfcc(MGDL)
                    )
                    event.reply(reply)
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
