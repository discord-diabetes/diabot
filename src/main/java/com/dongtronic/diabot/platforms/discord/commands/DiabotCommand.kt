package com.dongtronic.diabot.platforms.discord.commands

import com.jagrosh.jdautilities.command.Command

abstract class DiabotCommand(category: Command.Category, parent: Command?) : Command() {
    var examples = arrayOfNulls<String>(0)
        protected set

    val parent: Command?

    init {
        this.category = category
        this.parent = parent
    }

    override fun toString(): String {
        return if(this.parent != null) {
            "${this.parent}  $name"
        } else {
            this.name
        }
    }
}
