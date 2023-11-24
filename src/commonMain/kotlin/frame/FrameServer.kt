package frame

import bmap.TerrainTile
import kotlinx.serialization.Serializable

@Serializable
sealed interface FrameServer {

    @Serializable
    sealed interface Signal : FrameServer {
        val from: Owner

        @Serializable
        data class NewPeer(override val from: Owner) : Signal

        @Serializable
        data class Offer(override val from: Owner, val sessionDescription: String) : Signal

        @Serializable
        data class Answer(override val from: Owner, val sessionDescription: String) : Signal

        @Serializable
        data class IceCandidate(override val from: Owner, val iceCandidate: String) : Signal

        @Serializable
        data class Disconnect(override val from: Owner) : Signal
    }

    @Serializable
    data class TerrainBuild(
        val terrain: TerrainTile,
        val x: Int,
        val y: Int,
    ) : FrameServer

    @Serializable
    data object TerrainBuildSuccess : FrameServer

    @Serializable
    data object TerrainBuildFailed : FrameServer

    @Serializable
    data class TerrainDamage(
        val x: Int,
        val y: Int,
    ) : FrameServer

    @Serializable
    data class BaseTake(
        val index: Int,
        val owner: Int,
        var armor: Int,
        var shells: Int,
        var mines: Int,
    ) : FrameServer

    @Serializable
    data class BaseDamage(
        val index: Int,
    ) : FrameServer

    @Serializable
    data class PillDamage(
        val index: Int,
    ) : FrameServer

    @Serializable
    data class PillRepair(
        val index: Int,
        val armor: Int,
    ) : FrameServer

    @Serializable
    data class PillRepairSuccess(
        val material: Int,
    ) : FrameServer

    @Serializable
    data object PillRepairFailed : FrameServer

    @Serializable
    data class PillTake(
        val index: Int,
        val owner: Int,
    ) : FrameServer

    @Serializable
    data class PillPlacement(
        val index: Int,
        val armor: Int,
        val x: Int,
        val y: Int,
    ) : FrameServer

    @Serializable
    data object PillPlacementSuccess : FrameServer

    @Serializable
    data object PillPlacementFailed : FrameServer

    @Serializable
    data class PillDrop(
        val index: Int,
        val owner: Int,
        val x: Int,
        val y: Int,
    ) : FrameServer
}
