import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.html.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import java.time.Duration

fun main() {
    embeddedServer(Netty, 8080) {
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
            static("/img") {
                resources("files")
            }

            static("/static") {
                resources()
            }

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
                        canvas { id = "bolo-canvas" }
                        script { src = "/static/bolo.js" }
                    }
                }
            }

            webSocket("/ws") {
                for (frame in incoming) {
//                    when (frame) {
//                        is Frame.Text -> {
//                            val text = frame.readText()
//                            outgoing.send(Frame.Text("YOU SAID: $text"))
//                            if (text.equals("bye", ignoreCase = true)) {
//                                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
//                            }
//                        }
//                    }
                }
            }
        }
    }.start(wait = true)
}
