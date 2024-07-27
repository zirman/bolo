package client

import client.math.V2

interface Shell : GameProcess {
    val position: V2
    val bearing: Float
}
