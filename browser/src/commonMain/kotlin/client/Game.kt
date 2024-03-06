package client

import bmap.Bmap
import bmap.Entity
import bmap.TerrainTile
import frame.Owner
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import math.V2
import kotlin.random.Random

interface Game {
    val bmap: Bmap
    val random: Random
    val owner: Owner
    val sendChannel: SendChannel<Frame>
    var center: V2
    val tank: Tank?
    val zoomLevel: Float
    suspend fun terrainDamage(x: Int, y: Int)
    suspend fun buildTerrain(x: Int, y: Int, t: TerrainTile, result: (Boolean) -> Unit)
    suspend fun mineTerrain(x: Int, y: Int)
    suspend fun baseDamage(index: Int)
    suspend fun pillDamage(index: Int)
    operator fun get(x: Int, y: Int): Entity
}
