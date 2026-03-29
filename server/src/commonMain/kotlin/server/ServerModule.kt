@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.bmap.Bmap
import common.bmap.BmapReader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.io.File
import java.util.Properties

private const val MAP = "MAP"

@ContributesTo(AppScope::class)
@BindingContainer
interface ServerModule {
    companion object {
        @SingleIn(AppScope::class)
        @Provides
        fun provideProperties(): Properties {
            return File("${System.getProperty("user.dir")}${File.separator}bolo.properties")
                .takeIf { it.exists() }
                ?.inputStream()
                .let { inputStream ->
                    Properties().apply {
                        if (inputStream != null) {
                            load(inputStream)
                        }
                    }
                }
        }

        @SingleIn(AppScope::class)
        @Provides
        fun provideBmap(properties: Properties): Bmap {
            return BoloServer::class.java.classLoader.getResource(
                "maps${File.separator}${
                    properties.getProperty(MAP, "Flame War.map")
                }"
            )!!
                .readBytes()
                .toUByteArray()
                .let { BmapReader(offset = 0, buffer = it) }
                .bmap
        }
    }
}
