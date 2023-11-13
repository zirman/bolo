package frame

import bmap.Terrain
import kotlinx.serialization.Serializable

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
        val terrain: Terrain,
        val x: Int,
        val y: Int,
    ) : FrameClient

    @Serializable
    data class TerrainDamage(
        val code: Int,
        val x: Int,
        val y: Int,
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
        val x: Int,
        val y: Int,
    ) : FrameClient

    @Serializable
    data class PillRepair(
        val index: Int,
        val owner: Int,
        val x: Int,
        val y: Int,
        val material: Int,
    ) : FrameClient

    @Serializable
    data class PillPlacement(
        val index: Int,
        val x: Int,
        val y: Int,
        val material: Int,
    ) : FrameClient

    @Serializable
    data class Position(
        val x: Int,
        val y: Int,
    ) : FrameClient
}
