package com.dongtronic.diabot.data.migration.redis

import com.dongtronic.diabot.data.migration.MigrationManager
import com.dongtronic.diabot.data.mongodb.ProjectDAO
import com.dongtronic.diabot.data.mongodb.ProjectDTO
import com.dongtronic.diabot.util.logger
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@ChangeLog(order = "004")
class ProjectsMigrator {
    private val mongo = ProjectDAO.instance
    private val redis by lazy { com.dongtronic.diabot.data.redis.InfoDAO.getInstance() }
    private val logger = logger()

    fun needsMigration(): Boolean {
        if (!MigrationManager.canRedisMigrate()) return false

        return mongo.collection.countDocuments().toMono().map {
            return@map it == 0L || redis.listProjects().size.toLong() > it
        }.block()!!
    }

    @ChangeSet(order = "001", id = "redisProjects", author = "Garlic")
    fun migrate() {
        if (!needsMigration()) return

        val dtos = redis.listProjects().map { projectName ->
            val projectText = redis.getProjectText(projectName)

            ProjectDTO(name = projectName, text = projectText)
        }

        dtos.toFlux()
                .flatMap { mongo.addProject(it) }
                .map { it.wasAcknowledged() }
                .onErrorContinue { t, u ->
                    logger.warn("Could not import project: $u", t)
                }
                .filter { it }
                .blockLast()!!
    }
}