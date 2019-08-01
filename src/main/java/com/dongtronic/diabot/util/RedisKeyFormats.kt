package com.dongtronic.diabot.util

object RedisKeyFormats {
    const val nightscoutUrlFormat = "{{userid}}:nightscouturl"
    const val nightscoutPublicFormat = "{{userid}}:nightscoutprivate"
    const val nightscoutTokenFormat = "{{userid}}:nightscouttoken"
    const val nightscoutDisplayFormat = "{{userid}}:nightscoutdisplay"
    const val allNightscoutUrlsFormat = "*:nightscouturl"
    const val adminChannelIds = "{{guildid}}:adminchannels"
    const val simpleRewards = "{{guildid}}:simplerewards"
    const val rewardOptout = "{{guildid}}:rewardoptouts"
    const val usernamePattern = "{{guildid}}:usernamepattern"
    const val enforceUsernames = "{{guildid}}:enforceusernames"
    const val usernameHint = "{{guildid}}:usernamehint"

    const val ruleIds = "{{guildid}}:rules"
    const val rulesChannel = "{{guildid}}:ruleschannel"
    const val ruleText = "{{guildid}}:rules:{{ruleid}}:text"
    const val ruleTitle = "{{guildid}}:rules:{{ruleid}}:title"
    const val ruleMessage = "{{guildid}}:rules:{{ruleid}}:message"
}
