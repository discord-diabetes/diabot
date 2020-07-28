package com.dongtronic.diabot.util

object RedisKeyFormats {
    // Nightscout
    const val nightscoutUrlFormat = "{{userid}}:nightscouturl"
    const val nightscoutPublicFormat = "{{userid}}:{{guildid}}:nightscoutpublic"
    const val nightscoutTokenFormat = "{{userid}}:nightscouttoken"
    const val nightscoutDisplayFormat = "{{userid}}:nightscoutdisplay"
    const val allNightscoutUrlsFormat = "*:nightscouturl"
    const val nightscoutShortChannelsFormat = "{{guildid}}:nightscoutshortchannels"

    // Admin
    const val adminChannelIds = "{{guildid}}:adminchannels"

    // Rewards
    const val simpleRewards = "{{guildid}}:simplerewards"
    const val rewardOptout = "{{guildid}}:rewardoptouts"

    // Usernames
    const val usernamePattern = "{{guildid}}:usernamepattern"
    const val enforceUsernames = "{{guildid}}:enforceusernames"
    const val usernameHint = "{{guildid}}:usernamehint"

    // Rules
    const val ruleIds = "{{guildid}}:rules"
    const val rulesChannel = "{{guildid}}:ruleschannel"
    const val ruleText = "{{guildid}}:rules:{{ruleid}}:text"
    const val ruleTitle = "{{guildid}}:rules:{{ruleid}}:title"
    const val ruleMessage = "{{guildid}}:rules:{{ruleid}}:message"

    // Quotes
    const val quoteIds = "{{guildid}}:quotes"
    const val quoteIndex = "{{guildid}}:quotes:index"
    const val quoteAuthor = "{{guildid}}:quotes:{{quoteid}}:author"
    const val quoteAuthorId = "{{guildid}}:quotes:{{quoteid}}:authorid"
    const val quoteMessage = "{{guildid}}:quotes:{{quoteid}}:message"
    const val quoteMessageId = "{{guildid}}:quotes:{{quoteid}}:messageid"
    const val quoteTime = "{{guildid}}:quotes:{{quoteid}}:time"

    // Project info
    const val infoList = "info:projects"
    const val infoText = "info:{{project}}"
}
