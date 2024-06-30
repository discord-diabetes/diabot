package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.nutrition.NutritionixCommunicator
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope
import net.dv8tion.jda.api.EmbedBuilder
import reactor.core.scheduler.Schedulers

class NutritionCommand(category: Category) : DiscordCommand(category, null) {

    val logger = logger()

    init {
        this.name = "nutrition"
        this.aliases = arrayOf("nutriets")
        this.help = "Get nutrition information"
        this.examples = arrayOf("diabot nutrition one slice of bread", "diabot nutrition 1 slices brown bread with jam, 1 cup milk, 2 cups green salad")
        this.hidden = false
        this.guildOnly = false
        this.cooldown = 30
        this.cooldownScope = CooldownScope.USER
    }

    override fun execute(event: CommandEvent) {

        if (event.args.isEmpty()) {
            event.replyError("Please include a (list of) food item(s)")
        }

        val input = event.args

        NutritionixCommunicator.getNutritionInfo(input)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe({ result ->
                val builder = EmbedBuilder()

                builder.setTitle("Nutrition Result")

                builder.appendDescription("**Input**: $input \n")
                builder.appendDescription("**Parsed**: \n$result")

                if (result.totalCarbs.toInt() != 0) {
                    builder.addField("Carbs", result.totalCarbs.toInt().toString(), true)
                }

                if (result.totalFats.toInt() != 0) {
                    builder.addField("Total Fats", result.totalFats.toInt().toString(), true)
                }

                if (result.totalSaturatedFats.toInt() != 0) {
                    builder.addField("Saturated fats", result.totalSaturatedFats.toInt().toString(), true)
                }

                if (result.totalFibers.toInt() != 0) {
                    builder.addField("Dietary fibers", result.totalFibers.toInt().toString(), true)
                }

                builder.setColor(java.awt.Color.blue)
                builder.setAuthor("Powered by Nutritionix")

                event.reply(builder.build())
            }, {
                logger.warn("Could not communicate with Nutritionix", it)
                if (it is RequestStatusException) {
                    when (it.status) {
                        404 -> {
                            event.replyError("Couldn't find any food matching your request in the food database")
                        }

                        401 -> {
                            event.replyError("Rate limit reached, please try again tomorrow")
                        }

                        else -> {
                            event.replyError("Couldn't communicate with nutrition database. Please try again later.")
                        }
                    }
                } else {
                    event.replyError("An error occurred while grabbing nutrition data. Please try again later.")
                }
            })
    }
}
