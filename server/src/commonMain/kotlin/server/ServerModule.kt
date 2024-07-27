@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.BmapReader
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import java.io.File

val serverModule = module {
    single { BoloServer(get(), get()) } withOptions {
        createdAtStart()
    }

    single<Bmap> {
        File("build/resources/main/maps/Bob\'s Country Bunker rev. 1.1.map")
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    single { BmapCode() }
}