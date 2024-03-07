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
    fun terrainDamage(col: Int, row: Int)
    fun buildTerrain(col: Int, row: Int, t: TerrainTile, material: Int, result: (Boolean) -> Unit)
    fun mineTerrain(col: Int, row: Int)
    fun baseDamage(index: Int)
    fun pillDamage(index: Int)
    operator fun get(col: Int, row: Int): Entity
    fun BuilderMode.tryBuilderAction(tank: Tank, col: Int, row: Int): Builder?
}
