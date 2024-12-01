package fr.quentin.phosg.mixin;

import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor interface for the OrderedTextTooltipComponent class to access the text field.
 */
@Mixin(OrderedTextTooltipComponent.class)
public interface OrderedTextTooltipComponentAccessor {

    /**
     * Gets the ordered text from the OrderedTextTooltipComponent.
     *
     * @return The ordered text.
     */
    @Accessor("text")
    OrderedText getText();
}
