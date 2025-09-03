package com.dongtronic.diabot.data.redis

import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutSetDisplayCommand

@Deprecated(level = DeprecationLevel.WARNING, message = "Support for Redis will be removed in Diabot version 2")
class NightscoutUserDTO {
    var token: String? = null
    var displayOptions: Array<String> = NightscoutSetDisplayCommand.enabledOptions
    var avatarUrl: String? = null
        set(value) {
            if (value != null) {
                field = value
            }
        }
}
