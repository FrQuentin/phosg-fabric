package fr.quentin.phosg.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class to clear the title when the player disconnects from the game.
 */
@Mixin(MinecraftClient.class)
public class ClearTitleOnDisconnectMixin {
    @Shadow
    @Final
    public InGameHud inGameHud;

    /**
     * Injects code into the disconnect method to clear the title when the player disconnects.
     *
     * @param screen The screen to display after disconnecting.
     * @param ci Callback information.
     */
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void clearTitleMixin(Screen screen, CallbackInfo ci) {
        if (inGameHud != null) {
            inGameHud.clearTitle();
        }
    }
}
