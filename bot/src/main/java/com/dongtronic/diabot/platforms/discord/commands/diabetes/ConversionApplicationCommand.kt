package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.github.kaktushose.jda.commands.annotations.interactions.Choices
import com.github.kaktushose.jda.commands.annotations.interactions.Command
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction
import com.github.kaktushose.jda.commands.annotations.interactions.Param
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent

@Interaction
class ConversionApplicationCommand : ApplicationCommand {
    @Command("convert", desc = "Convert blood glucose values between mmol/L and mg/dL")
    fun convert(event: CommandEvent,
        @Param("Blood glucose level")
        glucose: Double,
        @Param("Blood glucose unit (mmol/L, mg/dL)", optional = true)
        @Choices("mmol/L", "mg/dL")
        unit: String?
    ) {
        val result = BloodGlucoseConverter.convert(glucose.toString(), unit)
            .getOrElse {
                if (it is IllegalArgumentException) {
                    event.with().ephemeral(true).reply("Could not convert: ${it.message}")
                }
                return
            }

        val reply = when {
            result.inputUnit === GlucoseUnit.MMOL -> String.format("%s mmol/L is %s mg/dL", result.mmol, result.mgdl)
            result.inputUnit === GlucoseUnit.MGDL -> String.format("%s mg/dL is %s mmol/L", result.mgdl, result.mmol)
            else -> {
                String.format(
                    arrayOf(
                        "*I'm not sure if you gave me mmol/L or mg/dL, so I'll give you both.*",
                        "%s mg/dL is **%s mmol/L**",
                        "%s mmol/L is **%s mg/dL**"
                    ).joinToString(
                        "%n"
                    ),
                    glucose, result.mmol, glucose, result.mgdl
                )
            }
        }

        event.reply(reply)
    }
}
