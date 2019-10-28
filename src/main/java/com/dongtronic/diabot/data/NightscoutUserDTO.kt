package com.dongtronic.diabot.data

import com.dongtronic.diabot.commands.nightscout.NightscoutSetDisplayCommand

class NightscoutUserDTO {
    var token: String? = null
    var displayOptions: Array<String> = NightscoutSetDisplayCommand.enabledOptions
    var avatarUrl: String? = null
        set (value) {
            if (value != null) {
                field = value
            }
        }
}