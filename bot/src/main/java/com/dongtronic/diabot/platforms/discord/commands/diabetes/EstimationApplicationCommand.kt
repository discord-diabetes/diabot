package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.logic.diabetes.A1cConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.github.kaktushose.jda.commands.annotations.interactions.Choices
import com.github.kaktushose.jda.commands.annotations.interactions.Command
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction
import com.github.kaktushose.jda.commands.annotations.interactions.Param
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent

@Interaction
class EstimationApplicationCommand : ApplicationCommand {

    @Command("average", desc = "Estimate average glucose from A1c")
    fun estimateAverage(event: CommandEvent, @Param("A1c value") a1c: Double) {
        val result = A1cConverter.estimateAverage(a1c.toString()).getOrElse {
            event.with().ephemeral(true).reply("Could not estimate average BG: ${it.message}")
            return
        }

        event.reply(
            String.format(
                "An A1c of **%s%%** (DCCT) or **%s mmol/mol** (IFCC) is about **%s mg/dL** or **%s mmol/L**",
                result.dcct, result.ifcc, result.original.mgdl, result.original.mmol
            )
        )
    }

    @Command("a1c", desc = "Estimate A1c from average glucose")
    fun estimateA1c(
        event: CommandEvent,
        @Param("Average glucose")
        average: Double,
        @Param("Blood glucose unit (mmol/L, mg/dL)", optional = true)
        @Choices("mmol/L", "mg/dL")
        unit: String?
    ) {
        val result = A1cConverter.estimateA1c(average.toString(), unit)
            .getOrElse {
                event.with().ephemeral(true).reply("Could not estimate A1c: ${it.message}")
                return
            }

        val message = when (result.original.inputUnit) {
            GlucoseUnit.MMOL ->
                String.format(
                    "An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)",
                    result.original.mmol, result.dcct, result.ifcc
                )

            GlucoseUnit.MGDL ->
                String.format(
                    "An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)",
                    result.original.mgdl, result.dcct, result.ifcc
                )

            else -> {
                String.format(
                    "An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC) %n",
                    result.original.original, result.getDcct(GlucoseUnit.MGDL), result.getIfcc(GlucoseUnit.MGDL)
                ) +
                    String.format(
                        "An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)",
                        result.original.original, result.getDcct(GlucoseUnit.MMOL), result.getIfcc(GlucoseUnit.MMOL)
                    )
            }
        }

        event.reply(message)
    }
}
