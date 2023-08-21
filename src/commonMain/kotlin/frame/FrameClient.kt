package frame

import bmap.Terrain
import kotlinx.serialization.Serializable

@Serializable
sealed class FrameClient {
    @Serializable
    data class TerrainBuild(
        val terrain: Terrain,
        val x: Int,
        val y: Int,
    ) : FrameClient()

    @Serializable
    data class TerrainDamage(
        val code: Int,
        val x: Int,
        val y: Int,
    ) : FrameClient()

    @Serializable
    data class BaseDamage(
        val index: Int,
        val code: Int,
    ) : FrameClient()

    @Serializable
    data class PillDamage(
        val index: Int,
        val code: Int,
        val x: Int,
        val y: Int,
    ) : FrameClient()

    @Serializable
    data class PillRepair(
        val index: Int,
        val owner: Int,
        val x: Int,
        val y: Int,
        val material: Int,
    ) : FrameClient()

    @Serializable
    data class PillPlacement(
        val index: Int,
        val x: Int,
        val y: Int,
        val material: Int,
    ) : FrameClient()

    @Serializable
    data class Position(
        val x: Int,
        val y: Int,
    ) : FrameClient()
}
