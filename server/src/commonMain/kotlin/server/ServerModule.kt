@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.BmapReader
import java.io.File
import java.util.Properties
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private const val MAP = "MAP"

val serverModule = module {
    single { BoloServer(get(), get()) } withOptions {
        createdAtStart()
    }

    // TODO make deferred and platform agnostic
    single {
        File("${System.getProperty("user.dir")}${File.separator}bolo.properties")
            .inputStream()
            .let { Properties().apply { load(it) } }
    }

    // TODO make deferred and platform agnostic
    single<Bmap> {
        BoloServer::class.java.classLoader.getResource(
            "maps${File.separator}${
                get<Properties>().getProperty(MAP, "Flame War.map")
            }"
        )!!
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    single { BmapCode() }
}
