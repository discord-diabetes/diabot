package com.dongtronic.diabot.platforms.discord.commands.nightscout

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.specifier.Greedy
import com.dongtronic.diabot.commands.Category
import com.dongtronic.diabot.commands.annotations.CommandCategory
import com.dongtronic.diabot.commands.annotations.DisplayName
import com.dongtronic.diabot.commands.annotations.Example
import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.exceptions.NightscoutDataException
import com.dongtronic.diabot.exceptions.NightscoutPrivateException
import com.dongtronic.diabot.exceptions.NightscoutStatusException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.JDACommandUser
import com.dongtronic.diabot.submitMono
import com.dongtronic.diabot.util.logger
import com.dongtronic.nightscout.Nightscout
import com.dongtronic.nightscout.data.NightscoutDTO
import com.dongtronic.nightscout.exceptions.NoNightscoutDataException
import com.fasterxml.jackson.core.JsonProcessingException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.onErrorMap
import reactor.kotlin.core.publisher.onErrorResume
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import retrofit2.HttpException
import java.awt.Color
import java.net.UnknownHostException
import java.time.Instant
import java.time.temporal.ChronoUnit

class NightscoutCommand {

    private val logger = logger()

//    init {
//        this.name = "nightscout"
//        this.help = "Get the most recent info from any Nightscout site"
//        this.arguments = "Partial Nightscout url (part before .herokuapp.com)"
//        this.guildOnly = false
//        this.aliases = arrayOf("ns", "bg", "bs")
//        this.examples = arrayOf("diabot nightscout casscout", "diabot ns", "diabot ns set https://casscout.herokuapp.com", "diabot ns @SomeUser#1234", "diabot ns public false")
//        this.children = arrayOf(
//                NightscoutSetUrlCommand(category, this),
//                NightscoutDeleteCommand(category, this),
//                NightscoutPublicCommand(category, this),
//                NightscoutSetTokenCommand(category, this),
//                NightscoutSetDisplayCommand(category, this)
//        )
//    }

    @CommandMethod("nightscout|ns|bg|bs [source]")
    @CommandDescription("Get the most recent info from any Nightscout site")
    @CommandCategory(Category.BG)
    @Example(["[nightscout] casscout", "[ns]", "[ns] set https://casscout.herokuapp.com", "[ns] @SomeUser#1234", "[ns] public false"])
    fun execute(
            e: JDACommandUser,
            @Argument("source", description = "The source of BG data")
            @DisplayName("nightscout URL/herokuapp subdomain/discord user/mention")
            @Greedy
            arg: String?
    ) {
        val event = e.event
        val args = arg?.trim() ?: ""

        // grab the necessary data
        val embed = if (args.isBlank()) {
            // read NS data from the author's NS
            getStoredData(event)
        } else {
            // parse the given arguments
            getUnstoredData(event, args)
        }.flatMap { data ->
            // send the message
            event.channel.sendMessage(data.t2)
                    .submitMono()
                    .doOnSuccess { addReactions(data.t1, it) }
        }.subscribeOn(Schedulers.boundedElastic())

        embed.subscribe({
            logger.debug("Sent Nightscout embed: $it")
        }, {
            handleError(it, event)
        })
    }

    /**
     * Handles errors which occur either:
     * - before fetching data from a Nightscout instance
     * or
     * - when replying
     *
     * @param ex The error which was thrown
     * @param event The command event which called this command
     */
    private fun handleError(ex: Throwable, event: MessageReceivedEvent) {
        when (ex) {
            is NightscoutDataException -> {
                if (ex.message != null) {
                    event.sendMessage(ex.message ?: "")
                } else {
                    event.sendMessage("Nightscout data could not be read")
                }
            }
            is UnconfiguredNightscoutException -> event.sendMessage("Please set your Nightscout hostname using `diabot nightscout set <hostname>`")
            is IllegalArgumentException -> event.sendMessage("Error: " + ex.message)
            is InsufficientPermissionException -> {
                logger.info("Couldn't reply with nightscout data due to missing permission: ${ex.permission}")
                event.sendMessage("Couldn't perform requested action due to missing permission: `${ex.permission}`")
            }
            is UnknownHostException -> {
                event.sendMessage("No NS host could be found")
                logger.info("No host found: ${ex.message}")
            }
            else -> {
                event.sendMessage("Unexpected error")
                logger.warn("Unexpected error: " + ex.message, ex)
            }
        }
    }

