package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminRewardOptoutCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "optout"
        this.help = "Opt user out of rewards"
        this.guildOnly = true
        this.aliases = arrayOf("o", "oo")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            if (args.size != 1) {
                throw IllegalArgumentException("UserID is required")
            }

            if (!StringUtils.isNumeric(args[0])) {
                throw IllegalArgumentException("UserID must be numeric")
            }

            val userId = args[0]
            val guildId = event.guild.id

            val user = event.guild.getMemberById(userId)?.user
                    ?: throw IllegalArgumentException("User `$userId` does not exist")

            RewardsDAO.instance.changeOpt(guildId, user.id, true).subscribe({
                logger.info("User ${user.name}#${user.discriminator} (${user.id}) opted out of rewards, $it")
                event.reply("User ${event.nameOf(user)} opted out of rewards")
            }, {
                logger.warn("Error while opting user ${user.name}#${user.discriminator} (${user.id}) out of rewards", it)
                event.replyError("Could not opt user ${event.nameOf(user)} out of rewards")
            })
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }


}
