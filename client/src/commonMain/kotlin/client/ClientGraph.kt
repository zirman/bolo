package client

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface ClientGraph {
    val clientApplication: ClientApplication
}
