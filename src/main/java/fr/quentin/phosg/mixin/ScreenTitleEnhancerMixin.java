package fr.quentin.phosg.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.quentin.phosg.PhosgClient;
import fr.quentin.phosg.util.ScreenDisplayMode;
import fr.quentin.phosg.util.ScreenConfig;
import fr.quentin.phosg.util.RenderPositionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Mixin class to enhance the rendering of screen titles in Minecraft.
 */
@Mixin(InGameHud.class)
public abstract class ScreenTitleEnhancerMixin {
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow private int titleStayTicks;
    @Shadow private Text title;
    @Shadow private Text subtitle;
    @Shadow private int titleFadeInTicks;
    @Shadow private int titleRemainTicks;
    @Shadow private int titleFadeOutTicks;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Unique
    private Text originalTitle;

    @Unique private int sidebarWidth = -1;
    @Unique private int sidebarOpacityAdjustment = 0;

    @Unique public boolean shouldRenderTitle = false;
    @Unique public boolean hideSidebar = false;
    @Unique public final RenderPositionInfo titleRenderInfo = new RenderPositionInfo();
    @Unique public final RenderPositionInfo subtitleRenderInfo = new RenderPositionInfo();

    /**
     * Pre-render hook to initialize rendering information.
     *
     * @param ci Callback information.
     */
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"))
    private void preRenderHud(CallbackInfo ci) {
        sidebarWidth = -1;
        hideSidebar = false;
        originalTitle = title;
        title = null;
    }

