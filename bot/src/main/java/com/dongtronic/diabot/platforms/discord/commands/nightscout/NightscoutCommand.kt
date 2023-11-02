package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.exceptions.NightscoutDataException
import com.dongtronic.diabot.exceptions.NightscoutFetchException
import com.dongtronic.diabot.exceptions.NightscoutPrivateException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.logic.diabetes.BloodGlucoseConverter
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.suspendNameOf
import com.dongtronic.diabot.util.logger
import com.dongtronic.nightscout.Nightscout
import com.dongtronic.nightscout.data.NightscoutDTO
import com.dongtronic.nightscout.exceptions.NoNightscoutDataException
import com.fasterxml.jackson.core.JsonProcessingException
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.onErrorResume
import retrofit2.HttpException
import java.awt.Color
import java.net.UnknownHostException
import java.time.Instant
import java.time.temporal.ChronoUnit

class NightscoutCommand(category: Category) : DiscordCommand(category, null) {

    private val logger = logger()

    init {
        this.name = "nightscout"
        this.help = "Get the most recent info from any Nightscout site"
        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
        this.guildOnly = false
        this.aliases = arrayOf("ns", "bg", "bs")
        this.examples = arrayOf(
                "diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com",
                "diabot ns @SomeUser#1234", "diabot ns public false"
        )
        this.children = arrayOf(
                NightscoutSetUrlCommand(category, this),
                NightscoutDeleteCommand(category, this),
                NightscoutPublicCommand(category, this),
                NightscoutSetTokenCommand(category, this),
                NightscoutSetDisplayCommand(category, this)
        )
    }

    override suspend fun executeSuspend(event: CommandEvent) {
        val args = event.args.trim()
        // grab the necessary data
        try {
            val userDTO = if (args.isBlank()) {
                getStoredData(event)
            } else {
                getUnstoredData(event)
            }

            val nsDTO = fetchNightscoutData(userDTO)

            val embed = buildNightscoutResponse(userDTO, nsDTO, event)

            val message = event.channel.sendMessageEmbeds(embed).await()

            addReactions(nsDTO, message)

            logger.debug("Sent Nightscout embed: {}", message)
        } catch (e: Exception) {
            event.reply(
                    if (e is NightscoutFetchException) {
                        handleGrabError(e.originalException, event.author, e.userDTO)
                    } else {
                        handleError(e)
                    }
            )
        }
    }

