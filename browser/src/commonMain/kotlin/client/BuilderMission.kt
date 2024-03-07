package client

sealed interface BuilderMission {
    val col: Int
    val row: Int
    val material: IntRange
    val mines: Int

    data class HarvestTree(override val col: Int, override val row: Int) : BuilderMission {
        override val material: IntRange = 0..0
        override val mines: Int = 0
    }

    data class BuildWall(override val col: Int, override val row: Int, override val material: IntRange) : BuilderMission {
        override val mines: Int = 0
    }

    data class BuildRoad(override val col: Int, override val row: Int) : BuilderMission {
        override val material: IntRange = 2..2
        override val mines: Int = 0
    }

    data class BuildBoat(override val col: Int, override val row: Int) : BuilderMission {
        override val material: IntRange = 8..8
        override val mines: Int = 0
    }

    data class PlaceMine(override val col: Int, override val row: Int) : BuilderMission {
        override val material: IntRange = 0..0
        override val mines: Int = 1
    }

    data class PlacePill(
        override val col: Int,
        override val row: Int,
        val index: Int,
        override val material: IntRange,
    ) : BuilderMission {
        override val mines: Int = 0
    }

    data class RepairPill(
        override val col: Int,
        override val row: Int,
        val index: Int,
        override val material: IntRange,
    ) : BuilderMission {
        override val mines: Int = 0
    }
}
