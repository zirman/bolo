package client

import common.bmap.Bmap
import common.bmap.Entity
import common.bmap.TerrainTile
import common.frame.Owner
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import client.math.V2

interface Game {
    val bmap: Bmap
    val owner: Owner
    val sendChannel: SendChannel<Frame>
    var center: V2
    val tank: Tank?
    val zoomLevel: Float
    fun terrainDamage(col: Int, row: Int)
    fun buildTerrain(col: Int, row: Int, terrainTile: TerrainTile, result: (BuildResult) -> Unit)
    fun mineTerrain(col: Int, row: Int, result: (BuildResult) -> Unit)
    fun baseDamage(index: Int)
    fun pillDamage(index: Int)
    operator fun get(col: Int, row: Int): Entity
    fun BuilderMode.tryBuilderAction(tank: Tank, col: Int, row: Int): Builder?
}
