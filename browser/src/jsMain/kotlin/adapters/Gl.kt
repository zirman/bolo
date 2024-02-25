package adapters

import bmap.WORLD_HEIGHT
import client.SPRITE_SHEET_HEIGHT
import client.SPRITE_SHEET_WIDTH
import client.Sprite
import client.SpriteInstance
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.COMPILE_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.NEAREST
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MAG_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MIN_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_S
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_T
import org.khronos.webgl.WebGLRenderingContextBase
import org.khronos.webgl.WebGLTexture
import org.khronos.webgl.WebGLUniformLocation
import org.khronos.webgl.set
import kotlin.math.floor

fun WebGLRenderingContext.createShader(program: WebGLProgram, type: Int, source: String) {
    val vertexShader = createShader(type)
    shaderSource(vertexShader, source)
    compileShader(vertexShader)

    if ((getShaderParameter(vertexShader, COMPILE_STATUS) as Boolean).not()) {
        throw IllegalStateException(getShaderInfoLog(vertexShader))
    }

    attachShader(program, vertexShader)
}

fun WebGLRenderingContext.setTextureParameters() {
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
        val y: Float = WORLD_HEIGHT.toFloat() - sprites[i].y
        val sprite: Sprite = sprites[i].sprite

        val s: Float = ((sprite.int.toFloat().mod(SPRITE_SHEET_WIDTH.toFloat())) / SPRITE_SHEET_WIDTH.toFloat())
        val t: Float = floor(sprite.int.toFloat() / SPRITE_SHEET_HEIGHT.toFloat()) / SPRITE_SHEET_HEIGHT.toFloat()

        vertex[(i * 8) + 0] = x - .5f
        vertex[(i * 8) + 1] = y - .5f
        vertex[(i * 8) + 2] = x + .5f
        vertex[(i * 8) + 3] = y - .5f
        vertex[(i * 8) + 4] = x + .5f
        vertex[(i * 8) + 5] = y + .5f
        vertex[(i * 8) + 6] = x - .5f
        vertex[(i * 8) + 7] = y + .5f

        coordinate[(i * 8) + 0] = s
        coordinate[(i * 8) + 1] = t + (1f / SPRITE_SHEET_HEIGHT.toFloat())
        coordinate[(i * 8) + 2] = s + (1f / SPRITE_SHEET_WIDTH.toFloat())
        coordinate[(i * 8) + 3] = t + (1f / SPRITE_SHEET_HEIGHT.toFloat())
        coordinate[(i * 8) + 4] = s + (1f / SPRITE_SHEET_WIDTH.toFloat())
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

fun WebGLRenderingContextBase.setTextureUniform(
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
