package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.data.redis.NightscoutDTO
import com.dongtronic.diabot.exceptions.NightscoutStatusException
import com.dongtronic.diabot.exceptions.NoNightscoutDataException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.logic.nightscout.NightscoutCommunicator.getEntries
import com.dongtronic.diabot.logic.nightscout.NightscoutCommunicator.getSettings
import com.dongtronic.diabot.logic.nightscout.NightscoutCommunicator.needsNightscoutToken
import com.dongtronic.diabot.logic.nightscout.NightscoutCommunicator.processPebble
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.google.gson.stream.MalformedJsonException
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.awt.Color
import java.net.UnknownHostException
import java.time.ZonedDateTime

class NightscoutCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val logger = logger()
    private val trendArrows: Array<String> = arrayOf("", "↟", "↑", "↗", "→", "↘", "↓", "↡", "↮", "↺")

    init {
        this.name = "nightscout"
        this.help = "Get the most recent info from any Nightscout site"
        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
        this.guildOnly = false
        this.aliases = arrayOf("ns", "bg", "bs")
        this.examples = arrayOf("diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com", "diabot ns public false")
        this.children = arrayOf(
                NightscoutSetUrlCommand(category, this),
                NightscoutDeleteCommand(category, this),
                NightscoutPublicCommand(category, this),
                NightscoutSetTokenCommand(category, this),
                NightscoutSetDisplayCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {

        val args = event.args.trim()

        try {
            if (args.isBlank()) {
                getStoredData(event)
            } else {
                getUnstoredData(event)
            }
        } catch (ex: UnconfiguredNightscoutException) {
            event.reply("Please set your Nightscout hostname using `diabot nightscout set <hostname>`")
        } catch (ex: IllegalArgumentException) {
            event.reply("Error: " + ex.message)
        } catch (ex: InsufficientPermissionException) {
            logger.info("Couldn't reply with nightscout data due to missing permission: ${ex.permission}")
            event.replyError("Couldn't perform requested action due to missing permission: `${ex.permission}`")
        } catch (ex: UnknownHostException) {
            event.reactError()
            logger.info("No host found: ${ex.message}")
        } catch (ex: Exception) {
            event.reactError()
            logger.warn("Unexpected error: " + ex.message)
        }

    }

    /**
     * Grabs data for the command sender and builds a Nightscout response.
     *
     * @param event [CommandEvent]
     */
    private fun getStoredData(event: CommandEvent) {
        val userDTO = getUserDto(event.author)
        buildNightscoutResponse(userDTO, event)
    }

    /**
     * Grabs data for another user/URL (depending on the arguments) and builds a Nightscout response.
     *
     * @param event [CommandEvent]
     */
    private fun getUnstoredData(event: CommandEvent) {
        // todo
//        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        val userDTO = NightscoutUserDTO()
//        val namedMembers = event.event.guild.getMembersByName(args[0], true) + event.event.guild.getMembersByNickname(args[0], true)
//        val mentionedMembers = event.event.message.mentionedMembers
//
//        val endpoint = when {
//            mentionedMembers.size == 1 -> {
//                val user = mentionedMembers[0].user
//                try {
//                    if (!getNightscoutPublic(user, event.guild.id)) {
//                        event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is private")
//                        return
//                    }
//                    getUserDto(user, userDTO)
//                    getNightscoutHost(user) + "/api/v1/"
//                } catch (ex: UnconfiguredNightscoutException) {
//                    throw IllegalArgumentException("User does not have a configured Nightscout URL.")
//                }
//            }
//            mentionedMembers.size > 1 -> throw IllegalArgumentException("Too many mentioned users.")
//            event.event.message.mentionsEveryone() -> throw IllegalArgumentException("Cannot handle mentioning everyone.")
//
//            else -> {
//
//                val hostname = args[0]
//                if (hostname.contains("http://") || hostname.contains("https://")) {
//                    var domain = hostname
//                    if (domain.endsWith("/")) {
//                        domain = domain.trimEnd('/')
//                    }
//
//                    // If Nightscout data cannot be retrieved from this domain then stop
//                    if (!getUnstoredDataForDomain(domain, event, userDTO))
//                        return
//
//                    "$domain/api/v1/"
//                }
//                // Try to get nightscout data from username/nickname, otherwise just try to get from hostname
//
//                else if (namedMembers.size == 1) {
//                    val user = namedMembers[0].user
//                    val domain = "https://$hostname.herokuapp.com"
//
//                    when {
//                        getNightscoutPublic(user, event.guild.id) -> {
//                            getUserDto(user, userDTO)
//                            getNightscoutHost(user) + "/api/v1/"
//
//                        }
//                        isNightscoutInstance(domain) -> {
//                            "$domain/api/v1/"
//                        }
//                        else -> {
//                            event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is private")
//                            return
//                        }
//                    }
//
//                } else {
//                    "https://$hostname.herokuapp.com/api/v1/"
//                }
//            }
//        }
//
//        buildNightscoutResponse(endpoint, userDTO, event)
    }

    /**
     * Gets token, display options, and an avatar URL for the domain given.
     * Replies with an error and returns false if no user is found, nightscout data is private, or if no token is found.
     *
     * @return if data can be retrieved from the given domain
     */
    private fun getUnstoredDataForDomain(domain: String, event: CommandEvent): Boolean {
        // Test if the Nightscout hosted at the given domain requires a token
        // If a token is not needed, skip grabbing data
        if (needsNightscoutToken(domain)) {
            // Get user ID from domain. If no user is found, respond with an error
            val userDtos = getUsersForDomain(domain)
            val userDto: NightscoutUserDTO?
            val user: User?

            userDtos?.mapNotNull {
                val foundUser = event.jda.getUserById(it.userId)
                if (foundUser != null) it to foundUser else null
            }?.firstOrNull {
                it.second.mutualGuilds.contains(event.guild)
                        && it.first.isNightscoutPublic(event.guild.idLong)
            }.let {
                userDto = it?.first
                user = it?.second
            }

            if (userDto == null) {
                event.replyError("Token secured Nightscout does not belong to any user.")
                return false
            }

            if (user == null) {
                event.replyError("Couldn't find user ${userDto.userId}")
                return false
            }

            if (!userDto.isNightscoutPublic(event.guild.idLong)) {
                // Nightscout data is private
                event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is private.")
                return false
            }

            if (userDto.token == null) {
                // No token found
                event.replyError("Nightscout data for ${NicknameUtils.determineDisplayName(event, user)} is unreadable due to missing token.")
                return false
            }

            getUserDto(user)
        }
        return true
    }

    /**
     * Loads all the necessary data from a Nightscout instance and replies with an embed of it.
     *
     * @param userDTO Data necessary for loading/rendering
     * @param event [CommandEvent]
     */
    private fun buildNightscoutResponse(userDTO: NightscoutUserDTO, event: CommandEvent) {
        val nsDto = NightscoutDTO()

        try {
            getSettings(userDTO.apiEndpoint, userDTO.token, nsDto)
            getEntries(userDTO.apiEndpoint, userDTO.token, nsDto)
            processPebble(userDTO.apiEndpoint, userDTO.token, nsDto)
        } catch (exception: NoNightscoutDataException) {
            event.reactError()
            logger.info("No nightscout data from ${userDTO.url}")
            return
        } catch (exception: MalformedJsonException) {
            event.reactError()
            logger.warn("Malformed JSON from ${userDTO.url}")
            return
        } catch (exception: NightscoutStatusException) {
            if (exception.status == 401) {
                event.replyError("Could not authenticate to Nightscout. Please set an authentication token with `diabot nightscout token <token>`")
            } else {
                event.reactError()
                logger.warn("Connection status ${exception.status} from ${userDTO.url}")
            }

            return
        }

        val channelType = event.channelType
        var shortReply = false
        if (channelType == ChannelType.TEXT) {
            // todo
//            shortReply = NightscoutDAO.getInstance().listShortChannels(event.guild.id).contains(event.channel.id) ||
//                    userDTO.displayOptions.contains("simple")
        }

        val builder = EmbedBuilder()

        buildResponse(nsDto, userDTO.jdaUser?.avatarUrl, userDTO.displayOptions, shortReply, builder)

        val embed = builder.build()
        event.reply(embed) { replyMessage -> addReactions(nsDto, replyMessage) }
    }

    private fun buildResponse(dto: NightscoutDTO, avatarUrl: String?, displayOptions: List<String>, short: Boolean, builder: EmbedBuilder) {
        if (displayOptions.contains("title")) builder.setTitle(dto.title)

        val (mmolString: String, mgdlString: String) = buildGlucoseStrings(dto)

        val trendString = trendArrows[dto.trend]
        builder.addField("mmol/L", mmolString, true)
        builder.addField("mg/dL", mgdlString, true)
        if (displayOptions.contains("trend")) builder.addField("trend", trendString, true)
        if (dto.iob != 0.0F && displayOptions.contains("iob")) {
            builder.addField("iob", dto.iob.toString(), true)
        }
        if (dto.cob != 0 && displayOptions.contains("cob")) {
            builder.addField("cob", dto.cob.toString(), true)
        }

        setResponseColor(dto, builder)

        if (avatarUrl != null && displayOptions.contains("avatar") && !short) {
            builder.setThumbnail(avatarUrl)
        }

        builder.setTimestamp(dto.dateTime)
        builder.setFooter("measured", "https://github.com/nightscout/cgm-remote-monitor/raw/master/static/images/large.png")

        if (dto.dateTime!!.plusMinutes(15).toInstant().isBefore(ZonedDateTime.now().toInstant())) {
            builder.setDescription("**BG data is more than 15 minutes old**")
        }
    }

    private fun buildGlucoseStrings(dto: NightscoutDTO): Pair<String, String> {
        val mmolString: String
        val mgdlString: String
        if (dto.delta != null) {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), dto.delta!!.mmol.toString(), dto.deltaIsNegative)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), dto.delta!!.mgdl.toString(), dto.deltaIsNegative)
        } else {
            mmolString = buildGlucoseString(dto.glucose!!.mmol.toString(), "999.0", false)
            mgdlString = buildGlucoseString(dto.glucose!!.mgdl.toString(), "999.0", false)
        }
        return Pair(mmolString, mgdlString)
    }

    /**
     * Formats a glucose value and delta (if available)
     *
     * @param glucose The glucose value.
     * @param delta The current delta.
     * @param negative Whether the delta is falling.
     * @return Formatted glucose and delta
     */
    private fun buildGlucoseString(glucose: String, delta: String, negative: Boolean): String {
        val builder = StringBuilder()

        builder.append(glucose)

        if (delta != "999.0") {
            // 999L is placeholder for absent delta
            builder.append(" (")

            if (negative) {
                builder.append("-")
            } else {
                builder.append("+")
            }

            builder.append(delta)
            builder.append(")")
        }

        return builder.toString()
    }

    /**
     * Adds reactions to a message based on the glucose value.
     *
     * @param dto The Nightscout DTO holding the glucose data.
     * @param response The message to react to.
     */
    private fun addReactions(dto: NightscoutDTO, response: Message) {
        // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
        if (dto.glucose!!.mgdl == 69 || dto.glucose!!.mmol == 6.9) {
            response.addReaction("\uD83D\uDE0F").queue()
        }
        // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
        if (dto.glucose!!.mgdl == 100
                || dto.glucose!!.mmol == 5.5
                || dto.glucose!!.mmol == 10.0) {
            response.addReaction("\uD83D\uDCAF").queue()
        }
    }

    /**
     * Adjust an embed's color based on the current glucose and ranges.
     *
     * @param dto The Nightscout DTO holding the glucose and range data.
     * @param builder The embed to set colors on.
     */
    private fun setResponseColor(dto: NightscoutDTO, builder: EmbedBuilder) {
        val glucose = dto.glucose!!.mgdl.toDouble()

        if (glucose >= dto.high || glucose <= dto.low) {
            builder.setColor(Color.red)
        } else if (glucose >= dto.top && glucose < dto.high || glucose > dto.low && glucose <= dto.bottom) {
            builder.setColor(Color.orange)
        } else {
            builder.setColor(Color.green)
        }
    }

    /**
     * Finds users in the database matching with the Nightscout domain given
     */
    private fun getUsersForDomain(domain: String): MutableList<NightscoutUserDTO>? {
        // todo

        // If no users are found with the given domain, return null
        return NightscoutDAO.instance.getUsersForURL(domain).collectList().block()
    }

    /**
     * Sets the data inside the given NightscoutUserDTO for the given user
     */
    private fun getUserDto(user: User): NightscoutUserDTO {
        val dto = NightscoutDAO.instance.getUser(user.idLong).block()!!
        return dto.copy(jdaUser = user)
    }
}
