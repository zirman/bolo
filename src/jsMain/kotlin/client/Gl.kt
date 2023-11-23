package client

import assert.assertNotNull
import bmap.ind
import bmap.worldHeight
import bmap.worldWidth
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import math.M4
import math.clampCycle
import math.pi
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.BLEND
import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAGMENT_SHADER
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.NEAREST
import org.khronos.webgl.WebGLRenderingContext.Companion.RGB
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MAG_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MIN_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_S
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_T
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLE_FAN
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_ALIGNMENT
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER
import org.khronos.webgl.WebGLTexture
import org.khronos.webgl.WebGLUniformLocation
import org.khronos.webgl.set
import util.loadImage
import kotlin.math.floor

const val tilePixelWidth = 16
const val tilePixelHeight = 16

const val tileSheetSrc = "static/tile_sheet.png"

const val spriteSheetWidth = 16
const val spriteSheetHeight = 16

const val spriteSheetSrc = "static/sprite_sheet.png"

enum class Sprite(val int: Int) {
    TankBoat0(spriteInd(x = 0, y = 0)),
    TankBoat1(spriteInd(x = 1, y = 0)),
    TankBoat2(spriteInd(x = 2, y = 0)),
    TankBoat3(spriteInd(x = 3, y = 0)),
    TankBoat4(spriteInd(x = 4, y = 0)),
    TankBoat5(spriteInd(x = 5, y = 0)),
    TankBoat6(spriteInd(x = 6, y = 0)),
    TankBoat7(spriteInd(x = 7, y = 0)),
    TankBoat8(spriteInd(x = 8, y = 0)),
    TankBoat9(spriteInd(x = 9, y = 0)),
    TankBoat10(spriteInd(x = 10, y = 0)),
    TankBoat11(spriteInd(x = 11, y = 0)),
    TankBoat12(spriteInd(x = 12, y = 0)),
    TankBoat13(spriteInd(x = 13, y = 0)),
    TankBoat14(spriteInd(x = 14, y = 0)),
    TankBoat15(spriteInd(x = 15, y = 0)),
    Tank0(spriteInd(x = 0, y = 1)),
    Tank1(spriteInd(x = 1, y = 1)),
    Tank2(spriteInd(x = 2, y = 1)),
    Tank3(spriteInd(x = 3, y = 1)),
    Tank4(spriteInd(x = 4, y = 1)),
    Tank5(spriteInd(x = 5, y = 1)),
    Tank6(spriteInd(x = 6, y = 1)),
    Tank7(spriteInd(x = 7, y = 1)),
    Tank8(spriteInd(x = 8, y = 1)),
    Tank9(spriteInd(x = 9, y = 1)),
    Tank10(spriteInd(x = 10, y = 1)),
    Tank11(spriteInd(x = 11, y = 1)),
    Tank12(spriteInd(x = 12, y = 1)),
    Tank13(spriteInd(x = 13, y = 1)),
    Tank14(spriteInd(x = 14, y = 1)),
    Tank15(spriteInd(x = 15, y = 1)),
    TankFriendlyBoat0(spriteInd(x = 0, y = 2)),
    TankFriendlyBoat1(spriteInd(x = 1, y = 2)),
    TankFriendlyBoat2(spriteInd(x = 2, y = 2)),
    TankFriendlyBoat3(spriteInd(x = 3, y = 2)),
    TankFriendlyBoat4(spriteInd(x = 4, y = 2)),
    TankFriendlyBoat5(spriteInd(x = 5, y = 2)),
    TankFriendlyBoat6(spriteInd(x = 6, y = 2)),
    TankFriendlyBoat7(spriteInd(x = 7, y = 2)),
    TankFriendlyBoat8(spriteInd(x = 8, y = 2)),
    TankFriendlyBoat9(spriteInd(x = 9, y = 2)),
    TankFriendlyBoat10(spriteInd(x = 10, y = 2)),
    TankFriendlyBoat11(spriteInd(x = 11, y = 2)),
    TankFriendlyBoat12(spriteInd(x = 12, y = 2)),
    TankFriendlyBoat13(spriteInd(x = 13, y = 2)),
    TankFriendlyBoat14(spriteInd(x = 14, y = 2)),
    TankFriendlyBoat15(spriteInd(x = 15, y = 2)),
    TankFriendly0(spriteInd(x = 0, y = 3)),
    TankFriendly1(spriteInd(x = 1, y = 3)),
    TankFriendly2(spriteInd(x = 2, y = 3)),
    TankFriendly3(spriteInd(x = 3, y = 3)),
    TankFriendly4(spriteInd(x = 4, y = 3)),
    TankFriendly5(spriteInd(x = 5, y = 3)),
    TankFriendly6(spriteInd(x = 6, y = 3)),
    TankFriendly7(spriteInd(x = 7, y = 3)),
    TankFriendly8(spriteInd(x = 8, y = 3)),
    TankFriendly9(spriteInd(x = 9, y = 3)),
    TankFriendly10(spriteInd(x = 10, y = 3)),
    TankFriendly11(spriteInd(x = 11, y = 3)),
    TankFriendly12(spriteInd(x = 12, y = 3)),
    TankFriendly13(spriteInd(x = 13, y = 3)),
    TankFriendly14(spriteInd(x = 14, y = 3)),
    TankFriendly15(spriteInd(x = 15, y = 3)),
    TankEnemyBoat0(spriteInd(x = 0, y = 4)),
    TankEnemyBoat1(spriteInd(x = 1, y = 4)),
    TankEnemyBoat2(spriteInd(x = 2, y = 4)),
    TankEnemyBoat3(spriteInd(x = 3, y = 4)),
    TankEnemyBoat4(spriteInd(x = 4, y = 4)),
    TankEnemyBoat5(spriteInd(x = 5, y = 4)),
    TankEnemyBoat6(spriteInd(x = 6, y = 4)),
    TankEnemyBoat7(spriteInd(x = 7, y = 4)),
    TankEnemyBoat8(spriteInd(x = 8, y = 4)),
    TankEnemyBoat9(spriteInd(x = 9, y = 4)),
    TankEnemyBoat10(spriteInd(x = 10, y = 4)),
    TankEnemyBoat11(spriteInd(x = 11, y = 4)),
    TankEnemyBoat12(spriteInd(x = 12, y = 4)),
    TankEnemyBoat13(spriteInd(x = 13, y = 4)),
    TankEnemyBoat14(spriteInd(x = 14, y = 4)),
    TankEnemyBoat15(spriteInd(x = 15, y = 4)),
    TankEnemy0(spriteInd(x = 0, y = 5)),
    TankEnemy1(spriteInd(x = 1, y = 5)),
    TankEnemy2(spriteInd(x = 2, y = 5)),
    TankEnemy3(spriteInd(x = 3, y = 5)),
    TankEnemy4(spriteInd(x = 4, y = 5)),
    TankEnemy5(spriteInd(x = 5, y = 5)),
    TankEnemy6(spriteInd(x = 6, y = 5)),
    TankEnemy7(spriteInd(x = 7, y = 5)),
    TankEnemy8(spriteInd(x = 8, y = 5)),
    TankEnemy9(spriteInd(x = 9, y = 5)),
    TankEnemy10(spriteInd(x = 10, y = 5)),
    TankEnemy11(spriteInd(x = 11, y = 5)),
    TankEnemy12(spriteInd(x = 12, y = 5)),
    TankEnemy13(spriteInd(x = 13, y = 5)),
    TankEnemy14(spriteInd(x = 14, y = 5)),
    TankEnemy15(spriteInd(x = 15, y = 5)),
    Shell0(spriteInd(x = 0, y = 6)),
    Shell1(spriteInd(x = 1, y = 6)),
    Shell2(spriteInd(x = 2, y = 6)),
    Shell3(spriteInd(x = 3, y = 6)),
    Shell4(spriteInd(x = 4, y = 6)),
    Shell5(spriteInd(x = 5, y = 6)),
    Shell6(spriteInd(x = 6, y = 6)),
    Shell7(spriteInd(x = 7, y = 6)),
    Shell8(spriteInd(x = 8, y = 6)),
    Shell9(spriteInd(x = 9, y = 6)),
    Shell10(spriteInd(x = 10, y = 6)),
    Shell11(spriteInd(x = 11, y = 6)),
    Shell12(spriteInd(x = 12, y = 6)),
    Shell13(spriteInd(x = 13, y = 6)),
    Shell14(spriteInd(x = 14, y = 6)),
    Shell15(spriteInd(x = 15, y = 6)),
    Explosion0(spriteInd(x = 0, y = 7)),
    Explosion1(spriteInd(x = 1, y = 7)),
    Explosion2(spriteInd(x = 2, y = 7)),
    Explosion3(spriteInd(x = 3, y = 7)),
    Explosion4(spriteInd(x = 4, y = 7)),
    Explosion5(spriteInd(x = 5, y = 7)),
    Lgm0(spriteInd(x = 7, y = 7)),
    Lgm1(spriteInd(x = 7, y = 7)),
    Parachute(spriteInd(x = 8, y = 7)),
    Reticule(spriteInd(x = 9, y = 7)),
    Cursor(spriteInd(x = 10, y = 7));

