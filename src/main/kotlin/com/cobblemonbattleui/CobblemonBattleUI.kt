package com.cobblemonbattleui

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object CobblemonBattleUI : ModInitializer {
    const val MOD_ID = "cobblemonbattleui"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Cobblemon Battle UI initialized")
    }
}
