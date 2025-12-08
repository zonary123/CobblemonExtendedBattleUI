package com.cobblemonextendedbattleui

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object CobblemonExtendedBattleUIClient : ClientModInitializer {

    // Keybind for toggling the battle info panel
    private lateinit var togglePanelKey: KeyBinding

    override fun onInitializeClient() {
        CobblemonExtendedBattleUI.LOGGER.info("Cobblemon Extended Battle UI Client initializing...")

        // Initialize panel (loads config)
        BattleInfoPanel.initialize()

        // Register keybindings
        registerKeybindings()

        // Register tick handler for keybind checking
        registerTickHandler()

        // Register HUD render callback for our battle UI overlay
        registerHudRenderer()

        CobblemonExtendedBattleUI.LOGGER.info("Cobblemon Extended Battle UI Client initialized!")
    }

    private fun registerHudRenderer() {
        HudRenderCallback.EVENT.register { context, _ ->
            // Render our additional battle information after all other HUD elements
            BattleInfoRenderer.render(context)
        }
    }

    private fun registerKeybindings() {
        // TAB key to toggle battle info panel
        togglePanelKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.cobblemonextendedbattleui.toggle_panel",  // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,  // Default to TAB
                "category.cobblemonextendedbattleui"  // Category translation key
            )
        )

        CobblemonExtendedBattleUI.LOGGER.info("Registered keybindings")
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            while (togglePanelKey.wasPressed()) {
                BattleInfoPanel.toggle()
            }
        }
    }
}
