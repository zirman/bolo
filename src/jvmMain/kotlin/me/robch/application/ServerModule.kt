@file:OptIn(ExperimentalUnsignedTypes::class)

package me.robch.application

import bmap.Bmap
import bmap.BmapCode
import bmap.BmapReader
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

val serverModule = module {
    singleOf(::BoloServer)

    single<Bmap> {
        File("build/processedResources/jvm/main/maps/Baringi.map")
            .readBytes()
            .toUByteArray()
            .let { BmapReader(offset = 0, buffer = it) }
            .bmap
    }

    singleOf(::BmapCode)
}
