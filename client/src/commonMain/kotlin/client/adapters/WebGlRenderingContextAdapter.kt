package client.adapters

import client.SpriteProgram
import client.TileProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface WebGlRenderingContextAdapter {
    fun tileProgramFactory(scope: CoroutineScope): Deferred<TileProgram>
    fun spriteProgramFactory(scope: CoroutineScope): Deferred<SpriteProgram>
}
