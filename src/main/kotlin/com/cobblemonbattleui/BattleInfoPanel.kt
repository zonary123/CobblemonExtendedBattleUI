package com.cobblemonbattleui

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.util.UUID

/**
 * Pokemon Scarlet/Violet inspired battle information panel.
 * Features draggable positioning, scroll wheel scaling, and clean arrow-based stat display.
 */
object BattleInfoPanel {

    // Panel state
    var isExpanded: Boolean = false
        private set

    // Input tracking
    private var wasKeyPressed: Boolean = false
    private var wasMousePressed: Boolean = false
    private var lastScrollY: Double = 0.0

    // Dragging state
    private var isDragging: Boolean = false
    private var dragOffsetX: Int = 0
    private var dragOffsetY: Int = 0
    private var dragStartX: Int = 0
    private var dragStartY: Int = 0
    private var hasDragged: Boolean = false
    private const val DRAG_THRESHOLD = 5  // Pixels moved before considered a drag

    // Colors
    private val PANEL_BG = color(22, 27, 34, 240)
    private val HEADER_BG = color(30, 37, 46, 255)
    private val SECTION_BG = color(26, 32, 40, 255)
    private val CONTENT_BG = color(18, 22, 28, 200)
    private val BORDER_COLOR = color(55, 65, 80, 255)
    private val BORDER_HIGHLIGHT = color(80, 95, 115, 255)
    private val TEXT_WHITE = color(255, 255, 255, 255)
    private val TEXT_LIGHT = color(220, 225, 230, 255)
    private val TEXT_DIM = color(140, 150, 165, 255)
    private val TEXT_GOLD = color(255, 210, 80, 255)
    private val STAT_BOOST = color(255, 100, 100, 255)
    private val STAT_DROP = color(100, 160, 255, 255)
    private val STAT_NEUTRAL = color(160, 165, 175, 255)
    private val ACCENT_PLAYER = color(100, 200, 255, 255)
    private val ACCENT_OPPONENT = color(255, 130, 110, 255)
    private val ACCENT_FIELD = color(255, 200, 100, 255)

    // Layout
    private const val BASE_PANEL_MARGIN = 10
    private const val BASE_PADDING = 8
    private const val BASE_LINE_HEIGHT = 12
    private const val BASE_SECTION_GAP = 6
    private const val BASE_HEADER_HEIGHT = 22
    private const val BASE_PANEL_WIDTH = 200
    private const val BASE_CORNER_RADIUS = 4

    // Toggle key
    private const val TOGGLE_KEY = GLFW.GLFW_KEY_V

    // Cached panel bounds for input detection
    private var panelBoundsX = 0
    private var panelBoundsY = 0
    private var panelBoundsW = 0
    private var panelBoundsH = 0
    private var headerEndY = 0

    private fun scaled(base: Int): Int = (base * PanelConfig.scale).toInt()
    private fun scaled(base: Float): Float = base * PanelConfig.scale
    private fun color(r: Int, g: Int, b: Int, a: Int): Int = (a shl 24) or (r shl 16) or (g shl 8) or b

    fun toggle() {
        isExpanded = !isExpanded
        PanelConfig.setStartExpanded(isExpanded)
        PanelConfig.save()
    }

    fun initialize() {
        PanelConfig.load()
        isExpanded = PanelConfig.startExpanded
    }

