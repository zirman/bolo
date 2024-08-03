@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.BmapReader
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val serverModule = module {
    single { BoloServer(get(), get()) } withOptions {
        createdAtStart()
    }

    // TODO make deferred and platform agnostic
    single<Bmap> {
        BoloServer::class.java.classLoader.getResource("maps/Easter Island III.map")!!
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    single { BmapCode() }
}
