package com.cobblemonbattleui

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

/**
 * Persistent configuration for the battle info panel.
 * Stores position, scale, and other user preferences.
 */
object PanelConfig {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File by lazy {
        FabricLoader.getInstance().configDir.resolve("cobblemonbattleui.json").toFile()
    }

    // Panel position (null = default right-center position)
    var panelX: Int? = null
        private set
    var panelY: Int? = null
        private set

    // Panel scale (1.0 = default)
    var scale: Float = 1.0f
        private set

    // Whether panel starts expanded
    var startExpanded: Boolean = false
        private set

    // Minimum and maximum scale
    const val MIN_SCALE = 0.3f
    const val MAX_SCALE = 2.0f
    const val SCALE_STEP = 0.05f

    data class ConfigData(
        val panelX: Int? = null,
        val panelY: Int? = null,
        val scale: Float = 1.0f,
        val startExpanded: Boolean = false
    )

    fun load() {
        try {
            if (configFile.exists()) {
                val data = gson.fromJson(configFile.readText(), ConfigData::class.java)
                panelX = data.panelX
                panelY = data.panelY
                scale = data.scale.coerceIn(MIN_SCALE, MAX_SCALE)
                startExpanded = data.startExpanded
                CobblemonBattleUI.LOGGER.info("PanelConfig: Loaded config - pos=(${panelX}, ${panelY}), scale=$scale")
            }
        } catch (e: Exception) {
            CobblemonBattleUI.LOGGER.warn("PanelConfig: Failed to load config, using defaults: ${e.message}")
        }
    }

    fun save() {
        try {
            val data = ConfigData(panelX, panelY, scale, startExpanded)
            configFile.parentFile?.mkdirs()
            configFile.writeText(gson.toJson(data))
            CobblemonBattleUI.LOGGER.debug("PanelConfig: Saved config")
        } catch (e: Exception) {
            CobblemonBattleUI.LOGGER.warn("PanelConfig: Failed to save config: ${e.message}")
        }
    }

    fun setPosition(x: Int?, y: Int?) {
        panelX = x
        panelY = y
    }

    fun setScale(newScale: Float) {
        scale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
    }

    fun adjustScale(delta: Float) {
        scale = (scale + delta).coerceIn(MIN_SCALE, MAX_SCALE)
    }

    fun setStartExpanded(expanded: Boolean) {
        startExpanded = expanded
    }

    fun resetToDefaults() {
        panelX = null
        panelY = null
        scale = 1.0f
        startExpanded = false
        save()
    }
}
