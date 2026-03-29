package client

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(GameScope::class)
@BindingContainer
interface GameModule {
    @Binds
    fun bindGame(gameImpl: GameImpl): Game
}
