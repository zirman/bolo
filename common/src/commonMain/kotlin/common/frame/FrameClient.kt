package common.frame

import common.bmap.TerrainTile
import kotlinx.serialization.Serializable

val frameClientSerializer = FrameClient.serializer()

@Serializable
sealed interface FrameClient {

    @Serializable
    sealed interface Signal : FrameClient {
        val owner: Owner

        @Serializable
        data class Offer(override val owner: Owner, val sessionDescription: String) : Signal

        @Serializable
        data class Answer(override val owner: Owner, val sessionDescription: String) : Signal

        @Serializable
        data class IceCandidate(override val owner: Owner, val iceCandidate: String) : Signal
    }

    @Serializable
    data class TerrainBuild(
        val terrain: TerrainTile,
        val col: Int,
        val row: Int,
    ) : FrameClient

    @Serializable
    data class TerrainDamage(
        val code: Int,
        val col: Int,
        val row: Int,
    ) : FrameClient

    @Serializable
    data class TerrainMine(
        val col: Int,
        val row: Int,
    ) : FrameClient

    @Serializable
    data class BaseDamage(
        val index: Int,
        val code: Int,
    ) : FrameClient

    @Serializable
    data class PillDamage(
        val index: Int,
        val code: Int,
        val col: Int,
        val row: Int,
    ) : FrameClient

    @Serializable
    data class PillRepair(
        val index: Int,
        val col: Int,
        val row: Int,
        val material: Int,
    ) : FrameClient

    @Serializable
    data class PillPlacement(
        val index: Int,
        val col: Int,
        val row: Int,
        val material: Int,
    ) : FrameClient

    @Serializable
    data class Position(
        val col: Int,
        val row: Int,
    ) : FrameClient
}
