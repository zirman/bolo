package adapters

import assert.assertNotNull
import bmap.WORLD_HEIGHT
import bmap.WORLD_WIDTH
import bmap.ind
import client.ImageTileArray
import client.SPRITE_SHEET_SRC
import client.SpriteInstance
import client.SpriteProgram
import client.TILES_COUNT
import client.TILE_SHEET_HEIGHT
import client.TILE_SHEET_SRC
import client.TILE_SHEET_WIDTH
import client.TileProgram
import client.imageTileIndex
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
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLE_FAN
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_ALIGNMENT
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER
import org.khronos.webgl.set

class WebGlRenderingContextAdapterImpl(
    private val webGlRenderingContext: WebGLRenderingContext,
) : WebGlRenderingContextAdapter {
    override fun tileProgramFactory(scope: CoroutineScope): Deferred<TileProgram> =
        webGlRenderingContext.tileProgramFactory(scope)

    override fun spriteProgramFactory(scope: CoroutineScope): Deferred<SpriteProgram> =
        webGlRenderingContext.spriteProgramFactory(scope)

    private fun WebGLRenderingContext.tileProgramFactory(
        scope: CoroutineScope,
    ): Deferred<TileProgram> = scope.async {
        val image = loadImage(TILE_SHEET_SRC)
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

        if ((getProgramParameter(program, LINK_STATUS) as Boolean).not()) {
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
        webGlRenderingContext.setTextureUniform(uTiles, tilesTexture, unit = TEXTURE0, x = 0)
        webGlRenderingContext.setTextureParameters()

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
        val sourceToOriginArray = Float32Array(TILES_COUNT * 3)

        for (y in 0..<TILE_SHEET_HEIGHT) {
            for (x in 0..<TILE_SHEET_WIDTH) {
                sourceToOriginArray[(imageTileIndex(x, y) * 3)] =
                    x.toFloat() / TILE_SHEET_WIDTH.toFloat()  // s offset
                sourceToOriginArray[(imageTileIndex(x, y) * 3) + 1] =
                    y.toFloat() / TILE_SHEET_HEIGHT.toFloat() // t offset
            }
        }

        bindTexture(TEXTURE_2D, sourceToOriginTexture)

        texImage2D(
            target = TEXTURE_2D,
            level = 0,
            internalformat = RGB,
            width = TILES_COUNT,
            height = 1,
            border = 0,
            format = RGB,
            type = FLOAT,
            pixels = sourceToOriginArray,
        )

        setTextureParameters()

        // initialize origin to destination texture
        val originToDestinationArray = Float32Array((WORLD_WIDTH * WORLD_HEIGHT) * 3)

        for (y in 0..<WORLD_HEIGHT) {
            for (x in 0..<WORLD_WIDTH) {
                originToDestinationArray[(ind(x, y) * 3)] = -x.toFloat() / WORLD_WIDTH.toFloat()      // s offset
                originToDestinationArray[(ind(x, y) * 3) + 1] = -y.toFloat() / WORLD_HEIGHT.toFloat() // t offset
            }
        }

        bindTexture(TEXTURE_2D, originToDestinationTexture)

        texImage2D(
            target = TEXTURE_2D,
            level = 0,
            internalformat = RGB,
            width = WORLD_WIDTH,
            height = WORLD_HEIGHT,
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
                WORLD_WIDTH.toFloat(), 0f,
                WORLD_WIDTH.toFloat(), WORLD_HEIGHT.toFloat(),
                0f, WORLD_HEIGHT.toFloat(),
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
                width = WORLD_WIDTH,
                height = WORLD_HEIGHT,
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
                    (WORLD_WIDTH.toFloat() / TILE_SHEET_WIDTH.toFloat()), 0f,
                    0f, (WORLD_HEIGHT.toFloat() / TILE_SHEET_HEIGHT.toFloat()),
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

    private fun WebGLRenderingContext.spriteProgramFactory(
        scope: CoroutineScope,
    ): Deferred<SpriteProgram> = scope.async {
        val image = loadImage(SPRITE_SHEET_SRC)
        val program = createProgram().assertNotNull("createProgram() returned null")

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
            uniformMatrix4fv(location = uClipMatrix, transpose = false, float32ArrayOf(*clipMatrix.array))
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
}
