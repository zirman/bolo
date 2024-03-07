package client

sealed interface BuilderMission {
    val col: Int
    val row: Int
    val material: Int
    val mines: Int

    data class HarvestTree(override val col: Int, override val row: Int) : BuilderMission {
        override val material: Int = 0
        override val mines: Int = 0
    }

    data class BuildWall(override val col: Int, override val row: Int, override val material: Int) : BuilderMission {
        override val mines: Int = 0
    }

    data class BuildRoad(override val col: Int, override val row: Int) : BuilderMission {
        override val material: Int = 2
        override val mines: Int = 0
    }

    data class BuildBoat(override val col: Int, override val row: Int) : BuilderMission {
        override val material: Int = 8
        override val mines: Int = 0
    }

    data class PlaceMine(override val col: Int, override val row: Int) : BuilderMission {
        override val material: Int = 0
        override val mines: Int = 1
    }

    data class PlacePill(
        override val col: Int,
        override val row: Int,
        val index: Int,
        override val material: Int,
    ) : BuilderMission {
        override val mines: Int = 0
    }

    data class RepairPill(
        override val col: Int,
        override val row: Int,
        val index: Int,
        override val material: Int,
    ) : BuilderMission {
        override val mines: Int = 0
    }
}
