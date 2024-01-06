package client

import bmap.TypeTile

interface ImageTileArray {
    fun getTypeTile(x: Int, y: Int): TypeTile
    fun update(x: Int, y: Int)
    val uint8Array: Any
}
