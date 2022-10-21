package com.dongtronic.diabot.data.mongodb

data class ProjectDTO(
        val name: String,
        val text: String
) : Comparable<ProjectDTO> {
    override fun compareTo(other: ProjectDTO): Int {
        return name.compareTo(other.name)
    }
}
