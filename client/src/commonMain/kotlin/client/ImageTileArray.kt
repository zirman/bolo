package client

import common.bmap.TypeTile

interface ImageTileArray {
    fun getTypeTile(col: Int, row: Int): TypeTile
    fun update(col: Int, row: Int)
    val uint8Array: Any
}