    fun withBearing(bearing: Float): Sprite =
        entries[(ordinal + ((bearing + (Float.pi * (1.0 / 16.0))) * (8.0 / Float.pi)).toInt().clampCycle(16))]
}

data class SpriteInstance(val x: Float, val y: Float, val sprite: Sprite)

typealias TileProgram = (clipMatrix: M4, tileArray: TileArray) -> Unit

fun WebGLRenderingContext.createTileProgram(
    coroutineScope: CoroutineScope,
): Deferred<TileProgram> = coroutineScope.async {
    val program = createProgram().assertNotNull("shader program is null")

    createShader(
        program,
        type = VERTEX_SHADER,
        """
        attribute vec4 aVertex;
        attribute vec2 aCoordinate;

        uniform mat4 uClipMatrix;

        varying highp vec2 vTileMapCoordinate;

        void main() {
          gl_Position = uClipMatrix * aVertex;
          vTileMapCoordinate = aCoordinate;
        }
        """.trimIndent(),
    )

    createShader(
        program,
        type = FRAGMENT_SHADER,
        """
        precision highp float;
        varying vec2 vTileMapCoordinate;

        uniform sampler2D uTiles;
        uniform sampler2D uTileMap;
        uniform sampler2D uSrcToOrigin;
        uniform sampler2D uOriginToDest;

        uniform mat2 uScale;

        void main() {
          gl_FragColor = texture2D(
            uTiles,
            uScale * (vTileMapCoordinate + texture2D(uOriginToDest, vTileMapCoordinate).xy) +
              texture2D(uSrcToOrigin, vec2(texture2D(uTileMap, vTileMapCoordinate).a)).xy
          );
        }
        """.trimIndent(),
    )

    linkProgram(program)

    if ((getProgramParameter(program, LINK_STATUS) as Boolean).not()) {
        window.alert("Unable to initialize the shader program: ${getProgramInfoLog(program)}")
        throw IllegalStateException(getProgramInfoLog(program))
    }

    val image = loadImage(tileSheetSrc)
    useProgram(program)

    val aVertex = getAttribLocation(program, "aVertex")
    val aCoordinate = getAttribLocation(program, "aCoordinate")
    val uClipMatrix = getUniformLocation(program, "uClipMatrix").assertNotNull("uClipMatrix location not found")

    // initialize fragment shader locations
    val uTiles = getUniformLocation(program, "uTiles").assertNotNull("uTiles location not found")
    val uTileMap = getUniformLocation(program, "uTileMap").assertNotNull("uTileMap location not found")

    val uSourceToOrigin = getUniformLocation(program, "uSrcToOrigin")
        .assertNotNull("uSrcToOrigin location not found")

    val uOriginToDestination = getUniformLocation(program, "uOriginToDest")
        .assertNotNull("uOriginToDest location not found")

    val uScale = getUniformLocation(program, "uScale").assertNotNull("uScale location not found")

    // initialize texture coordinate array buffer
    val coordBuffer = createBuffer().assertNotNull("createBuffer() failed")
    bindBuffer(ARRAY_BUFFER, coordBuffer)

    bufferData(
        ARRAY_BUFFER,
        arrayOf(
            0f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f,
        )
            .let { Float32Array(it) },
        STATIC_DRAW,
    )

    // initialize tiles element array buffer
    val elementBuffer = createBuffer().assertNotNull("createBuffer() failed")
    bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer)

    bufferData(
        ELEMENT_ARRAY_BUFFER,
        arrayOf<Short>(0, 1, 2, 3).let { Uint16Array(it) },
        STATIC_DRAW,
    )

    val tilesTexture = createTexture().assertNotNull("createTexture() failed")
    setTextureUniform(uTiles, tilesTexture, unit = TEXTURE0, x = 0)
    setTextureParameters()

    texImage2D(
        target = TEXTURE_2D,
        level = 0,             // level
        internalformat = RGBA, // internalFormat
        format = RGBA,         // srcFormat
        type = UNSIGNED_BYTE,  // srcType,
        source = image,
    )

    generateMipmap(TEXTURE_2D)

    val tileMapTexture = createTexture().assertNotNull("createTexture() failed")
    val originToDestinationTexture = createTexture().assertNotNull("createTexture() failed")

    // initialize source to origin texture
    val sourceToOriginTexture = createTexture().assertNotNull("createTexture() failed")
    val sourceToOriginArray = Float32Array(tilesCount * 3)

    for (y in 0..<tileSheetHeight) {
        for (x in 0..<tileSheetWidth) {
            sourceToOriginArray[(tileInd(x, y) * 3)] = x.toFloat() / tileSheetWidth.toFloat()      // s offset
            sourceToOriginArray[(tileInd(x, y) * 3) + 1] = y.toFloat() / tileSheetHeight.toFloat() // t offset
        }
    }

    bindTexture(TEXTURE_2D, sourceToOriginTexture)

    texImage2D(
        target = TEXTURE_2D,
        level = 0,
        internalformat = RGB,
        width = tilesCount,
        height = 1,
        border = 0,
        format = RGB,
        type = FLOAT,
        pixels = sourceToOriginArray,
    )

    setTextureParameters()

    // initialize origin to destination texture
    val originToDestinationArray = Float32Array((worldWidth * worldHeight) * 3)

    for (y in 0..<worldHeight) {
        for (x in 0..<worldWidth) {
            originToDestinationArray[(ind(x, y) * 3)] = -x.toFloat() / worldWidth.toFloat()      // s offset
            originToDestinationArray[(ind(x, y) * 3) + 1] = -y.toFloat() / worldHeight.toFloat() // t offset
        }
    }

    bindTexture(TEXTURE_2D, originToDestinationTexture)

    texImage2D(
        target = TEXTURE_2D,
        level = 0,
        internalformat = RGB,
        width = worldWidth,
        height = worldHeight,
        border = 0,
        format = RGB,
        type = FLOAT,
        pixels = originToDestinationArray,
    )

    setTextureParameters()

    // set tiles vertex position buffer
    val tilesVertexBuffer = createBuffer().assertNotNull("createBuffer() failed")
    bindBuffer(ARRAY_BUFFER, tilesVertexBuffer)

    bufferData(
        ARRAY_BUFFER,
        arrayOf(
            0f, 0f,
            worldWidth.toFloat(), 0f,
            worldWidth.toFloat(), worldHeight.toFloat(),
            0f, worldHeight.toFloat(),
        )
            .let { Float32Array(it) },
        STATIC_DRAW,
    )

    fun(clipMatrix: M4, tileArray: TileArray) {
        useProgram(program)
        disable(BLEND)

        bindTexture(TEXTURE_2D, tileMapTexture)
        pixelStorei(UNPACK_ALIGNMENT, 1)

        // update tile map texture
        texImage2D(
            target = TEXTURE_2D,
            level = 0,
            internalformat = ALPHA,
            width = worldWidth,
            height = worldHeight,
            border = 0,
            format = ALPHA,
            type = UNSIGNED_BYTE,
            pixels = tileArray.tiles,
        )

        setTextureParameters()

        // update uniforms
        uniformMatrix4fv(location = uClipMatrix, transpose = false, clipMatrix.array)

        setTextureUniform(location = uTiles, texture = tilesTexture, unit = TEXTURE0, x = 0)
        setTextureUniform(location = uTileMap, texture = tileMapTexture, unit = TEXTURE0, x = 1)
        setTextureUniform(
            location = uOriginToDestination,
            texture = originToDestinationTexture,
            unit = TEXTURE0,
            x = 2,
        )
        setTextureUniform(location = uSourceToOrigin, texture = sourceToOriginTexture, unit = TEXTURE0, x = 3)

        uniformMatrix2fv(
            location = uScale,
            transpose = false,
            arrayOf(
                (worldWidth.toFloat() / tileSheetWidth.toFloat()), 0f,
                0f, (worldHeight.toFloat() / tileSheetHeight.toFloat()),
            ),
        )

        bindBuffer(ARRAY_BUFFER, tilesVertexBuffer)

        // set vertex position attribute
        vertexAttribPointer(
            index = aVertex,
            size = 2,
            type = FLOAT,
            normalized = false,
            stride = 0,
            offset = 0,
        )

        enableVertexAttribArray(aVertex)

        bindBuffer(ARRAY_BUFFER, coordBuffer)

        // set texture coordinate attribute
        vertexAttribPointer(
            index = aCoordinate,
            size = 2,
            type = FLOAT,
            normalized = false,
            stride = 0,
            offset = 0,
        )

        enableVertexAttribArray(aCoordinate)

        // set element buffer
        bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer)

        // draw tiles
        drawElements(
            mode = TRIANGLE_FAN,
            count = 4,
            type = UNSIGNED_SHORT,
            offset = 0,
        )
    }
}

