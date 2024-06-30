package com.dongtronic.diabot.platforms.discord.commands.diabetes

import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.logic.diabetes.GlucoseUnit
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class ConversionApplicationCommand : ApplicationCommand {
    private val commandArgGlucose = "glucose"
    private val commandArgUnit = "unit"

    override val commandName: String = "convert"

    override fun config(): CommandData {
        return Commands.slash(commandName, "Convert blood glucose values between mmol/L and mg/dL")
            .addOption(OptionType.NUMBER, commandArgGlucose, "Blood glucose level", true)
            .addOptions(
                OptionData(OptionType.STRING, commandArgUnit, "Blood glucose unit (mmol/L, mg/dL)")
                    .addChoice("mmol/L", "mmol/L")
                    .addChoice("mg/dL", "mg/dL")
            )
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val glucoseNumber = event.getOption(commandArgGlucose)!!.asString
        val glucoseUnit = event.getOption(commandArgUnit)?.asString

        val result = BloodGlucoseConverter.convert(glucoseNumber, glucoseUnit)
            .getOrElse {
                if (it is IllegalArgumentException) {
                    event.reply("Could not convert: ${it.message}").queue()
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
                    glucoseNumber, result.mmol, glucoseNumber, result.mgdl
                )
            }
        }

        event.reply(reply).queue()
    }
}
