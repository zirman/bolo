@file:OptIn(ExperimentalUnsignedTypes::class)

package me.robch.application

import bmap.Bmap
import bmap.BmapCode
import bmap.BmapReader
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import java.io.File

val serverModule = module {
    single { BoloServer(get(), get()) } withOptions {
        createdAtStart()
    }

    single<Bmap> {
        File("build/processedResources/jvm/main/maps/Bob\'s Country Bunker rev. 1.1.map")
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    single { BmapCode() }
}
