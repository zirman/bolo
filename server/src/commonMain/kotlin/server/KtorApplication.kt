package server

import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.netty.handler.codec.compression.StandardCompressionOptions.gzip
import java.time.Duration
import kotlinx.css.CssBuilder
import org.koin.core.context.startKoin

fun Application.ktorModule() {
    // Manually start Koin until it's updated for Ktor 3
    val koinApp = startKoin {
        modules(serverModule)
        createEagerInstances()
    }

    // install(Koin) {
    //     slf4jLogger()
    //     modules(serverModule)
    // }

    install(ContentNegotiation) {
        json()
    }

    // install(CORS) {
    //     method(HttpMethod.Get)
    //     method(HttpMethod.Post)
    //     method(HttpMethod.Delete)
    //     anyHost()
    // }

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
        staticResources("/", null)
        staticResources("/", "files")
        staticResources("/src", "src")

        get("/") {
            call.respondHtml {
                mainHtml()
            }
        }

        get("/styles.css") {
            call.respondCss {
                mainCss()
            }
        }

        val boloServer: BoloServer = koinApp.koin.get() // by inject()

        webSocket("/wss") {
            boloServer.handleWebSocket(this)
        }
    }
}

suspend inline fun ApplicationCall.respondCss(builder: CssBuilder.() -> Unit) {
    this.respondText(CssBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
