package com.cobblemonbattleui.mixin;

import com.cobblemonbattleui.BattleInfoPanel;
import com.cobblemon.mod.common.client.CobblemonClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept mouse scroll events for panel scaling.
 */
@Mixin(Mouse.class)
public abstract class MouseScrollMixin {

    @Shadow
    private double x;

    @Shadow
    private double y;

    @Inject(
        method = "onMouseScroll",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Only intercept during battle
        if (CobblemonClient.INSTANCE.getBattle() != null) {
            // Let the panel try to handle the scroll
            if (BattleInfoPanel.INSTANCE.onScroll(this.x, this.y, vertical)) {
                ci.cancel();  // Consume the event if panel handled it
            }
        }
    }
}
