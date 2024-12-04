package fr.quentin.phosg.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

/**
 * Configuration class for screen utilities.
 */
@Config(name = "screen-util")
public class ScreenConfig implements ConfigData {
    public boolean contentAlwaysFitsScreen = true;
    public ScreenDisplayMode screenDisplayMode = ScreenDisplayMode.FADE;
    public int elementFadeSpeed = 13;
    public int minimumElementAlpha = 0;
    public int marginLeft = 8;
    public int marginRight = 8;
    public float preferredTitleScale = 4.0F;
    public float preferredSubtitleScale = 2.0F;
}
