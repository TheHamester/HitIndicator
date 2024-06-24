package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.latesthits.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT)
public class RenderEvents {
    private static final ResourceLocation INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator.png");
    private static final ResourceLocation EDGE_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/edge_indicator.png");
    private static final ResourceLocation INDICATOR_BLOCK = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_block.png");
    private static final ResourceLocation ND_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/nd_person_damage.png");
    private static final ResourceLocation[] MARKER_CRIT = {
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_crit1.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_crit2.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_crit3.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_crit4.png")
    };
    private static final ResourceLocation[] MARKER_KILL = {
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_kill1.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_kill2.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_kill3.png"),
            new ResourceLocation(HitIndication.MODID, "textures/hit/marker_kill4.png")
    };
    private static final int ND_INDICATOR_WIDTH = 66;
    private static final int INDICATOR_WIDTH = 42;
    private static final int INDICATOR_HEIGHT = 13;
    private static final int MARKER_WIDTH = 20;
    private static final int MARKER_HEIGHT = 20;
    private static final int EDGE_INDICATOR_WIDTH = 16;
    private static final int EDGE_INDICATOR_HEIGHT = 16;

    private static String lastHitColorString = "FF0000";
    private static String lastBlockColorString = "0000FF";
    private static float hitColorR = 1.0F, hitColorG = 0.0F, hitColorB = 0.0F;
    private static float blockColorR = 0.0F, blockColorG = 0.0F, blockColorB = 1.0F;

