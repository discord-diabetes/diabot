package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.logic.diabetes.A1cConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class EstimationApplicationCommand : ApplicationCommand {
    private val commandModeA1c = "a1c"
    private val commandModeAverage = "average"
    private val commandArgA1c = "a1c"
    private val commandArgUnit = "unit"
    private val commandArgAvg = "average"

    override val commandName: String = "estimate"

    override fun config(): CommandData {
        return CommandData(commandName, "Perform A1c and average glucose estimations").addSubcommands(
                SubcommandData(commandModeA1c, "Estimate A1c from average glucose")
                        .addOption(OptionType.NUMBER, commandArgAvg, "Average glucose", true)
                        .addOptions(OptionData(OptionType.STRING, commandArgUnit, "Blood glucose unit (mmol/L, mg/dL)")
                                .addChoice("mmol/L", "mmol/L")
                                .addChoice("mg/dL", "mg/dL")),
                SubcommandData(commandModeAverage, "Estimate average glucose from A1c")
                        .addOption(OptionType.NUMBER, commandArgA1c, "A1c value", true)
        )
    }

    override fun execute(event: SlashCommandEvent) {
        when (event.subcommandName) {
            commandModeAverage -> estimateAverage(event)
            commandModeA1c -> estimateA1c(event)
        }
    }

    private fun estimateAverage(event: SlashCommandEvent) {
        val input = event.getOption(commandArgA1c)!!

        val result = A1cConverter.estimateAverage(input.asString)

        event.reply(
                String.format("An A1c of **%s%%** (DCCT) or **%s mmol/mol** (IFCC) is about **%s mg/dL** or **%s mmol/L**",
                        result.dcct, result.ifcc, result.original.mgdl, result.original.mmol)
        ).queue()
    }

    private fun estimateA1c(event: SlashCommandEvent) {
        val inputNumber = event.getOption(commandArgAvg)!!
        val inputUnit = event.getOption(commandArgUnit)

        val result = A1cConverter.estimateA1c(inputNumber.asString, inputUnit?.asString)

        val message = when (result.original.inputUnit) {
            GlucoseUnit.MMOL -> String.format("An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.original.mmol, result.dcct, result.ifcc)
            GlucoseUnit.MGDL -> String.format("An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.original.mgdl, result.dcct, result.ifcc)
            else -> {
                String.format("An average of %s mmol/L is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC) %n", result.original.original, result.getDcct(GlucoseUnit.MGDL), result.getIfcc(GlucoseUnit.MGDL)) + String.format("An average of %s mg/dL is about **%s%%** (DCCT) or **%s mmol/mol** (IFCC)", result.original.original, result.getDcct(GlucoseUnit.MMOL), result.getIfcc(GlucoseUnit.MMOL))
            }
        }

        event.reply(message).queue()
    }
}
