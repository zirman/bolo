package client

import math.V2

interface Tank : EntityLoop {
    val position: V2
    val bearing: Float
    val sightRange: Float
    val onBoat: Boolean
    var material: Int
    var hasBuilder: Boolean
    var nextBuilderMission: BuilderMission?
}
