package com.dongtronic.diabot.platforms.discord.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class DiscordCommand(category: Category, parent: Command?) : Command() {
    var examples = arrayOfNulls<String>(0)
        protected set

    val parent: Command?

    init {
        this.category = category
        this.parent = parent
    }

    override fun execute(event: CommandEvent) {
        // The `.injectKTX()` line in the main class changes the event manager to a CoroutineEventManager
        val manager = event.jda.eventManager as? CoroutineEventManager
        if (manager == null) {
            // fallback to blocking if the event manager isn't a CoroutineEventManager for some reason
            runBlocking {
                launch {
                    executeSuspend(event)
                }
            }
        } else {
            manager.launch {
                executeSuspend(event)
            }
        }
    }

    open suspend fun executeSuspend(event: CommandEvent) {}

    override fun toString(): String {
        return if (this.parent != null) {
            "${this.parent}  $name"
        } else {
            this.name
        }
    }
}
