package com.dongtronic.diabot.data

import com.dongtronic.diabot.util.RedisKeyFormats
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import kotlin.IllegalArgumentException

class InfoDAO private constructor() {
    private var jedis: Jedis? = null
    private val logger = LoggerFactory.getLogger(InfoDAO::class.java)


    init {
        if (System.getenv("REDIS_URL") != null) {
            jedis = Jedis(System.getenv("REDIS_URL"))
        } else if (System.getenv("DIABOT_REDIS_URL") != null) {
            jedis = Jedis(System.getenv("DIABOT_REDIS_URL"))
        }
    }

    fun listProjects(): MutableList<String> {
        val key = RedisKeyFormats.infoList

        val projectListLength = jedis!!.llen(key)

        return jedis!!.lrange(key, 0, projectListLength - 1)
    }

    fun formatProject(project: String): String? {
        val projects = listProjects()

        projects.forEach{foundProject ->
            if (foundProject.toUpperCase() == project.toUpperCase()) {
                return foundProject
            }
        }

        return null
    }

    fun findProject(project: String): Int {
        val projects = listProjects()

        projects.forEach{foundProject ->
            if (foundProject.toUpperCase() == project.toUpperCase()) {
                return projects.indexOf(foundProject)
            }
        }

        return -1
    }

    fun removeProject(project: String) {
        val key = RedisKeyFormats.infoList

        if (findProject(project) == -1) {
            throw IllegalArgumentException("Project $project is not configured")
        }

        val properName = formatProject(project)
        val textKey = RedisKeyFormats.infoText.replace("{{project}}", properName!!)

        jedis!!.lrem(key, 0, properName)
        jedis!!.del(textKey)
    }

    fun getProjectText(project: String): String {
        val properName = formatProject(project)

        if (properName == null) {
            throw IllegalArgumentException("Project $project does not exist")
        }

        val key = RedisKeyFormats.infoText.replace("{{project}}", properName)

        return jedis!!.get(key)
    }

    fun setProjectText(project: String, description: String) {
        val exists = findProject(project) != -1

        if (exists) {
            // update existing project
            val properName = formatProject(project)
            val textKey = RedisKeyFormats.infoText.replace("{{project}}", properName!!)
            jedis!!.set(textKey, description)
        } else {
            // create new project
            val listKey = RedisKeyFormats.infoList
            val textKey = RedisKeyFormats.infoText.replace("{{project}}", project)
            jedis!!.lpush(listKey, project)
            jedis!!.set(textKey, description)
        }
    }

    companion object {
        private var instance: InfoDAO? = null

        fun getInstance(): InfoDAO {
            if (instance == null) {
                instance = InfoDAO()
            }
            return instance as InfoDAO
        }
    }
}
