package dev.robch.bolo

import client.armorId
import client.canvasId
import client.minesId
import client.shellsId
import client.statusPanelId
import kotlinx.html.BODY
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.progress
import kotlinx.html.script
import kotlinx.html.title

fun HTML.mainHtml() {
    head {
        title { +"Bolo" }
        meta(
            name = "description",
            content =
            """
            Bolo is a classic multiplayer tank game that gained popularity in the 1980s, particularly on the Apple II platform. Developed by Stuart Cheshire, it offered an innovative and engaging gaming experience for its time. In Bolo, players controlled colorful tanks navigating through maze-like terrains, engaging in intense battles against opponents.
            The game was renowned for its addictive multiplayer mode, allowing multiple players to compete against each other in real-time, either over a local network or via modem connections. This multiplayer aspect added a layer of excitement and competitiveness, as players strategized and maneuvered their tanks to outwit and outgun their adversaries.
            Bolo featured simple yet intuitive controls, allowing players to move their tanks across the landscape, fire projectiles at opponents, and strategically position themselves for tactical advantage. The graphics, while basic by today's standards, were vibrant and functional, effectively conveying the game's action-packed gameplay.
            One of the unique aspects of Bolo was its dynamic environment, with destructible terrain adding an element of unpredictability to battles. Players could strategically blast through walls and barriers to create pathways or trap opponents, adding depth to the gameplay and requiring thoughtful planning and adaptability.
            Despite its simple graphics and relatively limited hardware capabilities of the time, Bolo captivated players with its fast-paced gameplay, strategic depth, and intense multiplayer action. It remains a beloved classic among retro gaming enthusiasts, fondly remembered for its innovation and entertainment value in the early days of multiplayer gaming.
            """.trimIndent(),
        )
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        link(href = "/styles.css", rel = "stylesheet", type = "text/css")
        link(href = "/tile_sheet.png", rel = "prefetch", type = "image/png")
        link(href = "/sprite_sheet.png", rel = "prefetch", type = "image/png")
    }

    body {
        canvas { id = canvasId }
        statusPanel()
        script { src = "/bolo.js" }
    }
}

fun BODY.statusPanel() {
    div {
        id = statusPanelId
        statusBar(id = shellsId)
        statusBar(id = minesId)
        statusBar(id = armorId)
    }
}

fun DIV.statusBar(id: String) {
    progress {
        this.id = id
        value = "0"
        max = "1"
    }
}