    private fun handleInput(mc: MinecraftClient) {
        val handle = mc.window.handle

        val isKeyDown = GLFW.glfwGetKey(handle, TOGGLE_KEY) == GLFW.GLFW_PRESS
        if (isKeyDown && !wasKeyPressed) toggle()
        wasKeyPressed = isKeyDown

        val mouseX = (mc.mouse.x * mc.window.scaledWidth / mc.window.width).toInt()
        val mouseY = (mc.mouse.y * mc.window.scaledHeight / mc.window.height).toInt()
        val isMouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        val isOverPanel = mouseX >= panelBoundsX && mouseX <= panelBoundsX + panelBoundsW &&
                          mouseY >= panelBoundsY && mouseY <= panelBoundsY + panelBoundsH
        val isOverHeader = isOverPanel && mouseY <= headerEndY

        if (isMouseDown) {
            if (!wasMousePressed && isOverHeader) {
                isDragging = true
                hasDragged = false
                dragOffsetX = mouseX - panelBoundsX
                dragOffsetY = mouseY - panelBoundsY
                dragStartX = mouseX
                dragStartY = mouseY
            } else if (isDragging) {
                val deltaX = kotlin.math.abs(mouseX - dragStartX)
                val deltaY = kotlin.math.abs(mouseY - dragStartY)
                if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                    hasDragged = true
                }
                if (hasDragged) {
                    PanelConfig.setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY)
                }
            }
        } else {
            if (isDragging) {
                if (hasDragged) {
                    PanelConfig.save()
                } else {
                    toggle()
                }
                isDragging = false
                hasDragged = false
            }
        }
        wasMousePressed = isMouseDown
    }

    fun onScroll(mouseX: Double, mouseY: Double, deltaY: Double): Boolean {
        val mc = MinecraftClient.getInstance()
        val scaledX = (mouseX * mc.window.scaledWidth / mc.window.width).toInt()
        val scaledY = (mouseY * mc.window.scaledHeight / mc.window.height).toInt()

        val isOverPanel = scaledX >= panelBoundsX && scaledX <= panelBoundsX + panelBoundsW &&
                          scaledY >= panelBoundsY && scaledY <= panelBoundsY + panelBoundsH

        if (isOverPanel && deltaY != 0.0) {
            val delta = if (deltaY > 0) PanelConfig.SCALE_STEP else -PanelConfig.SCALE_STEP
            PanelConfig.adjustScale(delta)
            PanelConfig.save()
            return true  // Consume the scroll event
        }
        return false
    }

    fun render(context: DrawContext) {
        val battle = CobblemonClient.battle ?: return
        if (battle.minimised) return

        val mc = MinecraftClient.getInstance()
        handleInput(mc)

        val screenWidth = mc.window.scaledWidth
        val screenHeight = mc.window.scaledHeight

        // Register Pokemon for stat tracking
        val playerUUID = mc.player?.uuid ?: return
        val playerSide = if (battle.side1.actors.any { it.uuid == playerUUID }) battle.side1 else battle.side2
        val opponentSide = if (playerSide == battle.side1) battle.side2 else battle.side1

        val allPokemon = (playerSide.activeClientBattlePokemon + opponentSide.activeClientBattlePokemon)
            .mapNotNull { it.battlePokemon }

        for (pokemon in allPokemon) {
            BattleStateTracker.registerPokemon(pokemon.uuid, pokemon.displayName.string)
        }

        // Calculate panel dimensions
        val panelWidth = scaled(BASE_PANEL_WIDTH)
        val panelHeight = if (isExpanded) calculateExpandedHeight(allPokemon.size) else calculateCollapsedHeight()

        // Panel position (custom or default)
        val panelX = PanelConfig.panelX ?: (screenWidth - panelWidth - scaled(BASE_PANEL_MARGIN))
        val panelY = PanelConfig.panelY ?: ((screenHeight - panelHeight) / 2)

        // Clamp to screen bounds
        val clampedX = panelX.coerceIn(0, (screenWidth - panelWidth).coerceAtLeast(0))
        val clampedY = panelY.coerceIn(0, (screenHeight - panelHeight).coerceAtLeast(0))

        // Store bounds for input detection
        panelBoundsX = clampedX
        panelBoundsY = clampedY
        panelBoundsW = panelWidth
        panelBoundsH = panelHeight

        if (isExpanded) {
            renderExpanded(context, clampedX, clampedY, panelWidth, allPokemon.map { it.uuid to it.displayName.string })
        } else {
            renderCollapsed(context, clampedX, clampedY, panelWidth)
        }
    }

    private fun renderCollapsed(context: DrawContext, x: Int, y: Int, width: Int) {
        val height = calculateCollapsedHeight()
        val padding = scaled(BASE_PADDING)
        val lineHeight = scaled(BASE_LINE_HEIGHT)
        val headerHeight = scaled(BASE_HEADER_HEIGHT)

        // Panel background with border
        drawRoundedRect(context, x, y, width, height, PANEL_BG, BORDER_COLOR)

        // Header
        renderHeader(context, x, y, width)

        var currentY = y + headerHeight + padding / 2

        // Field conditions with icon, name, and turns
        BattleStateTracker.weather?.let { w ->
            val turns = BattleStateTracker.getWeatherTurnsRemaining() ?: "?"
            drawText(context, "${w.type.icon} ${w.type.displayName}", (x + padding).toFloat(), currentY.toFloat(), ACCENT_FIELD, scaled(0.8f))
            drawText(context, turns, (x + width - padding - scaled(turns.length * 5)).toFloat(), currentY.toFloat(), TEXT_DIM, scaled(0.75f))
            currentY += lineHeight
        }

        BattleStateTracker.terrain?.let { t ->
            val turns = BattleStateTracker.getTerrainTurnsRemaining() ?: "?"
            drawText(context, "${t.type.icon} ${t.type.displayName}", (x + padding).toFloat(), currentY.toFloat(), ACCENT_FIELD, scaled(0.8f))
            drawText(context, turns, (x + width - padding - scaled(turns.length * 5)).toFloat(), currentY.toFloat(), TEXT_DIM, scaled(0.75f))
            currentY += lineHeight
        }

        BattleStateTracker.getFieldConditions().forEach { (type, _) ->
            val turns = BattleStateTracker.getFieldConditionTurnsRemaining(type) ?: "?"
            drawText(context, "${type.icon} ${type.displayName}", (x + padding).toFloat(), currentY.toFloat(), ACCENT_FIELD, scaled(0.8f))
            drawText(context, turns, (x + width - padding - scaled(turns.length * 5)).toFloat(), currentY.toFloat(), TEXT_DIM, scaled(0.75f))
            currentY += lineHeight
        }

        // Side condition counts
        val playerConds = BattleStateTracker.getPlayerSideConditions()
        val oppConds = BattleStateTracker.getOpponentSideConditions()

        if (playerConds.isNotEmpty()) {
            drawText(context, "Ally: ${playerConds.size} effect${if (playerConds.size > 1) "s" else ""}",
                (x + padding).toFloat(), currentY.toFloat(), ACCENT_PLAYER, scaled(0.8f))
            currentY += (lineHeight * 0.9).toInt()
        }

        if (oppConds.isNotEmpty()) {
            drawText(context, "Enemy: ${oppConds.size} effect${if (oppConds.size > 1) "s" else ""}",
                (x + padding).toFloat(), currentY.toFloat(), ACCENT_OPPONENT, scaled(0.8f))
            currentY += (lineHeight * 0.9).toInt()
        }

        // Instructions
        currentY = y + height - padding - scaled(8)
        drawText(context, "V: expand | Drag: move | Scroll: size",
            (x + padding).toFloat(), currentY.toFloat(), TEXT_DIM, scaled(0.6f))
    }

    private fun renderExpanded(context: DrawContext, x: Int, y: Int, width: Int, pokemon: List<Pair<UUID, String>>) {
        val height = calculateExpandedHeight(pokemon.size)
        val padding = scaled(BASE_PADDING)
        val lineHeight = scaled(BASE_LINE_HEIGHT)
        val headerHeight = scaled(BASE_HEADER_HEIGHT)
        val sectionGap = scaled(BASE_SECTION_GAP)

        // Panel background with border
        drawRoundedRect(context, x, y, width, height, PANEL_BG, BORDER_COLOR)

        // Header
        renderHeader(context, x, y, width)

        var currentY = y + headerHeight + sectionGap

        currentY = renderSection(context, x, currentY, width, "FIELD", ACCENT_FIELD) { sectionY ->
            var sy = sectionY
            var hasContent = false

            BattleStateTracker.weather?.let { w ->
                val turns = BattleStateTracker.getWeatherTurnsRemaining() ?: "?"
                drawConditionLine(context, x + padding, sy, width - padding * 2,
                    w.type.icon, w.type.displayName, turns, ACCENT_FIELD)
                sy += lineHeight
                hasContent = true
            }

            BattleStateTracker.terrain?.let { t ->
                val turns = BattleStateTracker.getTerrainTurnsRemaining() ?: "?"
                drawConditionLine(context, x + padding, sy, width - padding * 2,
                    t.type.icon, t.type.displayName, turns, ACCENT_FIELD)
                sy += lineHeight
                hasContent = true
            }

            BattleStateTracker.getFieldConditions().forEach { (type, _) ->
                val turns = BattleStateTracker.getFieldConditionTurnsRemaining(type) ?: "?"
                drawConditionLine(context, x + padding, sy, width - padding * 2,
                    type.icon, type.displayName, turns, ACCENT_FIELD)
                sy += lineHeight
                hasContent = true
            }

            if (!hasContent) {
                drawText(context, "None active", (x + padding).toFloat(), sy.toFloat(), TEXT_DIM, scaled(0.8f))
                sy += lineHeight
            }
            sy
        }

        currentY += sectionGap

        currentY = renderSection(context, x, currentY, width, "ALLY EFFECTS", ACCENT_PLAYER) { sectionY ->
            var sy = sectionY
            val conditions = BattleStateTracker.getPlayerSideConditions()

            if (conditions.isNotEmpty()) {
                conditions.forEach { (type, state) ->
                    val info = when {
                        BattleStateTracker.getSideConditionTurnsRemaining(true, type) != null ->
                            BattleStateTracker.getSideConditionTurnsRemaining(true, type)!!
                        state.stacks > 1 -> "x${state.stacks}"
                        else -> ""
                    }
                    drawConditionLine(context, x + padding, sy, width - padding * 2,
                        type.icon, type.displayName, info, ACCENT_PLAYER)
                    sy += lineHeight
                }
            } else {
                drawText(context, "None active", (x + padding).toFloat(), sy.toFloat(), TEXT_DIM, scaled(0.8f))
                sy += lineHeight
            }
            sy
        }

        currentY += sectionGap

        currentY = renderSection(context, x, currentY, width, "ENEMY EFFECTS", ACCENT_OPPONENT) { sectionY ->
            var sy = sectionY
            val conditions = BattleStateTracker.getOpponentSideConditions()

            if (conditions.isNotEmpty()) {
                conditions.forEach { (type, state) ->
                    val info = when {
                        BattleStateTracker.getSideConditionTurnsRemaining(false, type) != null ->
                            BattleStateTracker.getSideConditionTurnsRemaining(false, type)!!
                        state.stacks > 1 -> "x${state.stacks}"
                        else -> ""
                    }
                    drawConditionLine(context, x + padding, sy, width - padding * 2,
                        type.icon, type.displayName, info, ACCENT_OPPONENT)
                    sy += lineHeight
                }
            } else {
                drawText(context, "None active", (x + padding).toFloat(), sy.toFloat(), TEXT_DIM, scaled(0.8f))
                sy += lineHeight
            }
            sy
        }

        currentY += sectionGap

        currentY = renderSection(context, x, currentY, width, "STAT STAGES", TEXT_GOLD) { sectionY ->
            var sy = sectionY

            for ((uuid, name) in pokemon) {
                val stats = BattleStateTracker.getStatChanges(uuid)
                val changedStats = stats.entries.filter { it.value != 0 }

                // Pokemon name
                drawText(context, name, (x + padding).toFloat(), sy.toFloat(),
                    if (changedStats.isNotEmpty()) TEXT_WHITE else TEXT_DIM, scaled(0.85f))
                sy += (lineHeight * 0.95).toInt()

                if (changedStats.isNotEmpty()) {
                    val sortedStats = changedStats.sortedBy { getStatSortOrder(it.key.identifier.toString()) }
                    val maxWidth = width - padding * 2 - scaled(8)
                    val startX = x + padding + scaled(8)

                    // Draw each stat with wrapping
                    var statX = startX
                    for ((stat, value) in sortedStats) {
                        val abbr = getStatAbbr(stat.identifier.toString())
                        val arrows = if (value > 0) "↑".repeat(value) else "↓".repeat(-value)
                        val color = if (value > 0) STAT_BOOST else STAT_DROP

                        // Calculate width this entry will take
                        val entryWidth = scaled((abbr.length * 5 + 2 + arrows.length * 5 + 8).toFloat()).toInt()

                        // Wrap to next line if needed
                        if (statX + entryWidth > x + width - padding && statX != startX) {
                            sy += (lineHeight * 0.9).toInt()
                            statX = startX
                        }

                        drawText(context, abbr, statX.toFloat(), sy.toFloat(), TEXT_LIGHT, scaled(0.75f))
                        statX += scaled((abbr.length * 5 + 2).toFloat()).toInt()
                        drawText(context, arrows, statX.toFloat(), sy.toFloat(), color, scaled(0.75f))
                        statX += scaled((arrows.length * 5 + 8).toFloat()).toInt()
                    }
                    sy += (lineHeight * 0.9).toInt()
                }
            }
            sy
        }

        currentY = y + height - scaled(BASE_PADDING) - scaled(8)
        drawText(context, "V: collapse | Drag header: move | Scroll: resize",
            (x + padding).toFloat(), currentY.toFloat(), TEXT_DIM, scaled(0.55f))
    }

    private fun renderHeader(context: DrawContext, x: Int, y: Int, width: Int) {
        val headerHeight = scaled(BASE_HEADER_HEIGHT)
        val padding = scaled(BASE_PADDING)

        context.fill(x + 1, y + 1, x + width - 1, y + headerHeight, HEADER_BG)
        headerEndY = y + headerHeight
        context.fill(x, y + headerHeight - 1, x + width, y + headerHeight, BORDER_COLOR)

        val arrow = if (isExpanded) "▼" else "▶"
        drawText(context, arrow, (x + padding).toFloat(), (y + scaled(6)).toFloat(), TEXT_GOLD, scaled(0.85f))
        drawText(context, "BATTLE INFO", (x + padding + scaled(12)).toFloat(), (y + scaled(6)).toFloat(),
            TEXT_WHITE, scaled(0.85f))

        val turnText = "T${BattleStateTracker.currentTurn}"
        drawText(context, turnText, (x + width - padding - scaled(turnText.length * 5)).toFloat(),
            (y + scaled(6)).toFloat(), TEXT_GOLD, scaled(0.85f))
    }

    private fun renderSection(
        context: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        title: String,
        accentColor: Int,
        contentRenderer: (Int) -> Int
    ): Int {
        val lineHeight = scaled(BASE_LINE_HEIGHT)
        val padding = scaled(BASE_PADDING)

        context.fill(x + 1, y, x + width - 1, y + lineHeight + 2, SECTION_BG)
        context.fill(x + 1, y, x + 3, y + lineHeight + 2, accentColor)
        drawText(context, title, (x + padding).toFloat(), (y + 2).toFloat(), accentColor, scaled(0.7f))
        return contentRenderer(y + lineHeight + 4)
    }

    private fun drawConditionLine(
        context: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        icon: String,
        name: String,
        info: String,
        accentColor: Int
    ) {
        val textScale = scaled(0.8f)
        drawText(context, icon, x.toFloat(), y.toFloat(), accentColor, textScale)
        drawText(context, name, (x + scaled(14)).toFloat(), y.toFloat(), TEXT_LIGHT, textScale)
        if (info.isNotEmpty()) {
            val infoWidth = scaled(info.length * 5)
            drawText(context, info, (x + width - infoWidth).toFloat(), y.toFloat(), TEXT_DIM, scaled(0.75f))
        }
    }

    private fun drawRoundedRect(context: DrawContext, x: Int, y: Int, w: Int, h: Int, fillColor: Int, borderColor: Int) {
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, fillColor)
        context.fill(x, y + 1, x + 1, y + h - 1, borderColor)
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, borderColor)
        context.fill(x + 1, y, x + w - 1, y + 1, borderColor)
        context.fill(x + 1, y + h - 1, x + w - 1, y + h, borderColor)
    }

    private fun calculateCollapsedHeight(): Int {
        val padding = scaled(BASE_PADDING)
        val lineHeight = scaled(BASE_LINE_HEIGHT)
        val headerHeight = scaled(BASE_HEADER_HEIGHT)

        var height = headerHeight + padding

        if (BattleStateTracker.weather != null) height += lineHeight
        if (BattleStateTracker.terrain != null) height += lineHeight
        height += BattleStateTracker.getFieldConditions().size * lineHeight

        if (BattleStateTracker.getPlayerSideConditions().isNotEmpty()) height += (lineHeight * 0.9).toInt()
        if (BattleStateTracker.getOpponentSideConditions().isNotEmpty()) height += (lineHeight * 0.9).toInt()

        height += padding + scaled(10)

        return height.coerceAtLeast(headerHeight + scaled(40))
    }

    private fun calculateExpandedHeight(pokemonCount: Int): Int {
        val padding = scaled(BASE_PADDING)
        val lineHeight = scaled(BASE_LINE_HEIGHT)
        val headerHeight = scaled(BASE_HEADER_HEIGHT)
        val sectionGap = scaled(BASE_SECTION_GAP)

        var height = headerHeight + sectionGap

        height += lineHeight + 4
        val fieldCount = listOfNotNull(BattleStateTracker.weather, BattleStateTracker.terrain).size +
                         BattleStateTracker.getFieldConditions().size
        height += (if (fieldCount > 0) fieldCount else 1) * lineHeight
        height += sectionGap

        height += lineHeight + 4
        val playerCount = BattleStateTracker.getPlayerSideConditions().size
        height += (if (playerCount > 0) playerCount else 1) * lineHeight
        height += sectionGap

        height += lineHeight + 4
        val opponentCount = BattleStateTracker.getOpponentSideConditions().size
        height += (if (opponentCount > 0) opponentCount else 1) * lineHeight
        height += sectionGap

        height += lineHeight + 4
        height += (pokemonCount * (lineHeight * 0.95)).toInt()
        height += (pokemonCount * 2 * (lineHeight * 0.9)).toInt()

        height += padding + scaled(10)

        return height
    }

    private fun drawText(context: DrawContext, text: String, x: Float, y: Float, color: Int, scale: Float) {
        drawScaledText(
            context = context,
            text = Text.literal(text),
            x = x,
            y = y,
            scale = scale,
            colour = color,
            shadow = true
        )
    }

    private fun getStatAbbr(identifier: String): String {
        return when {
            identifier.contains("attack") && identifier.contains("special") -> "SpA"
            identifier.contains("attack") -> "Atk"
            identifier.contains("defense") && identifier.contains("special") -> "SpD"
            identifier.contains("defence") && identifier.contains("special") -> "SpD"
            identifier.contains("defense") -> "Def"
            identifier.contains("defence") -> "Def"
            identifier.contains("speed") -> "Spe"
            identifier.contains("evasion") -> "Eva"
            identifier.contains("accuracy") -> "Acc"
            else -> identifier.take(3).uppercase()
        }
    }

    private fun getStatSortOrder(identifier: String): Int {
        val id = identifier.lowercase()
        return when {
            id.contains("attack") && id.contains("special") -> 2      // SpA
            id.contains("attack") -> 0                                 // Atk
            (id.contains("defense") || id.contains("defence")) && id.contains("special") -> 3  // SpD
            id.contains("defense") || id.contains("defence") -> 1     // Def
            id.contains("speed") -> 4                                  // Spe
            id.contains("accuracy") -> 5                               // Acc
            id.contains("evasion") -> 6                                // Eva
            else -> 7
        }
    }
}
