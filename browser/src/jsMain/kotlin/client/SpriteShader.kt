package client

import assert.assertNotNull
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import math.M4
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.BLEND
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAGMENT_SHADER
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.STATIC_DRAW
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE0
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER

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