    /**
     * Grabs data for the command sender and builds a Nightscout response.
     *
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private suspend fun getStoredData(event: CommandEvent): NightscoutUserDTO {
        return getUserDto(event.author, event.member)
    }

    /**
     * Grabs data for another user/URL (depending on the arguments) and builds a Nightscout response.
     *
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private suspend fun getUnstoredData(event: CommandEvent): NightscoutUserDTO {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val namedMembers = event.event.guild.members.filter {
            it.effectiveName.equals(event.args, true) ||
                    it.user.name.equals(event.args, true)
        }
        val mentionedMembers = event.event.message.mentions.members

        val dto = when {
            mentionedMembers.size > 1 ->
                throw IllegalArgumentException("Too many mentioned users.")

            event.event.message.mentions.mentionsEveryone() ->
                throw IllegalArgumentException("Cannot handle mentioning everyone.")

            mentionedMembers.size == 1 -> {
                val member = mentionedMembers[0]
                val exception = IllegalArgumentException("User does not have a configured Nightscout URL.")
                val dto = getUserDto(member.user, member, exception)
                if (!dto.isNightscoutPublic(event.guild.id)) {
                    throw NightscoutPrivateException(member.effectiveName)
                }

                dto
            }

            args.isNotEmpty() && args[0].matches("^https?://.*".toRegex()) -> {
                // is a URL
                val url = NightscoutFacade.parseNightscoutUrl(args[0]).first
                getDataFromDomain(url, event)
            }

            else -> {
                // Try to get nightscout data from username/nickname, otherwise just try to get from hostname
                val member = namedMembers.getOrNull(0)
                val domain = "https://${args[0]}.herokuapp.com"
                val fallbackDto = NightscoutUserDTO(url = domain)

                if (member != null) {
                    try {
                        val dto = getUserDto(member.user, member)
                        if (!dto.isNightscoutPublic(event.guild.id)) {
                            throw NightscoutPrivateException(member.effectiveName)
                        }
                        return dto
                    } catch (_: UnconfiguredNightscoutException) {
                    }
                }

                fallbackDto
            }
        }

        return dto
    }

    /**
     * Grabs user data for a Nightscout domain.
     *
     * @param domain The domain to look up
     * @param event Command event which called this command
     * @return NS user DTO for this domain. This will be a generic DTO if there was no data found.
     */
    private suspend fun getDataFromDomain(domain: String, event: CommandEvent): NightscoutUserDTO {
        // temporary userDTO for if this domain does not belong to anyone in the guild
        val fallback = NightscoutUserDTO(url = domain)

        val userDTO = getUsersForDomain(domain).firstOrNull() ?: return fallback
        // use generic DTO if the user is not in the guild which we are replying in.
        // this is to prevent users in other guilds from being able to see whether a NS belongs to any diabot user
        val member = event.guild.getMemberById(userDTO.userId) ?: return fallback
        val publicInThisGuild = userDTO.isNightscoutPublic(event.guild.id)
        val user: User = member.user

        if (!publicInThisGuild) {
            throw NightscoutPrivateException(event.suspendNameOf(user))
        }

        return userDTO.copy(jdaUser = user, jdaMember = member)
    }

    /**
     * Loads all the necessary data from a Nightscout instance into a [NightscoutDTO].
     *
     * @param userDTO The Nightscout instance to retrieve data from
     * @return [NightscoutDTO] containing the necessary data for rendering an embed
     */
    private suspend fun fetchNightscoutData(userDTO: NightscoutUserDTO): NightscoutDTO {
        val api = Nightscout(userDTO.apiEndpoint, userDTO.token)
        try {
            var dto = api.getSettings().awaitSingle()
            dto = api.getRecentSgv(dto).awaitSingle()
            dto = api.getPebble(dto).awaitSingle()
            return dto
        } catch (ex: Exception) {
            when (ex) {
                is HttpException,
                is UnknownHostException,
                is JsonProcessingException,
                is NoNightscoutDataException ->
                    throw NightscoutFetchException(userDTO, ex)

                else -> throw ex
            }
        } finally {
            api.close()
        }
    }