    /**
     * Handles errors which occur while grabbing Nightscout data.
     *
     * @param ex The [Throwable] which was given
     * @param event Command event which caused the bot to grab this Nightscout data
     * @param userDTO The user data which was used for fetching
     */
    private fun handleGrabError(ex: Throwable, event: MessageReceivedEvent, userDTO: NightscoutUserDTO) {
        when (ex) {
            is NoNightscoutDataException -> {
                event.sendMessage("No data could be retrieved from Nightscout")
                logger.info("No nightscout data from ${userDTO.url}")
            }
            is JsonProcessingException -> {
                event.sendMessage("Malformed JSON")
                logger.warn("Malformed JSON from ${userDTO.url}")
            }
            is NightscoutStatusException -> {
                if (ex.status == 401) {
                    if (userDTO.jdaUser != null) {
                        if (userDTO.jdaUser == event.author) {
                            event.sendMessage("Could not authenticate to Nightscout. Please set an authentication token with `diabot nightscout token <token>`")
                        } else {
                            event.sendMessage("Nightscout data for ${event.nameOf(userDTO.jdaUser)} is unreadable due to missing token.")
                        }
                    } else {
                        event.sendMessage("Nightscout data is unreadable due to missing token.")
                    }
                } else {
                    event.sendMessage("Could not connect to Nightscout instance.")
                    logger.warn("Connection status ${ex.status} from ${userDTO.url}")
                }
            }
        }
    }

    /**
     * Grabs data for the command sender and builds a Nightscout response.
     *
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private fun getStoredData(event: MessageReceivedEvent): Mono<Tuple2<NightscoutDTO, MessageEmbed>> {
        return getUserDto(event.author)
                .flatMap { buildNightscoutResponse(it, event) }
    }

    /**
     * Grabs data for another user/URL (depending on the arguments) and builds a Nightscout response.
     *
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private fun getUnstoredData(event: MessageReceivedEvent, args: String): Mono<Tuple2<NightscoutDTO, MessageEmbed>> {
        val args1 = args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val namedMembers = event.guild.members.filter {
            it.effectiveName.equals(args, true)
                    || it.user.name.equals(args, true)
        }
        val mentionedMembers = event.message.mentionedMembers

        val endpoint: Mono<NightscoutUserDTO> = when {
            mentionedMembers.size > 1 ->
                IllegalArgumentException("Too many mentioned users.").toMono()
            event.message.mentionsEveryone() ->
                IllegalArgumentException("Cannot handle mentioning everyone.").toMono()

            mentionedMembers.size == 1 -> {
                val user = mentionedMembers[0].user
                val exception = IllegalArgumentException("User does not have a configured Nightscout URL.")

                getUserDto(user, exception)
                        .handle { t, u: SynchronousSink<NightscoutUserDTO> ->
                            if (!t.isNightscoutPublic(event.guild.id)) {
                                u.error(NightscoutPrivateException(event.nameOf(user)))
                            } else {
                                u.next(t)
                            }
                        }
            }
            args1.isNotEmpty() && args1[0].matches("^https?://.*".toRegex()) -> {
                // is a URL
                val url = NightscoutSetUrlCommand.validateNightscoutUrl(args1[0])
                getDataFromDomain(url, event)
            }
            else -> {
                // Try to get nightscout data from username/nickname, otherwise just try to get from hostname
                val user = namedMembers.getOrNull(0)?.user
                val domain = "https://${args1[0]}.herokuapp.com"
                val fallbackDto = NightscoutUserDTO(url = domain).toMono()

                if (user == null) {
                    fallbackDto
                } else {
                    getUserDto(user)
                            .switchIfEmpty { fallbackDto }
                            .handle { userDTO, sink: SynchronousSink<NightscoutUserDTO> ->
                                if (!userDTO.isNightscoutPublic(event.guild.id)) {
                                    sink.error(NightscoutPrivateException(event.nameOf(user)))
                                } else {
                                    sink.next(userDTO)
                                }
                            }
                }
            }
        }

        return endpoint.flatMap { buildNightscoutResponse(it, event) }
    }

    /**
     * Grabs user data for a Nightscout domain.
     *
     * @param domain The domain to look up
     * @param event Command event which called this command
     * @return NS user DTO for this domain. This will be a generic DTO if there was no data found.
     */
    private fun getDataFromDomain(domain: String, event: MessageReceivedEvent): Mono<NightscoutUserDTO> {
        val userDtos = getUsersForDomain(domain)
        // temporary userDTO for if this domain does not belong to anyone in the guild
        val fallback = NightscoutUserDTO(url = domain).toMono()

        return userDtos
                .flatMap { userDTO ->
                    val member = event.guild.getMemberById(userDTO.userId)
                    val user: User
                    val publicInThisGuild = userDTO.isNightscoutPublic(event.guild.id)

                    if (member == null) {
                        // use generic DTO if the user is not in the guild which we are replying in.
                        // this is to prevent users in other guilds from being able to see whether a NS belongs to any diabot user
                        return@flatMap fallback
                    }

                    user = member.user

                    if (!publicInThisGuild) {
                        return@flatMap NightscoutPrivateException(event.nameOf(user))
                                .toMono<NightscoutUserDTO>()
                    }

                    userDTO.copy(jdaUser = user).toMono()
                }
                .singleOrEmpty()
                .switchIfEmpty { fallback }
    }

