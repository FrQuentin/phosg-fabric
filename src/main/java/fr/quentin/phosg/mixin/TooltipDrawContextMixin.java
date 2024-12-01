package fr.quentin.phosg.mixin;

import fr.quentin.phosg.helper.TooltipFixHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin for the DrawContext class to adjust tooltip rendering.
 */
@Mixin(DrawContext.class)
public abstract class TooltipDrawContextMixin {
    @Shadow
    public abstract int getScaledWindowWidth();

    /**
     * Makes the list of tooltip components mutable to allow modifications.
     *
     * @param value The original list of tooltip components.
     * @return A mutable copy of the original list.
     */
    @ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At(value = "HEAD"), index = 2, argsOnly = true)
    public List<TooltipComponent> makeListMutable(List<TooltipComponent> value) {
        return new ArrayList<>(value);
    }

    /**
     * Adjusts the tooltip components to ensure they do not run off the screen.
     *
     * @param textRenderer The text renderer.
     * @param components The list of tooltip components.
     * @param x The x-coordinate of the tooltip.
     * @param y The y-coordinate of the tooltip.
     * @param positioner The tooltip positioner.
     * @param ci The callback info.
     */
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At(value = "HEAD"))
    public void adjustTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, CallbackInfo ci) {
        TooltipFixHelper.adjustTooltipComponents(components, textRenderer, x, getScaledWindowWidth());
    }

    /**
     * Adjusts the render X position of the tooltip to ensure it stays within the screen bounds.
     *
     * @param value The original render X position.
     * @param textRenderer The text renderer.
     * @param components The list of tooltip components.
     * @param x The x-coordinate of the tooltip.
     * @return The adjusted render X position.
     */
    @ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"), index = 11)
    public int adjustRenderX(int value, TextRenderer textRenderer, List<TooltipComponent> components, int x) {
        return TooltipFixHelper.adjustRenderX(components, textRenderer, x);
    }
}
