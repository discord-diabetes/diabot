package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.nutrition.NutritionixCommunicator
import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory

class NutritionCommand(category: Category) : DiabotCommand(category, null) {

    val logger = LoggerFactory.getLogger(NutritionCommand::class.java)

    init {
        this.name = "nutrition"
        this.help = "Get nutrition information"
        this.hidden = false
        this.guildOnly = false
    }

    override fun execute(event: CommandEvent) {

        if (event.args.isEmpty()) {
            event.replyError("Please include a (list of) food item(s)")
        }

        val input = event.args

        try {
            val result = NutritionixCommunicator.getNutritionInfo(input)

            val builder = EmbedBuilder()

            builder.setTitle("Nutrition Result")

            builder.appendDescription("**Input**: $input \n")
            builder.appendDescription("**Parsed**: \n$result")

            if (result.totalCarbs != 0.0) {
                builder.addField("Carbs", result.totalCarbs.toInt().toString(), true)
            }

            if (result.totalFats != 0.0) {
                builder.addField("Total Fats", result.totalFats.toInt().toString(), true)
            }

            if (result.totalSaturatedFats != 0.0) {
                builder.addField("Saturated fats", result.totalSaturatedFats.toInt().toString(), true)
            }

            if (result.totalFibers != 0.0) {
                builder.addField("Dietary fibers", result.totalFibers.toInt().toString(), true)
            }

            builder.setColor(java.awt.Color.blue)
            builder.setAuthor("Powered by Nutritionix")

            event.reply(builder.build())
        } catch (ex: RequestStatusException) {
            if (ex.status == 404) {
                event.replyError("Couldn't find any food matching your request in the food database")
            } else {
                event.replyError("Couldn't communicate with nutrition database. Please try again later.")
            }
        }
    }
}
