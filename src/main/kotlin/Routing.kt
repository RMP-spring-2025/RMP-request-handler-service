package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import redis.clients.jedis.Jedis

val keyDB = Jedis("localhost", 6379)


fun Application.configureRouting() {
    routing {
        route("/service1") {
            get("/{id}") {
                val id = call.parameters["id"]
                val value = keyDB.get(id) ?: "Not found" // Читаем данные из KeyDB
                call.respond(HttpStatusCode.OK, mapOf("id" to id, "value" to value))
            }
            post("/{id}/{value}") {
                val id = call.parameters["id"]
                val value = call.parameters["value"]
                if (id != null && value != null) {
                    keyDB.set(id, value) // Записываем в KeyDB
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Saved in KeyDB", "id" to id, "value" to value))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters"))
                }
            }
        }

        route("/service2") {
            get {
                val keys = keyDB.keys("*")
                call.respond(HttpStatusCode.OK, mapOf("keys" to keys))
            }
            delete("/{id}") {
                val id = call.parameters["id"]
                if (id != null) {
                    keyDB.del(id) // Удаляем запись из KeyDB
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Deleted from KeyDB", "id" to id))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters"))
                }
            }
        }
    }
}
