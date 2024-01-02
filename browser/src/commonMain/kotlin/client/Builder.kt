package client

import math.V2

sealed interface BuilderMission {
    val x: Int
    val y: Int

    data class HarvestTree(override val x: Int, override val y: Int) : BuilderMission
    data class BuildWall(override val x: Int, override val y: Int) : BuilderMission
    data class BuildRoad(override val x: Int, override val y: Int) : BuilderMission
    data class BuildBoat(override val x: Int, override val y: Int) : BuilderMission
    data class PlaceMine(override val x: Int, override val y: Int) : BuilderMission
    data class PlacePill(override val x: Int, override val y: Int, val index: Int, val material: Int) : BuilderMission
    data class RepairPill(override val x: Int, override val y: Int, val index: Int, val material: Int) : BuilderMission
}

interface Builder : GeneratorLoop<Tick> {
    val position: V2
}