typealias SpriteProgram = (M4, List<SpriteInstance>) -> Unit

// generator could have been used but cannot type check because of multiple yield types
fun WebGLRenderingContext.createSpriteProgram(
    coroutineScope: CoroutineScope,
): Deferred<SpriteProgram> = coroutineScope.async {
    val program = createProgram().assertNotNull("createProgram() failed")

    createShader(
        program,
        type = VERTEX_SHADER,
        """
        attribute vec4 aVertex;
        attribute vec2 aCoord;

        uniform mat4 uClipMatrix;

        varying highp vec2 vCoord;

        void main () {
            gl_Position = uClipMatrix * aVertex;
            vCoord = aCoord;
        }
        """.trimIndent(),
    )

    createShader(
        program,
        type = FRAGMENT_SHADER,
        """
        precision highp float;
        varying vec2 vCoord;

        uniform sampler2D uTexture;

        void main () {
            gl_FragColor = texture2D(
                uTexture,
                vCoord
            );
        }
        """.trimIndent(),
    )

    linkProgram(program)

    if ((getProgramParameter(program, LINK_STATUS) as Boolean).not()) {
        window.alert("linkProgram() failed: ${getProgramInfoLog(program)}")
        throw IllegalStateException()
    }

    val image = loadImage(spriteSheetSrc)
    useProgram(program)

    // initialize vertex shader locations
    val aVertex = getAttribLocation(program, "aVertex")
    val aCoord = getAttribLocation(program, "aCoord")
    val uClipMatrix = getUniformLocation(program, "uClipMatrix").assertNotNull("uClipMatrix location not found")

    // initialize fragment shader locations
    val uTexture = getUniformLocation(program, "uTexture").assertNotNull("uTexture location not found")

    // load texture
    val texture = createTexture().assertNotNull("createTexture() failed")
    setTextureUniform(uTexture, texture, unit = TEXTURE0, x = 0)
    setTextureParameters()

    texImage2D(
        target = TEXTURE_2D,
        level = 0,             // level
        internalformat = RGBA, // internalFormat
        format = RGBA,         // srcFormat
        type = UNSIGNED_BYTE,  // srcType,
        source = image,
    )

    generateMipmap(TEXTURE_2D)

    val vertexBuffer = createBuffer().assertNotNull("createBuffer() failed")
    val coordBuffer = createBuffer().assertNotNull("createBuffer() failed")

    // initialize element array buffer
    val elementBuffer = createBuffer().assertNotNull("createBuffer() failed")

    fun(clipMatrix: M4, sprites: List<SpriteInstance>) {
        val (vertex, coordinate, element) = spriteToBuffer(sprites)
        useProgram(program)
        enable(BLEND)

        // set uniforms
        uniformMatrix4fv(location = uClipMatrix, transpose = false, clipMatrix.array)
        setTextureUniform(location = uTexture, texture = texture, unit = TEXTURE0, x = 0)

        bindBuffer(ARRAY_BUFFER, vertexBuffer)

        // update vertex position buffer
        bufferData(
            target = ARRAY_BUFFER,
            data = vertex,
            usage = STATIC_DRAW,
        )

        // set vertex position attribute
        vertexAttribPointer(
            index = aVertex,
            size = 2,
            type = FLOAT,
            normalized = false,
            stride = 0,
            offset = 0,
        )

        enableVertexAttribArray(aVertex)

        bindBuffer(ARRAY_BUFFER, coordBuffer)

        // update texture coordinate buffer
        bufferData(
            target = ARRAY_BUFFER,
            data = coordinate,
            usage = STATIC_DRAW,
        )

        // set texture coordinate attribute
        vertexAttribPointer(
            index = aCoord,
            size = 2,
            type = FLOAT,
            normalized = false,
            stride = 0,
            offset = 0,
        )

        enableVertexAttribArray(aCoord)

        bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer)

        bufferData(
            target = ELEMENT_ARRAY_BUFFER,
            data = element,
            usage = STATIC_DRAW,
        )

        // draw sprites
        drawElements(
            mode = TRIANGLES,
            count = element.length,
            type = UNSIGNED_SHORT,
            offset = 0,
        )
    }
}

