package client

import assert.assertNotNull
import bmap.ind
import bmap.tileInd
import bmap.tileSheetHeight
import bmap.tileSheetWidth
import bmap.tilesCount
import bmap.worldHeight
import bmap.worldWidth
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import math.M4
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.BLEND
import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.COMPILE_STATUS
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
import kotlin.math.floor

typealias TileProgram = (clipMatrix: M4, tileArray: ImageTileArrayImpl) -> Unit

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
        float32ArrayOf(
            0f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f,
        ),
        STATIC_DRAW,
    )

    // initialize tiles element array buffer
    val elementBuffer = createBuffer().assertNotNull("createBuffer() failed")
    bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer)

    bufferData(
        ELEMENT_ARRAY_BUFFER,
        uint16ArrayOf(0u, 1u, 2u, 3u),
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
        float32ArrayOf(
            0f, 0f,
            worldWidth.toFloat(), 0f,
            worldWidth.toFloat(), worldHeight.toFloat(),
            0f, worldHeight.toFloat(),
        ),
        STATIC_DRAW,
    )

    fun(clipMatrix: M4, tileArray: ImageTileArrayImpl) {
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
            pixels = tileArray.imageTiles,
        )

        setTextureParameters()

        // update uniforms
        uniformMatrix4fv(location = uClipMatrix, transpose = false, clipMatrix.array.toTypedArray())

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
        uniformMatrix4fv(location = uClipMatrix, transpose = false, clipMatrix.array.toTypedArray())
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

    if ((getShaderParameter(vertexShader, COMPILE_STATUS) as Boolean).not()) {
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

fun spriteToBuffer(sprites: List<SpriteInstance>): Triple<Float32Array, Float32Array, Uint16Array> {
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

fun WebGLRenderingContext.setTextureUniform(
    location: WebGLUniformLocation,
    texture: WebGLTexture,
    unit: Int, x: Int,
) {
    activeTexture(unit + x)
    bindTexture(TEXTURE_2D, texture)
    uniform1i(location, x)
}

fun float32ArrayOf(vararg fs: Float) = Float32Array(fs.size).apply {
    fs.forEachIndexed { index, fl ->
        this[index] = fl
    }
}

fun uint16ArrayOf(vararg ss: UShort) = Uint16Array(ss.size).apply {
    ss.forEachIndexed { index, fl ->
        this[index] = fl.toShort()
    }
}
