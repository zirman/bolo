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
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.BLEND
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAGMENT_SHADER
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.RGB
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLE_FAN
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_ALIGNMENT
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER
import org.khronos.webgl.set

fun WebGLRenderingContext.createTileProgram(
    coroutineScope: CoroutineScope,
): Deferred<TileProgram> = coroutineScope.async {
    val image = loadImage(tileSheetSrc)
    val program = createProgram().assertNotNull("createProgram() returned null")

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

    if ((getProgramParameter(program, LINK_STATUS) as JsBoolean).toBoolean().not()) {
        window.alert("Unable to initialize the shader program: ${getProgramInfoLog(program)}")
        throw IllegalStateException(getProgramInfoLog(program))
    }

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

    fun(clipMatrix: M4, tileArray: ImageTileArray) {
        useProgram(program)
        disable(BLEND)

        bindTexture(TEXTURE_2D, tileMapTexture)
        pixelStorei(UNPACK_ALIGNMENT, 1)

        // update tile map texture
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        texImage2D(
            target = TEXTURE_2D,
            level = 0,
            internalformat = ALPHA,
            width = worldWidth,
            height = worldHeight,
            border = 0,
            format = ALPHA,
            type = UNSIGNED_BYTE,
            pixels = tileArray.uint8Array as ArrayBufferView,
        )

        setTextureParameters()

        // update uniforms
        uniformMatrix4fv(location = uClipMatrix, transpose = false, float32ArrayOf(*clipMatrix.array))

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
            float32ArrayOf(
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
