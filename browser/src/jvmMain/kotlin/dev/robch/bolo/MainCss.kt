package dev.robch.bolo

import client.builderModePanelId
import client.canvasId
import client.statusPanelId
import kotlinx.css.Border
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Cursor
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.border
import kotlinx.css.cursor
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.opacity
import kotlinx.css.paddingRight
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.top
import kotlinx.css.width

fun CssBuilder.mainCss() {
    rule("#$canvasId") {
        position = Position.fixed
        backgroundColor = Color.magenta
        cursor = Cursor.crosshair
        top = 0.px
        left = 0.px
        width = 100.pct
        height = 100.pct
    }

    rule("#$statusPanelId") {
        position = Position.fixed
        top = 0.px
        left = 0.px
        width = 256.px
        opacity = 0.95
    }

    rule("#$statusPanelId progress") {
        width = 100.pct
        border = Border(
            width = 3.px,
            style = BorderStyle.solid,
            color = Color.black,
        )
    }

    rule("#$builderModePanelId") {
        backgroundColor = Color.whiteSmoke
        width = LinearDimension.fitContent
        border = Border(
            width = 3.px,
            style = BorderStyle.solid,
            color = Color.black,
        )
    }

    rule("#$builderModePanelId label") {
        paddingRight = 4.px
    }
}
