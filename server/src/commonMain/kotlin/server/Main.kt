package server

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import java.io.FileInputStream
import java.security.KeyStore
import org.slf4j.LoggerFactory

private const val USE_KEYSTORE_PATH: String = ""
private const val KEY_ALIAS = "robch.dev"
private const val KEY_STORE_PASSWORD_STRING = ""
private const val PRIVATE_KEY_PASSWORD_STRING = ""
private const val HTTP_PORT = 8080
private const val HTTPS_PORT = 8443

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
        port = HTTP_PORT
    }

    val ks = if (USE_KEYSTORE_PATH.isNotBlank()) {
        KeyStore.getInstance("JKS").apply {
            load(
                /* stream = */ FileInputStream("keystore.jks"),
                /* password = */ KEY_STORE_PASSWORD_STRING.toCharArray(),
            )
        }
    } else {
        buildKeyStore {
            certificate(KEY_ALIAS) {
                password = KEY_STORE_PASSWORD_STRING
            }
        }
    }

    sslConnector(
        keyStore = ks,
        keyAlias = KEY_ALIAS,
        keyStorePassword = { KEY_STORE_PASSWORD_STRING.toCharArray() },
        privateKeyPassword = { PRIVATE_KEY_PASSWORD_STRING.toCharArray() },
    ) {
        port = HTTPS_PORT
    }
}
