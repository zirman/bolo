package dev.robch.bolo

import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.security.KeyStore

fun main() {
    embeddedServer(
        factory = Netty,
        environment = applicationEnvironment { log = LoggerFactory.getLogger("ktor.application") },
        configure = { envConfig() },
        module = Application::ktorModule,
    ).start(wait = true)
}

private fun ApplicationEngine.Configuration.envConfig() {
    connector {
        port = 8080
    }

    val password = "".toCharArray()

    sslConnector(
        keyStore = KeyStore.getInstance("JKS").apply {
            load(
                /* stream = */ FileInputStream("keystore.jks"),
                /* password = */ password,
            )
        },
        keyAlias = "robch.dev",
        keyStorePassword = { password },
        privateKeyPassword = { password },
    ) {
        port = 8443
    }
}
