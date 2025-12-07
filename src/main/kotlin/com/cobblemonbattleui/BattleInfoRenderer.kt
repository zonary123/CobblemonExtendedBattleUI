package com.cobblemonbattleui

import net.minecraft.client.gui.DrawContext

/**
 * Renders additional battle information on the battle overlay.
 */
object BattleInfoRenderer {

    fun render(context: DrawContext) {
        TeamIndicatorUI.render(context)
        BattleInfoPanel.render(context)
    }
}
