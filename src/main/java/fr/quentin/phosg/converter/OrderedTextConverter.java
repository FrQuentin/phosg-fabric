package fr.quentin.phosg.converter;

import net.minecraft.text.*;

/**
 * Converts OrderedText to Text by visiting each character and applying the style.
 */
public class OrderedTextConverter implements CharacterVisitor {
    private final MutableText text = Text.empty();

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        String charStr = new String(Character.toChars(codePoint));
        text.append(Text.literal(charStr).setStyle(style));
        return true;
    }

    /**
     * Gets the converted Text.
     *
     * @return The converted Text.
     */
    public Text getText() {
        return text;
    }

    /**
     * Converts an OrderedText to a Text.
     *
     * @param orderedText The OrderedText to convert.
     * @return The converted Text.
     */
    public static Text convert(OrderedText orderedText) {
        OrderedTextConverter visitor = new OrderedTextConverter();
        orderedText.accept(visitor);
        return visitor.getText();
    }
}
