package com.cobblemonbattleui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket;
import com.cobblemonbattleui.BattleMessageInterceptor;
import net.minecraft.client.MinecraftClient;

/**
 * Mixin to intercept battle messages and extract stat change information.
 */
@Mixin(value = BattleMessageHandler.class, remap = false)
public class BattleMessageHandlerMixin {

    /**
     * Inject at the start of handle() to process messages before they're displayed.
     */
    @Inject(method = "handle", at = @At("HEAD"))
    private void onHandle(BattleMessagePacket packet, MinecraftClient client, CallbackInfo ci) {
        // Process each message for stat changes
        BattleMessageInterceptor.INSTANCE.processMessages(packet.getMessages());
    }
}
