package com.dongtronic.diabot.util

object RedisKeyFormats {
    const val nightscoutUrlFormat = "{{userid}}:nightscouturl"
    const val allNightscoutUrlsFormat = "*:nightscouturl"
    const val adminChannelIds = "{{guildid}}:adminchannels"
    const val simpleRewards = "{{guildid}}:simplerewards"
    const val rewardOptout = "{{guildid}}:rewardoptouts"
    const val usernamePattern = "{{guildid}}:usernamepattern"
    const val enforceUsernames = "{{guildid}}:enforceusernames"
    const val usernameHint = "{{guildid}}:usernamehint"
}
