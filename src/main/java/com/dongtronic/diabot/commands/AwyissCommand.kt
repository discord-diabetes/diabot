package com.dongtronic.diabot.commands

import com.dongtronic.diabot.util.ServerRoles
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.doc.standard.CommandInfo
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

import java.awt.*

class AwyissCommand(category: Command.Category) : DiabotCommand() {

    init {
        this.name = "awyiss"
        this.help = "muther f'in breadcrumbs"
        this.arguments = "<phrase> ..."
        this.guildOnly = true
        this.aliases = arrayOf("duck", "breadcrumbs")
        this.requiredRole = ServerRoles.required
        this.category = category
    }

    override fun execute(event: CommandEvent) {
        event.reactSuccess()

        val url = "http://awyisser.com/api/generator"

        try {
            val client = HttpClient()
            val method = PostMethod(url)

            //Add any parameter if u want to send it with Post req.
            method.addParameter("phrase", event.args)

            val statusCode = client.executeMethod(method)

            if (statusCode == -1) {
                event.reactError()
            }

            val json = method.responseBodyAsString

            val jsonObject = JsonParser().parse(json).asJsonObject
            val imageUrl = jsonObject.get("link").asString

            val builder = EmbedBuilder()

            builder.setTitle("Awyiss - " + event.args)
            builder.setAuthor(event.author.name)
            builder.setImage(imageUrl)
            builder.setColor(Color.white)

            val embed = builder.build()

            event.reply(embed)
        } catch (e: Exception) {
            event.replyError("Something went wrong: " + e.message)
        }


    }
}
