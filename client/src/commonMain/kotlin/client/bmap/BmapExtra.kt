package client.bmap

import common.bmap.Bmap
import common.bmap.BmapExtra

fun BmapExtra.loadCodes(bmap: Bmap) {
    pillCodes.forEachIndexed { index, code ->
        bmap.pills[index].code = code
    }

    baseCodes.forEachIndexed { index, code ->
        bmap.bases[index].code = code
    }
}
