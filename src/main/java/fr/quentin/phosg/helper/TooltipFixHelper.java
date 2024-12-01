package fr.quentin.phosg.helper;

import fr.quentin.phosg.converter.OrderedTextConverter;
import fr.quentin.phosg.mixin.OrderedTextTooltipComponentAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Helper class for fixing tooltips to ensure they do not run off the screen.
 */
public class TooltipFixHelper {
    private static boolean shouldFlip = false;

    /**
     * Adjusts the tooltip components to ensure they do not run off the screen.
     *
     * @param components The list of tooltip components.
     * @param textRenderer The text renderer.
     * @param x The x-coordinate of the tooltip.
     * @param width The width of the screen.
     */
    public static void adjustTooltipComponents(List<TooltipComponent> components, TextRenderer textRenderer, int x, int width) {
        shouldFlip = false;

        int forcedWidth = 0;
        for (TooltipComponent component : components) {
            if (!(component instanceof OrderedTextTooltipComponent)) {
                int componentWidth = component.getWidth(textRenderer);
                if (componentWidth > forcedWidth) {
                    forcedWidth = componentWidth;
                }
            }
        }

        int maxWidth = width - 20 - x;
        if (forcedWidth > maxWidth || maxWidth < 100) {
            shouldFlip = true;
            maxWidth = x - 28;
        }

        handleNewLines(components);
        handleLongLines(components, textRenderer, maxWidth);
    }

    /**
     * Adjusts the render X position of the tooltip to ensure it stays within the screen bounds.
     *
     * @param components The list of tooltip components.
     * @param textRenderer The text renderer.
     * @param x The x-coordinate of the tooltip.
     * @return The adjusted render X position.
     */
    public static int adjustRenderX(List<TooltipComponent> components, TextRenderer textRenderer, int x) {
        int maxWidth = 0;
        for (TooltipComponent tooltipComponent : components) {
            int newWidth = tooltipComponent.getWidth(textRenderer);
            if (newWidth > maxWidth) {
                maxWidth = newWidth;
            }
        }
        int renderX = x + 12;

        if (shouldFlip) {
            renderX -= 28 + maxWidth;
        }

        return renderX;
    }

    /**
     * Wraps long lines of text in the tooltip components to fit within the specified max size.
     *
     * @param components The list of tooltip components.
     * @param textRenderer The text renderer.
     * @param maxSize The maximum size for the lines.
     */
    private static void handleLongLines(List<TooltipComponent> components, TextRenderer textRenderer, int maxSize) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i) instanceof OrderedTextTooltipComponent orderedTextTooltipComponent) {
                Text text = OrderedTextConverter.convert(((OrderedTextTooltipComponentAccessor) orderedTextTooltipComponent).getText());
                if (text.getSiblings().isEmpty()) continue;

                List<TooltipComponent> wrapped = textRenderer.wrapLines(text, maxSize).stream().map(TooltipComponent::of).toList();
                components.remove(i);
                components.addAll(i, wrapped);
            }
        }
    }

    /**
     * Handles new lines in the tooltip components by splitting the text at newline characters.
     *
     * @param components The list of tooltip components.
     */
    private static void handleNewLines(List<TooltipComponent> components) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i) instanceof OrderedTextTooltipComponent orderedTextTooltipComponent) {
                Text text = OrderedTextConverter.convert(((OrderedTextTooltipComponentAccessor) orderedTextTooltipComponent).getText());

                List<Text> children = text.getSiblings();
                for (int j = 0; j < children.size() - 1; j++) {
                    String code = children.get(j).getString() + children.get(j + 1).getString();
                    if (code.equals("\\n")) {
                        components.set(i, TooltipComponent.of(createTextWithChildren(children, 0, j).asOrderedText()));
                        components.add(i + 1, TooltipComponent.of(createTextWithChildren(children, j + 2, children.size()).asOrderedText()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates a new Text object with the specified children.
     *
     * @param children The list of child Text objects.
     * @param from The starting index.
     * @param end The ending index.
     * @return The new Text object.
     */
    private static Text createTextWithChildren(List<Text> children, int from, int end) {
        MutableText text = Text.literal("");
        for (int i = from; i < end; i++) {
            text.append(children.get(i));
        }
        return text;
    }
}
