package com.dongtronic.diabot.platforms.discord.commands.diabetes

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import com.dongtronic.diabot.commands.Category
import com.dongtronic.diabot.commands.annotations.CommandCategory
import com.dongtronic.diabot.commands.annotations.Example
import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.diabot.exceptions.UnknownUnitException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.JDACommandUser
import com.dongtronic.diabot.util.logger

class ConvertCommand {
    private val logger = logger()

    @CommandMethod("convert <value> [unit]")
    @CommandDescription("Convert blood glucose between mmol/L and mg/dL")
    @CommandCategory(Category.BG)
    @Example(["[convert] 5"])
    fun execute(
            sender: JDACommandUser,
            @Argument("value", description = "BG value")
            value: String,
            @Argument("unit", description = "Unit of BG value")
            unit: String?
    ) {
        val event = sender.event
        if (event.author.isBot) {
            return
        }

        val result: ConversionDTO?

        try {
            result = BloodGlucoseConverter.convert(value, unit)

            val reply = when {
                result!!.inputUnit === GlucoseUnit.MMOL -> String.format("%s mmol/L is %s mg/dL", result!!.mmol, result.mgdl)
                result!!.inputUnit === GlucoseUnit.MGDL -> String.format("%s mg/dL is %s mmol/L", result!!.mgdl, result.mmol)
                else -> {
                    String.format(
                            arrayOf(
                                    "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                                    "%s mg/dL is **%s mmol/L**",
                                    "%s mmol/L is **%s mg/dL**"
                            ).joinToString("%n"),
                            value,
                            result!!.mmol,
                            value,
                            result.mgdl
                    )
                }
            }

            sender.replyS(reply)

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
            sender.replyErrorS("Could not convert: ${ex.message}")
        } catch (ex: UnknownUnitException) {
            sender.replyErrorS("I don't know how to convert from $unit")
            logger.warn("Unknown BG unit $unit")
        }
    }
}
