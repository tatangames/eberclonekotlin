package com.santaananortemetapan.uberclone.models

import com.beust.klaxon.*
private val klaxon = Klaxon()

data class Client (
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
) {
    fun toJson() = klaxon.toJsonString(this)

    companion object {
        fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}
