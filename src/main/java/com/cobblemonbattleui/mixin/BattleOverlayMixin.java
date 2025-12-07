package com.cobblemonbattleui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.gui.battle.BattleOverlay;
import com.cobblemonbattleui.BattleInfoRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.DrawContext;

/**
 * Mixin to extend Cobblemon's BattleOverlay with additional information.
 */
@Mixin(value = BattleOverlay.class, remap = false)
public abstract class BattleOverlayMixin {

    /**
     * Inject at the end of the render method to add our custom UI elements
     * AFTER the default battle overlay has rendered.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Render our additional battle information
        BattleInfoRenderer.INSTANCE.render(context);
    }
}