    /**
     * Loads all the necessary data from a Nightscout instance and creates an embed of it.
     *
     * @param userDTO Data necessary for loading/rendering
     * @param event Command event which called this command
     * @return A nightscout DTO and an embed based on it
     */
    private fun buildNightscoutResponse(userDTO: NightscoutUserDTO, event: MessageReceivedEvent): Mono<Tuple2<NightscoutDTO, MessageEmbed>> {
        val api = Nightscout(userDTO.apiEndpoint, userDTO.token)
        return Mono.from(
                api.getSettings()
                        .flatMap { api.getRecentSgv(it) }
                        .flatMap { api.getPebble(it) }
        ).doFinally {
            api.close()
        }.onErrorMap(HttpException::class) {
            NightscoutStatusException(it.code())
        }.onErrorResume({ error ->
            error is NightscoutStatusException
                    || error is JsonProcessingException
                    || error is NoNightscoutDataException
        }, {
            // fallback to `handleGrabError` if the error is any of the above
            handleGrabError(it, event, userDTO)
            Mono.empty<NightscoutDTO>()
        }).zipWhen { nsDto ->
            // attach a message embed to the NightscoutDTO
            val channelType = event.channelType
            var isShort = false.toMono()
            if (channelType == ChannelType.TEXT) {
                isShort = if (userDTO.displayOptions.contains("simple")) {
                    true.toMono()
                } else {
                    ChannelDAO.instance.hasAttribute(event.channel.id, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT)
                }
            }

            isShort.map { buildResponse(nsDto, userDTO, it).build() }
        }
    }

    /**
     * Builds an embed from NS data.
     *
     * @param nsDTO The Nightscout data to build a response off of
     * @param userDTO The owner of the [NightscoutDTO] data
     * @param short Whether to exclude the user avatar from this embed
     * @param builder Optional: the embed to build off of
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

        if (userDTO.jdaUser?.avatarUrl != null && displayOptions.contains("avatar") && !short) {
            builder.setThumbnail(userDTO.jdaUser.avatarUrl)
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
    private fun addReactions(dto: NightscoutDTO, response: Message) {
        val newest = dto.getNewestEntry()
        // #20: Reply with :smirk: when value is 69 mg/dL or 6.9 mmol/L
        if (newest.glucose.mgdl == 69 || newest.glucose.mmol == 6.9) {
            response.addReaction("\uD83D\uDE0F").queue()
        }
        // #36 and #60: Reply with :100: when value is 100 mg/dL, 5.5 mmol/L, or 10.0 mmol/L
        if (newest.glucose.mgdl == 100
                || newest.glucose.mmol == 5.5
                || newest.glucose.mmol == 10.0) {
            response.addReaction("\uD83D\uDCAF").queue()
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
        } else if (glucose >= dto.top && glucose < dto.high || glucose > dto.low && glucose <= dto.bottom) {
            builder.setColor(Color.orange)
        } else {
            builder.setColor(Color.green)
        }

        return builder
    }

    /**
     * Finds users in the database matching with the Nightscout domain given
     *
     * @param domain The domain to look up
     * @return The [NightscoutUserDTO]s which match the given domain
     */
    private fun getUsersForDomain(domain: String): Flux<NightscoutUserDTO> {
        return NightscoutDAO.instance.getUsersForURL(domain)
                // if there are no users then return an empty Flux
                .onErrorResume(NoSuchElementException::class) { Flux.empty() }
    }

    /**
     * Gets a [NightscoutUserDTO] for the given user.
     *
     * @param user The user to look up
     * @param throwable The exception to throw if the user has not configured their Nightscout
     * @return A [NightscoutUserDTO] instance belonging to the given user
     */
    private fun getUserDto(user: User, throwable: Throwable = UnconfiguredNightscoutException()): Mono<NightscoutUserDTO> {
        return NightscoutDAO.instance.getUser(user.id)
                .onErrorMap(NoSuchElementException::class) { throwable }
                .flatMap {
                    if (it.url != null) {
                        it.copy(jdaUser = user).toMono()
                    } else {
                        // throw an exception if the url is blank
                        throwable.toMono()
                    }
                }
    }
}

private fun MessageReceivedEvent.sendMessage(message: String) {
    channel.sendMessage(message).queue()
}