    private static ResourceLocation itemNameOverlay = new ResourceLocation("item_name");

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        if(event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        int screenMiddleX = event.getWindow().getGuiScaledWidth() / 2;
        int screenMiddleY = event.getWindow().getGuiScaledHeight() / 2;

        updateColorsIfNeeded();

        Vec3 viewVector = calculateViewVector(0, mc.player.getYRot());
        Vec2 lookVec = new Vec2((float)viewVector.x, (float)viewVector.z);
        Vec2 playerPos = new Vec2((float) mc.player.getX(), (float) mc.player.getZ());
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicatorEdge(event.getGuiGraphics(), hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        } else {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicator(event.getGuiGraphics(), hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }

        if (ClientLatestHits.currentHitMarker != null)
            drawHitMarker(event.getGuiGraphics(), ClientLatestHits.currentHitMarker, screenMiddleX, screenMiddleY);
    }

    private static void updateColorsIfNeeded() {
        String currentHitColor = HitIndicatorClientConfigs.HitIndicatorColor.get();
        String currentBlockColor = HitIndicatorClientConfigs.BlockIndicatorColor.get();
        if (!lastHitColorString.equals(currentHitColor)) {
            try {
                int parsedValue = Integer.parseInt(currentHitColor, 16);
                hitColorR = (parsedValue >> 16 & 0xFF) / 255.0F;
                hitColorG = ((parsedValue >> 8) & 0xFF) / 255.0F;
                hitColorB = (parsedValue & 0xFF) / 255.0F;
            } catch (Exception e) {
                hitColorR = 1.0F;
                hitColorG = 0.0F;
                hitColorB = 0.0F;
            }
            lastHitColorString = currentHitColor;
        }

        if (!lastBlockColorString.equals(currentBlockColor)) {
            try {
                int parsedValue = Integer.parseInt(currentBlockColor, 16);
                blockColorR = (parsedValue >> 16 & 0xFF) / 255.0F;
                blockColorG = ((parsedValue >> 8) & 0xFF) / 255.0F;
                blockColorB = (parsedValue & 0xFF) / 255.0F;
            } catch (Exception e) {
                blockColorR = 0.0F;
                blockColorG = 0.0F;
                blockColorB = 1.0F;
            }
            lastBlockColorString = currentBlockColor;
        }
    }

    private static void drawHitMarker(GuiGraphics gui, HitMarker hitMarker, int screenMiddleX, int screenMiddleY) {
        float opacity = hitMarker.getType() == HitMarkerType.CRIT ? 30 : 60;
        opacity /= 100.0f;

        ResourceLocation atlasLocation = getMarkerTexture(hitMarker.getType(), hitMarker.getLifeTime());

        float defaultScale = 1;
        int scaledTextureWidth = (int)Math.floor(MARKER_WIDTH * defaultScale);
        int scaledTextureHeight = (int)Math.floor(MARKER_HEIGHT * defaultScale);
        RenderSystem.setShaderColor(1, 1, 1, opacity);
        gui.blit(atlasLocation, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawIndicator(GuiGraphics gui, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vec2 playerPos, Vec2 lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vec2 diff = new Vec2((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);
        int distanceFromCrosshair = HitIndicatorClientConfigs.DistanceFromCrosshair.get();

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = hit.getType() != HitIndicatorType.ND_HIT ? (int)Math.floor(INDICATOR_WIDTH * defaultScale) : (int)Math.floor(ND_INDICATOR_WIDTH * 1.25);
        int scaledTextureHeight = hit.getType() != HitIndicatorType.ND_HIT ? (int)Math.floor(INDICATOR_HEIGHT * defaultScale) : (int)Math.floor(ND_INDICATOR_WIDTH * 1.25);

        if(hit.getType() != HitIndicatorType.ND_HIT) {
            if (HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
                float scale = Mth.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
                scaledTextureWidth = (int) Math.floor(scaledTextureWidth * scale);
                scaledTextureHeight = (int) Math.floor(scaledTextureHeight * scale);
            }

            if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
                float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());
                float distanceScalingCutoff = HitIndicatorClientConfigs.DistanceScalingCutoff.get();
                float distanceScaling = 1.0f - (distanceFromPlayer <= distanceScalingCutoff ? 0f : (distanceFromPlayer - distanceScalingCutoff) / 10.0f);
                if (distanceScaling > 1) distanceScaling = 1;
                if (distanceScaling < 0) distanceScaling = 0;
                scaledTextureWidth = (int) Math.floor(scaledTextureWidth * distanceScaling);
                scaledTextureHeight = (int) Math.floor(scaledTextureHeight * distanceScaling);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        ResourceLocation atlasLocation = getTextureAndSetColor(hit);

        gui.pose().pushPose();
        gui.pose().translate(screenMiddleX, screenMiddleY, 0);
        if(hit.getType() != HitIndicatorType.ND_HIT)
            gui.pose().mulPose(Axis.ZP.rotationDegrees((float)angleBetween));
        gui.pose().translate(-screenMiddleX, -screenMiddleY, 0);
        gui.blit(atlasLocation, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 - (hit.getType() == HitIndicatorType.ND_HIT ? 0 : distanceFromCrosshair), 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        gui.pose().popPose();

        RenderSystem.disableBlend();
    }

    private static void drawIndicatorEdge(GuiGraphics gui, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vec2 playerPos, Vec2 lookVec) {
        if (hit.getType() == HitIndicatorType.ND_HIT && HitIndicatorClientConfigs.EdgeOfScreenMode.get())
            return;

        Vector3d sourceVec3d = hit.getLocation();
        Vec2 diff = new Vec2((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = (int)Math.floor(EDGE_INDICATOR_WIDTH * defaultScale);
        int scaledTextureHeight = (int)Math.floor(EDGE_INDICATOR_HEIGHT * defaultScale);

        if (HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
            float scale = Mth.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
            scaledTextureWidth = (int) Math.floor(scaledTextureWidth * scale);
            scaledTextureHeight = (int) Math.floor(scaledTextureHeight * scale);
        }

        if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
            float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());
            float distanceScalingCutoff = HitIndicatorClientConfigs.DistanceScalingCutoff.get();
            float distanceScaling = 1.0f - (distanceFromPlayer <= distanceScalingCutoff ? 0f : (distanceFromPlayer - distanceScalingCutoff) / 10.0f);
            if (distanceScaling > 1) distanceScaling = 1;
            if (distanceScaling < 0) distanceScaling = 0;
            scaledTextureWidth = (int) Math.floor(scaledTextureWidth * distanceScaling);
            scaledTextureHeight = (int) Math.floor(scaledTextureHeight * distanceScaling);
        }

        int blitX, blitY;
        double targetAngle = -angleBetween;
        if (targetAngle >= 45.0F && targetAngle <= 135.0F) {
            blitX = 0;
            blitY = (int)Mth.lerp((targetAngle - 45.0F) / (90.0F), 0, 2 * screenMiddleY - scaledTextureHeight);
        } else if (targetAngle <= -45.0F && targetAngle >= -135.0F) {
            blitX = 2 * screenMiddleX - scaledTextureWidth;
            blitY = (int)Mth.lerp((targetAngle + 45.0F) / (-90.0F), 0, 2 * screenMiddleY - scaledTextureHeight);
        } else if (targetAngle >= 0.0F && targetAngle <= 45.0) {
            blitX = (int)Mth.lerp(targetAngle / 45.0F, screenMiddleX, 0);
            blitY = 0;
        } else if (targetAngle >= -45.0 && targetAngle <= 0.0F) {
            blitX = (int)Mth.lerp(targetAngle / (-45.0F), screenMiddleX, 2 * screenMiddleX - scaledTextureWidth);
            blitY = 0;
        } else if (targetAngle <= 180F && targetAngle >= 135.0F) {
            blitX = (int)Mth.lerp((targetAngle - 135.0F) / (45.0F), 0, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight;
        } else {
            blitX = (int)Mth.lerp((targetAngle + 135.0F) / (-45.0F), 2 * screenMiddleX - scaledTextureWidth, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        ResourceLocation atlasLocation = getTextureAndSetColor(hit);

        gui.pose().pushPose();

        gui.pose().translate(blitX + scaledTextureWidth / 2.0F, blitY + scaledTextureHeight / 2.0F, 0);
        gui.pose().mulPose(Axis.ZP.rotationDegrees((float)angleBetween));
        gui.pose().translate(-blitX - scaledTextureWidth / 2.0F, -blitY - scaledTextureHeight / 2.0F, 0);
        gui.blit(atlasLocation, blitX, blitY, 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        gui.pose().popPose();

        RenderSystem.disableBlend();
    }

    private static ResourceLocation getTextureAndSetColor(HitIndicator hit) {
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0f;
        opacity /= 100.0f;

        if (hit.getType() == HitIndicatorType.ND_HIT || hit.getType() == HitIndicatorType.HIT)
            RenderSystem.setShaderColor(hitColorR, hitColorG, hitColorB, opacity);
        else
            RenderSystem.setShaderColor(blockColorR, blockColorG, blockColorB, opacity);

        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) return EDGE_INDICATOR;
        else if (hit.getType() == HitIndicatorType.ND_HIT) return ND_INDICATOR;
        else if (hit.getType() == HitIndicatorType.HIT) return INDICATOR;
        else return INDICATOR_BLOCK;
    }

    private static ResourceLocation getMarkerTexture(HitMarkerType type, int lifetime) {
        switch(type) {
            case KILL:
                if(lifetime > 6) {
                    return MARKER_KILL[9 - lifetime];
                }
                return MARKER_KILL[3];
            default:
                if(lifetime > 6) {
                    return MARKER_CRIT[9 - lifetime];
                }
                return MARKER_CRIT[3];
        }
    }

    private static double angleBetween(Vec2 first, Vec2 second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
    }

    private static float calculateDistanceFromPlayer(Vector3d damageLocation) {
        if(Minecraft.getInstance().player == null)
            return 0;

        Vec3 playerPos = Minecraft.getInstance().player.getPosition(0);
        double d0 = damageLocation.x - playerPos.x;
        double d1 = damageLocation.y - playerPos.y;
        double d2 = damageLocation.z - playerPos.z;
        return (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    private static Vec3 calculateViewVector(float pPitch, float pYaw) {
        float f = pPitch * ((float) Math.PI / 180F);
        float f1 = -pYaw * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }
}