private fun WebGLRenderingContext.createShader(program: WebGLProgram, type: Int, source: String) {
    val vertexShader = createShader(type)
    shaderSource(vertexShader, source)
    compileShader(vertexShader)

    if ((getShaderParameter(vertexShader, WebGLRenderingContext.COMPILE_STATUS) as Boolean).not()) {
        throw IllegalStateException(getShaderInfoLog(vertexShader))
    }

    attachShader(program, vertexShader)
}

private fun WebGLRenderingContext.setTextureParameters() {
    texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST)
    texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)
}

private fun spriteInd(x: Int, y: Int): Int = (spriteSheetWidth * y) + x

private fun spriteToBuffer(sprites: List<SpriteInstance>): Triple<Float32Array, Float32Array, Uint16Array> {
    val vertex = Float32Array(sprites.size * 8)
    val coordinate = Float32Array(sprites.size * 8)
    val element = Uint16Array(sprites.size * 6)

    for (i in sprites.indices) {
        val x: Float = sprites[i].x
        val y: Float = worldHeight.toFloat() - sprites[i].y
        val sprite: Sprite = sprites[i].sprite

        val s: Float = ((sprite.int.toFloat() % spriteSheetWidth.toFloat()) / spriteSheetWidth.toFloat())
        val t: Float = floor(sprite.int.toFloat() / spriteSheetWidth.toFloat()) / spriteSheetHeight.toFloat()

        vertex[(i * 8) + 0] = x - .5f
        vertex[(i * 8) + 1] = y - .5f
        vertex[(i * 8) + 2] = x + .5f
        vertex[(i * 8) + 3] = y - .5f
        vertex[(i * 8) + 4] = x + .5f
        vertex[(i * 8) + 5] = y + .5f
        vertex[(i * 8) + 6] = x - .5f
        vertex[(i * 8) + 7] = y + .5f

        coordinate[(i * 8) + 0] = s
        coordinate[(i * 8) + 1] = t + (1f / spriteSheetHeight.toFloat())
        coordinate[(i * 8) + 2] = s + (1f / spriteSheetWidth.toFloat())
        coordinate[(i * 8) + 3] = t + (1f / spriteSheetHeight.toFloat())
        coordinate[(i * 8) + 4] = s + (1f / spriteSheetWidth.toFloat())
        coordinate[(i * 8) + 5] = t
        coordinate[(i * 8) + 6] = s
        coordinate[(i * 8) + 7] = t

        element[(i * 6) + 0] = (i * 4).toShort()
        element[(i * 6) + 1] = ((i * 4) + 1).toShort()
        element[(i * 6) + 2] = ((i * 4) + 2).toShort()
        element[(i * 6) + 3] = (i * 4).toShort()
        element[(i * 6) + 4] = ((i * 4) + 2).toShort()
        element[(i * 6) + 5] = ((i * 4) + 3).toShort()
    }

    return Triple(vertex, coordinate, element)
}

fun WebGLRenderingContext.setTextureUniform(location: WebGLUniformLocation, texture: WebGLTexture, unit: Int, x: Int) {
    activeTexture(unit + x)
    bindTexture(TEXTURE_2D, texture)
    uniform1i(location, x)
}
