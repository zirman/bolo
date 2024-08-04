package server

import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.util.Properties
import org.slf4j.LoggerFactory

private const val KEYSTORE_PATH = "KEYSTORE_PATH"
private const val KEY_ALIAS = "KEY_ALIAS"
private const val KEY_STORE_PASSWORD = "KEY_STORE_PASSWORD"
private const val PRIVATE_KEY_PASSWORD = "PRIVATE_KEY_PASSWORD"
private const val HTTP_PORT = "HTTP_PORT"
private const val HTTPS_PORT = "HTTPS_PORT"

fun main() {
    embeddedServer(
        factory = Netty,
        environment = applicationEnvironment { log = LoggerFactory.getLogger("ktor.application") },
        configure = { envConfig() },
        module = Application::ktorModule,
    ).start(wait = true)
}

private fun ApplicationEngine.Configuration.envConfig() {
    val properties = File("${System.getProperty("user.dir")}${File.separator}bolo.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        .let { inputStream ->
            Properties().apply {
                if (inputStream != null) {
                    load(inputStream)
                }
            }
        }

    val httpPort = properties.getProperty(HTTP_PORT, "8080").toInt()
    val httpsPort = properties.getProperty(HTTPS_PORT, "8443").toInt()
    val keystorePath: String? = properties.getProperty(KEYSTORE_PATH, null)
    val keyAlias = properties.getProperty(KEY_ALIAS, "robch.dev")
    val keyStorePassword = properties.getProperty(KEY_STORE_PASSWORD, "")
    val privateKeyPassword = properties.getProperty(PRIVATE_KEY_PASSWORD, "")

    val keyStore = if (keystorePath != null) {
        KeyStore.getInstance("JKS").apply {
            load(
                /* stream = */ FileInputStream(keystorePath),
                /* password = */ keyStorePassword.toCharArray(),
            )
        }
    } else {
        buildKeyStore {
            certificate(keyAlias) {
                password = keyStorePassword
            }
        }
    }
    connector {
        port = httpPort
    }
    sslConnector(
        keyStore = keyStore,
        keyAlias = keyAlias,
        keyStorePassword = { keyStorePassword.toCharArray() },
        privateKeyPassword = { privateKeyPassword.toCharArray() },
    ) {
        port = httpsPort
    }
}
