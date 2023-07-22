package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.latesthits.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Side.CLIENT)
public class RenderEvents {
    private static final ResourceLocation INDICATOR_RED = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_red.png");
    private static final ResourceLocation INDICATOR_BLUE = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_blue.png");
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


    private static final int indicatorWidth = 42;
    private static final int indicatorHeight = 13;
    private static final int markerWidth = 20;
    private static final int markerHeight = 20;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        TextureManager textureManager = mc.getTextureManager();

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenMiddleX = scaledResolution.getScaledWidth() / 2;
        int screenMiddleY = scaledResolution.getScaledHeight() / 2;

        Vector2d lookVec = getLookVec(mc.player);
        Vector2d playerPos = new Vector2d(mc.player.posX, mc.player.posZ);
        for(HitIndicator hitIndicator : ClientLatestHits.latestHitIndicators)
            drawHitIndicator(hitIndicator, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);

        if(ClientLatestHits.currentHitMarker != null)
            drawHitMarker(ClientLatestHits.currentHitMarker, textureManager, screenMiddleX, screenMiddleY);
    }

    private static void drawHitMarker(HitMarker hitMarker, TextureManager textureManager, int screenMiddleX, int screenMiddleY) {
        float opacity = hitMarker.getType() == HitMarkerType.CRIT ? 30 : 60;
        opacity /= 100.0f;

        bindMarkerTexture(textureManager, hitMarker.getType(), hitMarker.getLifeTime());

        float defaultScale = 1;
        int scaledTextureWidth = (int)Math.floor(markerWidth * defaultScale);
        int scaledTextureHeight = (int)Math.floor(markerHeight * defaultScale);
        GL11.glColor4f(1, 1, 1, opacity);
        Gui.drawModalRectWithCustomSizedTexture(screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void drawHitIndicator(HitIndicator hitIndicator, TextureManager textureManager, int screenMiddleX, int screenMiddleY, Vector2d playerPos, Vector2d lookVec) {
        Vector3d sourceVec3d = hitIndicator.getLocation();
        Vector2d diff = new Vector2d(sourceVec3d.x - playerPos.x, sourceVec3d.z - playerPos.y);
        double angleBetween = angleBetween(lookVec, diff);
        float opacity = hitIndicator.getLifeTime() >= 25
                ? HitIndicatorConfig.IndicatorOpacity
                : HitIndicatorConfig.IndicatorOpacity * hitIndicator.getLifeTime() / 25.0f;
        opacity /= 100.0f;

        float defaultScale = 1 + HitIndicatorConfig.IndicatorDefaultScale / 100.0f;
        int scaledTextureWidth = (int)Math.floor(indicatorWidth * defaultScale);
        int scaledTextureHeight = (int)Math.floor(indicatorHeight * defaultScale);
        if(HitIndicatorConfig.SizeDependsOnDamage) {
            float scale = MathHelper.clamp(hitIndicator.getDamagePercent() > 30 ? 1 + hitIndicator.getDamagePercent() / 125.0f : 1, 0, 3);
            scaledTextureWidth = (int)Math.floor(scaledTextureWidth * scale);
            scaledTextureHeight = (int)Math.floor(scaledTextureHeight* scale);
        }

        float distanceFromPlayer = calculateDistanceFromPlayer(hitIndicator.getLocation());
        float distanceScaling = 1.0f - (distanceFromPlayer <= 10f ? 0f : (distanceFromPlayer - 10.0f) / 10.0f);
        if(distanceScaling > 1) distanceScaling = 1;
        if(distanceScaling < 0) distanceScaling = 0;
        scaledTextureWidth = (int)Math.floor(scaledTextureWidth * distanceScaling);
        scaledTextureHeight = (int)Math.floor(scaledTextureHeight * distanceScaling);

        bindIndicatorTexture(textureManager, hitIndicator.getType());

        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, opacity);
        GL11.glTranslatef(screenMiddleX, screenMiddleY, 0);
        GL11.glRotatef((float)angleBetween, 0, 0, 1);
        GL11.glTranslatef(-screenMiddleX, -screenMiddleY, 0);
        Gui.drawModalRectWithCustomSizedTexture(screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 - 30 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private static void bindIndicatorTexture(TextureManager textureManager, HitIndicatorType type) {
        switch(type) {
            case BLUE: textureManager.bindTexture(INDICATOR_BLUE);  break;
            default: textureManager.bindTexture(INDICATOR_RED);  break;
        }
    }

    private static void bindMarkerTexture(TextureManager textureManager, HitMarkerType type, int lifetime) {
        switch(type) {
            case KILL:
                if(lifetime > 6) {
                    textureManager.bindTexture(MARKER_KILL[9 - lifetime]);
                    return;
                }
                textureManager.bindTexture(MARKER_KILL[3]);
                break;
            default:
                if(lifetime > 6) {
                    textureManager.bindTexture(MARKER_CRIT[9 - lifetime]);
                    return;
                }
                textureManager.bindTexture(MARKER_CRIT[3]);
                break;
        }
    }

    private static double angleBetween(Vector2d first, Vector2d second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
    }

    private static Vector2d getLookVec(EntityPlayerSP player) {
        return new Vector2d(-Math.sin(-player.rotationYaw * Math.PI / 180.0 - Math.PI), -Math.cos(-player.rotationYaw * Math.PI / 180.0 - Math.PI));
    }

    private static float calculateDistanceFromPlayer(Vector3d damageLocation) {
        if(Minecraft.getMinecraft().player == null)
            return 0;

        Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();
        double d0 = damageLocation.x - playerPos.x;
        double d1 = damageLocation.y - playerPos.y;
        double d2 = damageLocation.z - playerPos.z;
        return (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
}
