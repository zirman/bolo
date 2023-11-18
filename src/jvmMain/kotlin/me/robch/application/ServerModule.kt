@file:OptIn(ExperimentalUnsignedTypes::class)

package me.robch.application

import bmap.Bmap
import bmap.BmapCode
import bmap.BmapReader
import org.koin.dsl.module
import java.io.File

val serverModule = module {
    single<BoloServer> { BoloServer() }

    single<Bmap> {
        File("build/processedResources/jvm/main/maps/Baringi.map")
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    single<BmapCode> { BmapCode() }
}
