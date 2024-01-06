package client

import math.V2

interface Shell : GeneratorLoop<Tick> {
    val position: V2
    val bearing: Float
}
