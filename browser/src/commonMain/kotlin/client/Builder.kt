package client

import math.V2

interface Builder : GeneratorLoop<Tick> {
    val position: V2
}
