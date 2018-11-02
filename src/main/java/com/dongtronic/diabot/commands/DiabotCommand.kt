package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command

abstract class DiabotCommand : Command() {
    var examples = arrayOfNulls<String>(0)
        protected set

    override fun toString(): String {
        return this.name
    }
}
