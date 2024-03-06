package client

import math.V2

interface Shell : EntityLoop {
    val position: V2
    val bearing: Float
}
