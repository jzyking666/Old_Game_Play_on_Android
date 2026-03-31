package com.retro.arcade.feature.dino.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

data class DinoSpriteRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

object OriginalDinoSpriteSheet {
    val cloud = DinoSpriteRect(x = 86, y = 2, width = 46, height = 14)
    val moonBase = DinoSpriteRect(x = 484, y = 2, width = 20, height = 40)
    val starA = DinoSpriteRect(x = 645, y = 2, width = 9, height = 9)
    val starB = DinoSpriteRect(x = 645, y = 11, width = 9, height = 9)

    val pterodactylA = DinoSpriteRect(x = 134, y = 2, width = 46, height = 40)
    val pterodactylB = DinoSpriteRect(x = 180, y = 2, width = 46, height = 40)

    fun smallCactus(size: Int): DinoSpriteRect {
        val width = 17 * size
        val x = ((17 * size) * 0.5f * (size - 1)).roundToInt() + 228
        return DinoSpriteRect(x = x, y = 2, width = width, height = 35)
    }

    fun largeCactus(size: Int): DinoSpriteRect {
        val width = 25 * size
        val x = ((25 * size) * 0.5f * (size - 1)).roundToInt() + 332
        return DinoSpriteRect(x = x, y = 2, width = width, height = 50)
    }

    val trexIdleA = DinoSpriteRect(x = 892, y = 2, width = 44, height = 47)
    val trexIdleB = DinoSpriteRect(x = 848, y = 2, width = 44, height = 47)
    val trexRunA = DinoSpriteRect(x = 936, y = 2, width = 44, height = 47)
    val trexRunB = DinoSpriteRect(x = 980, y = 2, width = 44, height = 47)
    val trexCrash = DinoSpriteRect(x = 1068, y = 2, width = 44, height = 47)
    val trexJump = DinoSpriteRect(x = 848, y = 2, width = 44, height = 47)
    val trexDuckA = DinoSpriteRect(x = 1112, y = 2, width = 59, height = 47)
    val trexDuckB = DinoSpriteRect(x = 1171, y = 2, width = 59, height = 47)

    val horizonA = DinoSpriteRect(x = 2, y = 52, width = 600, height = 12)
    val horizonB = DinoSpriteRect(x = 602, y = 52, width = 600, height = 12)

    val restartFrames = List(8) { index ->
        DinoSpriteRect(x = 2 + (36 * index), y = 68, width = 36, height = 32)
    }
    val gameOver = DinoSpriteRect(x = 655, y = 15, width = 191, height = 11)

    fun moon(phaseIndex: Int): DinoSpriteRect {
        val offsets = listOf(140, 120, 100, 60, 40, 20, 0)
        val x = 484 + offsets[phaseIndex % offsets.size]
        val width = if (phaseIndex % offsets.size == 3) 40 else 20
        return DinoSpriteRect(x = x, y = 2, width = width, height = 40)
    }

    fun star(index: Int): DinoSpriteRect {
        return if (index % 2 == 0) starA else starB
    }

    fun digit(char: Char): DinoSpriteRect {
        val spriteIndex = when (char) {
            '0' -> 0
            '1' -> 1
            '2' -> 2
            '3' -> 3
            '4' -> 4
            '5' -> 5
            '6' -> 6
            '7' -> 7
            '8' -> 8
            '9' -> 9
            'H' -> 10
            'I' -> 11
            else -> 0
        }
        return DinoSpriteRect(
            x = 655 + (spriteIndex * 10),
            y = 2,
            width = 10,
            height = 13
        )
    }
}

fun DrawScope.drawSprite(
    image: ImageBitmap,
    sprite: DinoSpriteRect,
    dstTopLeft: Offset,
    dstWidth: Float,
    dstHeight: Float,
    tint: Color,
    alpha: Float = 1f
) {
    drawImage(
        image = image,
        srcOffset = IntOffset(sprite.x, sprite.y),
        srcSize = IntSize(sprite.width, sprite.height),
        dstOffset = IntOffset(dstTopLeft.x.roundToInt(), dstTopLeft.y.roundToInt()),
        dstSize = IntSize(
            width = max(1, dstWidth.roundToInt()),
            height = max(1, dstHeight.roundToInt())
        ),
        alpha = alpha,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn)
    )
}