    /**
     * Loads all the necessary data from a Nightscout instance and creates an embed of it.
     *
     * @param userDTO User Nightscout settings
     * @param nsDTO Fetched Nightscout data
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private suspend fun buildNightscoutResponse(userDTO: NightscoutUserDTO, nsDTO: NightscoutDTO, event: CommandEvent): MessageEmbed {
        val isShort = if (event.channelType.isGuild) {
            if (userDTO.displayOptions.contains("simple")) {
                true
            } else {
                ChannelDAO.instance.hasAttribute(event.channel.id, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT).awaitSingle()
            }
        } else {
            false
        }

        return buildResponse(nsDTO, userDTO, isShort).build()
    }

    /**
     * Builds an embed from NS data.
     *
     * @param nsDTO The Nightscout data to build a response off of
     * @param userDTO The owner of the [NightscoutDTO] data
     * @param short Whether to exclude the user avatar from this embed
     * @param builder Optional: the embed to build on
     * @return The embed which was created
     */
    private fun buildResponse(
            nsDTO: NightscoutDTO,
            userDTO: NightscoutUserDTO,
            short: Boolean,
            builder: EmbedBuilder = EmbedBuilder()
    ): EmbedBuilder {
        val newest = nsDTO.getNewestEntry()
        val displayOptions = userDTO.displayOptions

        if (displayOptions.contains("title")) builder.setTitle(nsDTO.title)

        val (mmolString: String, mgdlString: String) = buildGlucoseStrings(nsDTO)

        val trendString = newest.trend.unicode
        builder.addField("mmol/L", mmolString, true)
        builder.addField("mg/dL", mgdlString, true)
        if (displayOptions.contains("trend")) builder.addField("trend", trendString, true)
        if (nsDTO.iob != 0.0F && displayOptions.contains("iob")) {
            builder.addField("iob", nsDTO.iob.toString(), true)
        }
        if (nsDTO.cob != 0 && displayOptions.contains("cob")) {
            builder.addField("cob", nsDTO.cob.toString(), true)
        }

        setResponseColor(nsDTO, builder)

        val avatarUrl = userDTO.jdaMember?.effectiveAvatarUrl ?: userDTO.jdaUser?.avatarUrl
        if (avatarUrl != null && displayOptions.contains("avatar") && !short) {
            builder.setThumbnail(avatarUrl)
        }

        builder.setTimestamp(newest.dateTime)
        builder.setFooter("measured", "https://github.com/nightscout/cgm-remote-monitor/raw/master/static/images/large.png")

        if (newest.dateTime.plus(15, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            builder.setDescription("**BG data is more than 15 minutes old**")
        }

        return builder
    }

    /**
     * Builds glucose strings from [NightscoutDTO] into mmol/L and mg/dL values, including delta if there is any.
     *
     * @param dto The Nightscout DTO to read glucose from
     * @return A pair consisting of mmol/L in the first value and mg/dL in the second value
     */
    private fun buildGlucoseStrings(dto: NightscoutDTO): Pair<String, String> {
        val newest = dto.getNewestEntry()
        val mmolString: String
        val mgdlString: String
        if (newest.delta != null) {
            mmolString = buildGlucoseString(newest.glucose.mmol.toString(), newest.delta!!.mmol.toString())
            mgdlString = buildGlucoseString(newest.glucose.mgdl.toString(), newest.delta!!.mgdl.toString())
        } else {
            mmolString = buildGlucoseString(newest.glucose.mmol.toString(), "999.0")
            mgdlString = buildGlucoseString(newest.glucose.mgdl.toString(), "999.0")
        }
        return Pair(mmolString, mgdlString)
    }

    /**
     * Formats a glucose value and delta (if available)
     *
     * @param glucose The glucose value.
     * @param delta The current delta.
     * @return Formatted glucose and delta
     */
    private fun buildGlucoseString(glucose: String, delta: String): String {
        val builder = StringBuilder()

        builder.append(glucose)

        if (delta != "999.0") {
            // 999L is placeholder for absent delta
            builder.append(" (")

            if (!delta.startsWith("-")) {
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
    private suspend fun addReactions(dto: NightscoutDTO, response: Message) {
        BloodGlucoseConverter.getReactions(dto.getNewestEntry().glucose.mmol, dto.getNewestEntry().glucose.mgdl).forEach {
            response.addReaction(Emoji.fromUnicode(it)).await()
        }
    }

    /**
     * Adjust an embed's color based on the current glucose and ranges.
     *
     * @param dto The Nightscout DTO holding the glucose and range data.
     * @param builder Optional: the embed to set colors on.
     */
    private fun setResponseColor(dto: NightscoutDTO, builder: EmbedBuilder = EmbedBuilder()): EmbedBuilder {
        val glucose = dto.getNewestEntry().glucose.mgdl.toDouble()

        if (glucose >= dto.high || glucose <= dto.low) {
            builder.setColor(Color.red)
        } else if (glucoseIsOrange(glucose, dto)) {
            builder.setColor(Color.orange)
        } else {
            builder.setColor(Color.green)
        }

        return builder
    }

    /**
     * Return true if the glucose is in the orange rendering range.
     */
    private fun glucoseIsOrange(glucose: Double, dto: NightscoutDTO): Boolean {
        if (glucose >= dto.top && glucose < dto.high) {
            return true
        }

        if (glucose > dto.low && glucose <= dto.bottom) {
            return true
        }

        return false
    }

    /**
     * Finds users in the database matching with the Nightscout domain given
     *
     * @param domain The domain to look up
     * @return The [NightscoutUserDTO]s which match the given domain
     */
    private fun getUsersForDomain(domain: String): Flow<NightscoutUserDTO> {
        return NightscoutDAO.instance.getUsersForURL(domain)
                // if there are no users then return an empty Flux
                .onErrorResume(NoSuchElementException::class) { Flux.empty() }
                .asFlow()
    }

    /**
     * Gets a [NightscoutUserDTO] for the given user.
     *
     * @param user The user to look up
     * @param member Optional member object for the [user]
     * @param throwable The exception to throw if the user has not configured their Nightscout
     * @return A [NightscoutUserDTO] instance belonging to the given user
     */
    private suspend fun getUserDto(
            user: User,
            member: Member? = null,
            throwable: Throwable = UnconfiguredNightscoutException()
    ): NightscoutUserDTO {
        try {
            val nsUser = NightscoutDAO.instance.getUser(user.id).awaitSingle()
            if (nsUser.url != null) {
                return nsUser.copy(jdaUser = user, jdaMember = member)
            }
        } catch (_: NoSuchElementException) {
        }

        // throw an exception if the url is blank or no user was found
        throw throwable
    }

    companion object {
        private val logger = logger()

        /**
         * Handles errors which occur before attempting to contact the Nightscout instance
         *
         * @param ex The error which was thrown
         * @return Message to be displayed to the executor of the command
         */
        fun handleError(ex: Throwable): String {
            val error = "\uD83D\uDE22"

            val message: String = when (ex) {
                is NightscoutDataException -> {
                    if (ex.message != null) {
                        "$error ${ex.message}"
                    } else {
                        "$error Nightscout data could not be read"
                    }
                }

                is UnconfiguredNightscoutException -> "Please set your Nightscout hostname using `/nightscout set url <hostname>`"
                is IllegalArgumentException -> "Error: ${ex.message}"
                is InsufficientPermissionException -> {
                    logger.info("Couldn't reply with nightscout data due to missing permission: ${ex.permission}")
                    "$error Couldn't perform requested action due to missing permission: `${ex.permission}`"
                }

                else -> {
                    logger.warn("Unexpected error: " + ex.message, ex)
                    "Unexpected error occurred: ${ex.javaClass.simpleName}"
                }
            }

            return message
        }

        /**
         * Handles errors which occur while grabbing Nightscout data.
         *
         * @param ex The [Throwable] which was given
         * @param userDTO The user data which was used for fetching
         * @return Message to be displayed to the executor of the command
         */
        fun handleGrabError(ex: Throwable, author: User, userDTO: NightscoutUserDTO): String {
            // error/crying emoji
            var message = "\uD83D\uDE22 "

            when (ex) {
                is UnknownHostException -> {
                    message += "Could not resolve host"
                    logger.info("No host found: ${ex.message}")
                }

                is NoNightscoutDataException -> {
                    message += "No BG data could be retrieved"
                    logger.info("No nightscout data from ${userDTO.url}")
                }

                is JsonProcessingException -> {
                    message += "Could not parse JSON"
                    logger.warn("Malformed JSON from ${userDTO.url}")
                }

                is HttpException -> {
                    if (ex.code() == 401) {
                        message += if (userDTO.jdaUser != null && userDTO.jdaUser == author) {
                            "Could not authenticate to Nightscout. Please set an authentication token with `/nightscout set token <token>`, " +
                                    "or view our [setup guide](https://github.com/discord-diabetes/diabot/blob/main/docs/nightscout_setup.md)."
                        } else {
                            "Nightscout data is unreadable due to missing token."
                        }
                    } else {
                        message += "Could not connect to Nightscout instance due to HTTP code ${ex.code()}"
                        logger.warn("Connection status ${ex.code()} from ${userDTO.url}")
                    }
                }
            }

            return message
        }
    }
}
