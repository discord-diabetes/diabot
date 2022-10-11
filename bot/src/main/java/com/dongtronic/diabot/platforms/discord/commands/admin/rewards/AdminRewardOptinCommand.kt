package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminRewardOptinCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "optin"
        this.help = "Opt user in to rewards"
        this.guildOnly = true
        this.aliases = arrayOf("i", "oi")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            if (args.size != 1) {
                throw IllegalArgumentException("UserID is required")
            }

            val user = if (event.message.mentions.members.size == 0) {
                if (!StringUtils.isNumeric(args[0])) {
                    throw IllegalArgumentException("User ID must be valid")
                }

                val userId = args[0]
                event.guild.getMemberById(userId)
                        ?: throw IllegalArgumentException("User `$userId` is not in the server")
            } else {
                event.message.mentions.members[0]
            }

            val guildId = event.guild.id


            RewardsDAO.instance.changeOpt(guildId, user.id, false).subscribe({
                logger.info("User ${user.effectiveName} (${user.id}) opted in to rewards, $it")
                event.reply("User ${user.effectiveName} opted in to rewards")
            }, {
                logger.warn("Error while opting user ${user.effectiveName} (${user.id}) in to rewards", it)
                event.replyError("Could not opt user ${user.effectiveName} in to rewards")
            })
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }


}
