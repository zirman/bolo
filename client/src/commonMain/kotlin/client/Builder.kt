package client

import client.math.V2

interface Builder : GameProcess {
    val position: V2
}
