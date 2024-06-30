package com.dongtronic.diabot.data.mongodb

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

data class ChannelDTO(
    val guildId: String,
    val channelId: String,
    val attributes: Set<ChannelAttribute> = emptySet()
) {
    enum class ChannelAttribute {
        @JsonEnumDefaultValue
        UNKNOWN,
        ADMIN,
        NIGHTSCOUT_SHORT
    }
}
