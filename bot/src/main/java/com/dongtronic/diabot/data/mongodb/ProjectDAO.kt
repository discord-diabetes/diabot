package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.util.*
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.upsert
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class ProjectDAO private constructor() {
    private val mongo = MongoDB.getInstance().database
    val collection: MongoCollection<ProjectDTO> =
        mongo.getCollection(DiabotCollection.PROJECTS.getEnv(), ProjectDTO::class.java)
    private val scheduler = Schedulers.boundedElastic()
    private val logger = logger()

    init {
        // Create a unique index
        val options = IndexOptions().unique(true)
        collection.createIndex(descending(ProjectDTO::name), options).toMono()
            .subscribeOn(scheduler)
            .subscribe()
    }

    /**
     * Gets a [ProjectDTO] from a project name
     *
     * @param name The project to look up
     * @return The project's DTO
     */
    fun getProject(name: String): Mono<ProjectDTO> {
        return collection.findOne(filter(name))
            .subscribeOn(scheduler)
    }

    /**
     * Gets all projects in the database
     *
     * @return [Flux] of all the projects in the database
     */
    fun listProjects(): Flux<ProjectDTO> {
        return collection.findMany()
            .subscribeOn(scheduler)
    }

    /**
     * Inserts a new project into the database.
     * This should only be used if it is known that the project does not exist already as it will error otherwise.
     *
     * @param dto The [ProjectDTO] to be inserted
     * @return The result of the insertion
     */
    fun addProject(dto: ProjectDTO): Mono<InsertOneResult> {
        return collection.insertOne(dto).toMono()
            .subscribeOn(scheduler)
    }

    /**
     * Deletes a project from the database.
     *
     * @param name The project's name
     * @return The result of deleting the project
     */
    fun deleteProject(name: String): Mono<DeleteResult> {
        return collection.deleteOne(filter(name)).toMono()
            .subscribeOn(scheduler)
    }

    /**
     * Sets a project's information
     *
     * @param name The project name
     * @param text The project's new text
     * @return The result of setting the project's information
     */
    fun setInfo(name: String, text: String): Mono<UpdateResult> {
        return collection.updateOne(
            filter(name),
            setValue(ProjectDTO::text, text), upsert()
        )
            .toMono()
            .subscribeOn(scheduler)
    }

    companion object {
        val instance: ProjectDAO by lazy { ProjectDAO() }

        fun filter(name: String): Bson {
            return ProjectDTO::name eq name
        }
    }
}
