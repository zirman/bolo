package client

import client.math.V2

data class NextBuilderMission(val builderMode: BuilderMode, val col: Int, val row: Int)

interface Tank : GameProcess {
    val position: V2
    val bearing: Float
    val sightRange: Float
    val onBoat: Boolean
    var material: Int
    var mines: Int
    var hasBuilder: Boolean
    fun getNextBuilderMission(): NextBuilderMission?
    fun setNextBuilderMission(nextBuilderMission: NextBuilderMission?)
}