    /**
     * Post-render hook to finalize rendering information.
     *
     * @param context The drawing context.
     * @param tickCounter The render tick counter.
     * @param ci Callback information.
     */
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
    private void postRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        calculateRenderInfo(context);
        executeRenderInfo(context, tickCounter.getTickDelta(false));
        title = originalTitle;
    }

    /**
     * Calculate the rendering information for the title and subtitle.
     *
     * @param context The drawing context.
     */
    @Unique
    private void calculateRenderInfo(DrawContext context) {
        shouldRenderTitle = originalTitle != null && titleStayTicks > 0;
        if (shouldRenderTitle) {
            ScreenConfig config = PhosgClient.getConfig();
            TextRenderer textRenderer = getTextRenderer();

            int titleWidth = textRenderer.getWidth(originalTitle);
            calculateTitleRenderDetails(context, titleRenderInfo, config.preferredTitleScale, titleWidth, config);

            if (subtitle != null) {
                int subtitleWidth = textRenderer.getWidth(subtitle);
                calculateTitleRenderDetails(context, subtitleRenderInfo, config.preferredSubtitleScale, subtitleWidth, config);
            }
        }
    }

    /**
     * Calculate the rendering details for the title or subtitle.
     *
     * @param context The drawing context.
     * @param renderInfo The render position information.
     * @param initialScale The initial scale of the text.
     * @param textWidth The width of the text.
     * @param config The screen configuration.
     */
    @Unique
    private void calculateTitleRenderDetails(DrawContext context, RenderPositionInfo renderInfo,
                                             float initialScale, int textWidth, ScreenConfig config) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        float renderScale = initialScale;
        int renderAreaWidth = screenWidth - config.marginLeft - config.marginRight;
        int renderAreaWidthWithSidebar = renderAreaWidth - sidebarWidth;

        if (config.screenDisplayMode == ScreenDisplayMode.FADE) {
            renderAreaWidthWithSidebar -= sidebarWidth;
        }

        boolean exceedsBoundary = false;
        int renderTextWidth = (int)(renderScale * textWidth);

        if (renderTextWidth > renderAreaWidthWithSidebar) {
            if (config.screenDisplayMode == ScreenDisplayMode.FADE) {
                hideSidebar = true;
                if (config.contentAlwaysFitsScreen && renderTextWidth > renderAreaWidth) {
                    renderScale = (float)renderAreaWidth / textWidth;
                    exceedsBoundary = true;
                }
            } else {
                if (config.contentAlwaysFitsScreen) {
                    renderScale = (config.screenDisplayMode == ScreenDisplayMode.MOVE)
                            ? (float)renderAreaWidthWithSidebar / textWidth
                            : (float)renderAreaWidth / textWidth;
                    exceedsBoundary = true;
                }
            }
        }

        float titlePosX = (float)screenWidth / 2;
        float titlePosY = (float)screenHeight / 2;

        if (config.screenDisplayMode == ScreenDisplayMode.MOVE) {
            titlePosX -= (float)sidebarWidth / 2;
        }

        if (exceedsBoundary) {
            titlePosX += (float)(config.marginLeft - config.marginRight) / 2;
        }

        renderInfo.x = titlePosX;
        renderInfo.y = titlePosY;
        renderInfo.scale = renderScale;
    }

    /**
     * Execute the rendering based on the calculated information.
     *
     * @param context The drawing context.
     * @param tickDelta The tick delta.
     */
    @Unique
    private void executeRenderInfo(DrawContext context, float tickDelta) {
        if (shouldRenderTitle) {
            Profiler profiler = client.getProfiler();
            TextRenderer textRenderer = getTextRenderer();

            profiler.push("titleAndSubtitle");

            int alpha = getAlpha(tickDelta);

            if (alpha > 8) {
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(titleRenderInfo.x, titleRenderInfo.y, 0.0F);
                RenderSystem.enableBlend();

                matrices.push();
                matrices.scale(titleRenderInfo.scale, titleRenderInfo.scale, 1.0F);

                int titleColor = ColorHelper.Argb.withAlpha(alpha, -1);
                int titleWidth = textRenderer.getWidth(originalTitle);
                context.drawTextWithBackground(textRenderer, originalTitle, -titleWidth / 2, -10, titleWidth, titleColor);

                matrices.pop();

                if (subtitle != null) {
                    matrices.push();
                    matrices.scale(subtitleRenderInfo.scale, subtitleRenderInfo.scale, 1.0F);
                    int subtitleWidth = textRenderer.getWidth(subtitle);
                    context.drawTextWithBackground(textRenderer, subtitle, -subtitleWidth / 2, 5, subtitleWidth, titleColor);
                    matrices.pop();
                }

                RenderSystem.disableBlend();
                matrices.pop();
            }

            profiler.pop();
        }
    }

    private int getAlpha(float tickDelta) {
        float ticksLeft = (float)titleStayTicks - tickDelta;
        int alpha = 255;

        if (titleStayTicks > titleFadeOutTicks + titleRemainTicks) {
            float r = (float)(titleFadeInTicks + titleRemainTicks + titleFadeOutTicks) - ticksLeft;
            alpha = (int)(r * 255.0F / titleFadeInTicks);
        }

        if (titleStayTicks <= titleFadeOutTicks) {
            alpha = (int)(ticksLeft * 255.0F / titleFadeOutTicks);
        }

        alpha = MathHelper.clamp(alpha, 0, 255);
        return alpha;
    }

    /**
     * Tick hook to adjust sidebar opacity.
     *
     * @param ci Callback information.
     */
    @Inject(method = "tick()V", at = @At("HEAD"))
    void onTick(CallbackInfo ci) {
        ScreenConfig config = PhosgClient.getConfig();
        if (hideSidebar) {
            if (sidebarOpacityAdjustment > -255) {
                sidebarOpacityAdjustment -= config.elementFadeSpeed;
                sidebarOpacityAdjustment = Math.max(sidebarOpacityAdjustment, -255);
            }
        } else {
            if (sidebarOpacityAdjustment < 0) {
                sidebarOpacityAdjustment += config.elementFadeSpeed;
                sidebarOpacityAdjustment = Math.min(sidebarOpacityAdjustment, 0);
            }
        }
    }

    /**
     * Cancel scoreboard rendering if opacity is too low.
     *
     * @param context The drawing context.
     * @param objective The scoreboard objective.
     * @param ci Callback information.
     */
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void cancelScoreboardRender(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (getAdjustedScoreboardColor(-1) >>> 24 <= 8) {
            ci.cancel();
        }
    }

    /**
     * Modify the sidebar background color.
     *
     * @param args The method arguments.
     */
    @ModifyArgs(
            method = "method_55440",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
            )
    )
    private void modifySidebarBackground(Args args) {
        if (sidebarWidth == -1) {
            int x1 = args.get(0);
            int x2 = args.get(2);
            sidebarWidth = x2 - x1;
        }
        args.set(4, getAdjustedScoreboardColor(args.get(4)));
    }

    /**
     * Modify the scoreboard text color.
     *
     * @param color The original color.
     * @return The adjusted color.
     */
    @ModifyArg(
            method = "method_55440",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"
            ), index = 4
    )
    private int modifyScoreboardTextColor(int color) {
        return getAdjustedScoreboardColor(color);
    }

    /**
     * Get the adjusted scoreboard color based on opacity adjustment.
     *
     * @param color The original color.
     * @return The adjusted color.
     */
    @Unique
    private int getAdjustedScoreboardColor(int color) {
        ScreenConfig config = PhosgClient.getConfig();
        int alpha = color >>> 24;
        alpha += sidebarOpacityAdjustment;

        alpha = Math.max(config.minimumElementAlpha, Math.min(alpha, 255));

        color &= ~0xFF000000;
        color |= alpha << 24;
        return color;
    }
}