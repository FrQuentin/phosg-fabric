package fr.quentin.phosg.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for fixing potential misalignment of list item highlight border in Minecraft.
 * Fixes MC-267469. <a href="https://bugs.mojang.com/browse/MC-267469">...</a>
 */
@Mixin(EntryListWidget.class)
public abstract class EntryListHighlightFixMixin {

    @Shadow
    public abstract int getRowLeft();

    @Shadow
    public abstract int getRowRight();

    // Default constructor
    public EntryListHighlightFixMixin() {
        super();
    }

    /**
     * Injects code into the drawSelectionHighlight method to fix the highlight border misalignment.
     *
     * @param context The drawing context.
     * @param yPosition The y position of the selection.
     * @param entryWidth The width of the entry.
     * @param entryHeight The height of the entry.
     * @param borderColor The border color.
     * @param fillColor The fill color.
     * @param ci Callback information.
     */
    @Inject(method = "drawSelectionHighlight", at = @At("HEAD"), cancellable = true)
    protected void fixHighlightBorder(DrawContext context, int yPosition, int entryWidth, int entryHeight, int borderColor, int fillColor, CallbackInfo ci) {
        // Use getRowLeft() and getRowRight() to get more appropriate values.
        int leftBorder = getRowLeft() - 2;
        int rightBorder = getRowRight() - 2;

        // Draw the border and fill of the selection.
        context.fill(leftBorder, yPosition - 2, rightBorder, yPosition + entryHeight + 2, borderColor);
        context.fill(leftBorder + 1, yPosition - 1, rightBorder - 1, yPosition + entryHeight + 1, fillColor);

        // Cancel the original method call.
        ci.cancel();
    }
}
