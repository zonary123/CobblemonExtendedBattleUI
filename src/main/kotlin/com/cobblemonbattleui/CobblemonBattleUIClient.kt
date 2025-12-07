package com.cobblemonbattleui

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object CobblemonBattleUIClient : ClientModInitializer {

    // Keybind for toggling the battle info panel
    private lateinit var togglePanelKey: KeyBinding

    override fun onInitializeClient() {
        CobblemonBattleUI.LOGGER.info("Cobblemon Battle UI Client initializing...")

        // Initialize panel (loads config)
        BattleInfoPanel.initialize()

        // Register keybindings
        registerKeybindings()

        // Register tick handler for keybind checking
        registerTickHandler()

        CobblemonBattleUI.LOGGER.info("Cobblemon Battle UI Client initialized!")
    }

    private fun registerKeybindings() {
        // TAB key to toggle battle info panel
        togglePanelKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.cobblemonbattleui.toggle_panel",  // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,  // Default to TAB
                "category.cobblemonbattleui"  // Category translation key
            )
        )

        CobblemonBattleUI.LOGGER.info("Registered keybindings")
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            while (togglePanelKey.wasPressed()) {
                BattleInfoPanel.toggle()
            }
        }
    }
}
