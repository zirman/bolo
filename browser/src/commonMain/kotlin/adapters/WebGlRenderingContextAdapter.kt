package adapters

import client.SpriteProgram
import client.TileProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface WebGlRenderingContextAdapter {
    fun tileProgramFactory(coroutineScope: CoroutineScope): Deferred<TileProgram>
    fun spriteProgramFactory(coroutineScope: CoroutineScope): Deferred<SpriteProgram>
}
