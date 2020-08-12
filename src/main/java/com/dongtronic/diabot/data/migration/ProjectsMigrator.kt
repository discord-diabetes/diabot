package com.dongtronic.diabot.data.migration

import com.dongtronic.diabot.data.mongodb.ProjectDAO
import com.dongtronic.diabot.data.mongodb.ProjectDTO
import com.dongtronic.diabot.util.logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import redis.clients.jedis.Jedis

class ProjectsMigrator : Migrator {
    private val redis = com.dongtronic.diabot.data.redis.InfoDAO.getInstance()
    private val mongo = ProjectDAO.instance
    private val jedis: Jedis = Jedis(System.getenv("REDIS_URL"))
    private val logger = logger()

    override fun checkAndMigrate(): Flux<Long> {
        return needsMigration().flatMapMany { needsMigrating ->
            if (needsMigrating) {
                migrate()
            } else {
                Flux.empty()
            }
        }
    }

    override fun needsMigration(): Mono<Boolean> {
        return mongo.collection.countDocuments().toMono().map {
            return@map it == 0L || redis.listProjects().size.toLong() > it
        }
    }

    override fun migrate(): Flux<Long> {
        val dtos = redis.listProjects().map { projectName ->
            val projectText = redis.getProjectText(projectName)

            ProjectDTO(name = projectName, text = projectText)
        }

        return dtos.toFlux()
                .flatMap { mongo.addProject(it) }
                .map { it.insertedId }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import project: $u", t)
                }
                .filter { it != null }
                .count()
                .toFlux()
    }
}