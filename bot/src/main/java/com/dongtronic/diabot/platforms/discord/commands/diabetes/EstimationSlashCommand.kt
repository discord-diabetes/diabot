package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.logic.diabetes.A1cConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.SlashCommand
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_A1C
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_AVG
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_ARG_UNIT
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_MODE_A1C
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_MODE_AVG
import com.dongtronic.diabot.util.CommandValues.ESTIMATE_COMMAND_NAME
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class EstimationSlashCommand : SlashCommand() {
    override val commandName: String = ESTIMATE_COMMAND_NAME

    override fun execute(event: SlashCommandEvent) {
        when (event.subcommandName) {
            ESTIMATE_COMMAND_MODE_AVG -> estimateAverage(event)
            ESTIMATE_COMMAND_MODE_A1C -> estimateA1c(event)
        }
    }

    private fun estimateAverage(event: SlashCommandEvent) {
        val input = event.getOption(ESTIMATE_COMMAND_ARG_A1C)!!

        val result = A1cConverter.estimateAverage(input.asString)

        event.reply(
                String.format("An A1c of **%s%%** (DCCT) or **%s mmol/mol** (IFCC) is about **%s mg/dL** or **%s mmol/L**",
                        result.dcct, result.ifcc, result.original.mgdl, result.original.mmol)
        ).queue()
    }

    private fun estimateA1c(event: SlashCommandEvent) {
        val inputNumber = event.getOption(ESTIMATE_COMMAND_ARG_AVG)!!
        val inputUnit = event.getOption(ESTIMATE_COMMAND_ARG_UNIT)

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