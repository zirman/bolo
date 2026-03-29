package server

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface ServerGraph {
    val boloServer: BoloServer
}
