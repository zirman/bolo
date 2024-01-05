package client

import bmap.Bmap
import bmap.TypeTile
import bmap.border
import bmap.ind
import bmap.toTypeTile
import bmap.worldHeight
import bmap.worldWidth
import frame.Owner
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

class ImageTileArrayImpl(private val bmap: Bmap, private val owner: Owner) : ImageTileArray {
    private val tiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
        .also { tiles ->
            for (y in 0..<worldHeight) {
                for (x in 0..<worldWidth) {
                    tiles[ind(x, y)] = bmap[x, y].toTypeTile().ordinal.toByte()
                }
            }

            for (base in bmap.bases) {
                tiles[ind(base.x, base.y)] =
                    when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toByte()
            }

            for (pill in bmap.pills) {
                if (pill.isPlaced) {
                    tiles[ind(pill.x, pill.y)] = (TypeTile.PillHostile0.ordinal + pill.armor).toByte()
                }
            }
        }

    private val imageTiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
        .also { tiles ->
            for (y in 0..<worldHeight) {
                for (x in 0..<worldWidth) {
                    tiles[ind(x, y)] = mapImage(x, y).index.toByte()
                }
            }
        }

    override fun getTypeTile(x: Int, y: Int): TypeTile =
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) TypeTile.SeaMined
        else TypeTile.entries[tiles[ind(x, y)].toInt()]

    override fun update(x: Int, y: Int) {
        run {
            for (pill in bmap.pills) {
                if (pill.isPlaced && pill.x == x && pill.y == y) {
                    tiles[ind(pill.x, pill.y)] = run {
                        if (pill.owner == owner.int) TypeTile.PillFriendly0 else TypeTile.PillHostile0
                    }
                        .ordinal
                        .let { it + pill.armor }
                        .toByte()
                    return@run
                }
            }

            for (base in bmap.bases) {
                if (base.x == x && base.y == y) {
                    tiles[ind(base.x, base.y)] = when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toByte()
                    return@run
                }
            }

            if (x >= border && x < worldWidth - border && y >= border && y < worldHeight - border) {
                tiles[ind(x, y)] = bmap[x, y].toTypeTile().ordinal.toByte()
            }
        }

        for (yi in y - 1..y + 1) {
            for (xi in x - 1..x + 1) {
                imageTiles[ind(xi, yi)] = mapImage(xi, yi).index.toByte()
            }
        }
    }

    override val arrayBuffer: Any get() = tiles
}
