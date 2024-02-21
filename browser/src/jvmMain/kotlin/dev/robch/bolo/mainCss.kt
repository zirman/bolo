package dev.robch.bolo

import client.canvasId
import client.statusPanelId
import kotlinx.css.Border
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.border
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.opacity
import kotlinx.css.position
import kotlinx.css.top
import kotlinx.css.width

fun CssBuilder.mainCss() {
    rule("#$canvasId") {
        position = Position.fixed
        backgroundColor = Color.magenta
        top = LinearDimension("0")
        left = LinearDimension("0")
        width = LinearDimension("100%")
        height = LinearDimension("100%")
    }

    rule("#$statusPanelId") {
        position = Position.fixed
        top = LinearDimension("0")
        left = LinearDimension("0")
        width = LinearDimension("33%")
        border = Border(
            width = LinearDimension("3px"),
            style = BorderStyle.solid,
            color = Color.black,
        )
        opacity = 0.95
    }

    rule("#$statusPanelId progress") {
        width = LinearDimension("100%")
        border = Border(
            width = LinearDimension("3px"),
            style = BorderStyle.solid,
            color = Color.black,
        )
    }
}
