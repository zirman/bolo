package me.robch.application

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.netty.handler.codec.compression.StandardCompressionOptions.gzip
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import util.canvasId
import java.time.Duration

fun Application.ktorModule() {
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }

    install(ContentNegotiation) {
        json()
    }

//        install(CORS) {
//            method(HttpMethod.Get)
//            method(HttpMethod.Post)
//            method(HttpMethod.Delete)
//            anyHost()
//        }

    install(Compression) {
        gzip()
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        // Disabled (max value). The connection will be closed if surpassed this length.
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        staticResources("/static", null)
        staticResources("/static", "files")

        get("/") {
            call.respondHtml {
                head {
                    title { +"Bolo" }

                    style {
                        unsafe {
                            raw(
                                """
                                    canvas {
                                        position: fixed;
                                        background-color: #808;
                                        top: 0;
                                        left: 0;
                                        width: 100%;
                                        height: 100%;
                                    }
                                    """.trimIndent(),
                            )
                        }
                    }

                    meta {
                        name = "description"
                        content = ""
                    }

                    meta {
                        name = "viewport"
                        content = "width=device-width, initial-scale=1"
                    }
                }

                body {
                    canvas { id = canvasId }
                    script { src = "/static/bolo.js" }
                }
            }
        }

        val boloServer: BoloServer by inject()

        webSocket("/ws") {
            boloServer.handleWebSocket(this)
        }
    }
}
