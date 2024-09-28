package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.latesthits.*;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
    private static final float ND_INDICATOR_DEFAULT_SCALE = 1.25F;
    private static final int INDICATOR_WIDTH = 42;
    private static final int INDICATOR_HEIGHT = 13;
    private static final int MARKER_WIDTH = 20;
    private static final int MARKER_HEIGHT = 20;
    private static final int EDGE_INDICATOR_WIDTH = 16;
    private static final int EDGE_INDICATOR_HEIGHT = 16;
    private static final int HUD_HEIGHT = 50;

    private static String lastHitColorString = "FF0000";
    private static String lastBlockColorString = "0000FF";
    private static float hitColorR = 1.0F, hitColorG = 0.0F, hitColorB = 0.0F;
    private static float blockColorR = 0.0F, blockColorG = 0.0F, blockColorB = 1.0F;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null)
            return;

        int screenMiddleX = event.getWindow().getGuiScaledWidth() / 2;
        int screenMiddleY = event.getWindow().getGuiScaledHeight() / 2;

        updateColorsIfNeeded();

        Vec3 viewVector = calculateViewVector(0, mc.player.getYRot());
        Vec2 lookVec = new Vec2((float)viewVector.x, (float)viewVector.z);
        Vec2 playerPos = new Vec2((float)mc.player.getX(), (float)mc.player.getZ());
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicatorEdge(event.getMatrixStack(), hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        } else {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicator(event.getMatrixStack(), hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }

        if (ClientLatestHits.currentHitMarker != null)
            drawHitMarker(event.getMatrixStack(), ClientLatestHits.currentHitMarker, screenMiddleX, screenMiddleY);
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

    private static void drawHitMarker(PoseStack stack, HitMarker hitMarker, int screenMiddleX, int screenMiddleY) {
        float opacity = hitMarker.getType() == HitMarkerType.CRIT ? 0.3F : 0.6F;

        bindMarkerTexture(hitMarker.getType(), hitMarker.getLifeTime());

        RenderSystem.enableBlend();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        Gui.blit(stack, screenMiddleX - MARKER_WIDTH / 2, screenMiddleY - MARKER_HEIGHT / 2, 0, 0, MARKER_WIDTH, MARKER_HEIGHT, MARKER_WIDTH, MARKER_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.disableBlend();
    }

    private static void drawIndicator(PoseStack stack, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vec2 playerPos, Vec2 lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vec2 diff = new Vec2((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        float angleBetween = angleBetween(lookVec, diff);
        int distanceFromCrosshair = HitIndicatorClientConfigs.DistanceFromCrosshair.get();

        Vec2 textureScale = calculateIndicatorScale(hit);
        int scaledTextureWidth = (int)Math.floor(textureScale.x);
        int scaledTextureHeight = (int)Math.floor(textureScale.y);

        renderIndicator(stack, hit, screenMiddleX, screenMiddleY, angleBetween,
                screenMiddleX - scaledTextureWidth / 2,
                screenMiddleY - scaledTextureHeight / 2 - (hit.getType() == HitIndicatorType.ND_HIT ? 0 : distanceFromCrosshair),
                scaledTextureWidth, scaledTextureHeight);
    }

    private static void drawIndicatorEdge(PoseStack stack, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vec2 playerPos, Vec2 lookVec) {
        if (hit.getType() == HitIndicatorType.ND_HIT)
            return;

        Vector3d sourceVec3d = hit.getLocation();
        Vec2 diff = new Vec2((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        float angleBetween = angleBetween(lookVec, diff);

        Vec2 textureScale = calculateIndicatorScale(hit);
        int scaledTextureWidth = (int)Math.floor(textureScale.x);
        int scaledTextureHeight = (int)Math.floor(textureScale.y);

        int blitX, blitY;
        double targetAngle = -angleBetween;
        if (targetAngle >= 45.0F && targetAngle <= 135.0F) {
            blitX = 0;
            blitY = (int)Mth.lerp((targetAngle - 45.0F) / (90.0F), 0, 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT);
        } else if (targetAngle <= -45.0F && targetAngle >= -135.0F) {
            blitX = 2 * screenMiddleX - scaledTextureWidth;
            blitY = (int)Mth.lerp((targetAngle + 45.0F) / (-90.0F), 0, 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT);
        } else if (targetAngle >= 0.0F && targetAngle <= 45.0) {
            blitX = (int)Mth.lerp(targetAngle / 45.0F, screenMiddleX, 0);
            blitY = 0;
        } else if (targetAngle >= -45.0 && targetAngle <= 0.0F) {
            blitX = (int)Mth.lerp(targetAngle / (-45.0F), screenMiddleX, 2 * screenMiddleX - scaledTextureWidth);
            blitY = 0;
        } else if (targetAngle <= 180F && targetAngle >= 135.0F) {
            blitX = (int)Mth.lerp((targetAngle - 135.0F) / (45.0F), 0, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT;
        } else {
            blitX = (int)Mth.lerp((targetAngle + 135.0F) / (-45.0F), 2 * screenMiddleX - scaledTextureWidth, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT;
        }

        renderIndicator(stack, hit, blitX + scaledTextureWidth / 2.0F, blitY + scaledTextureHeight / 2.0F,
                angleBetween, blitX, blitY, scaledTextureWidth, scaledTextureHeight);
    }

    private static Vec2 calculateIndicatorScale(HitIndicator hit) {
        if(hit.getType() == HitIndicatorType.ND_HIT)
            return new Vec2(ND_INDICATOR_WIDTH * ND_INDICATOR_DEFAULT_SCALE, ND_INDICATOR_WIDTH * ND_INDICATOR_DEFAULT_SCALE);

        float defaultScale = 1.0F + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0F;
        float scaledTextureWidthF, scaledTextureHeightF;
        if(HitIndicatorClientConfigs.EdgeOfScreenMode.get()) {
            scaledTextureWidthF = EDGE_INDICATOR_WIDTH * defaultScale;
            scaledTextureHeightF = EDGE_INDICATOR_HEIGHT * defaultScale;
        } else {
            scaledTextureWidthF = INDICATOR_WIDTH * defaultScale;
            scaledTextureHeightF = INDICATOR_HEIGHT * defaultScale;
        }

        if (HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
            float scale = (hit.getDamagePercent() > 30) ? (1.0F + hit.getDamagePercent()) / 125.0F : 1.0F;
            scale = Mth.clamp(scale, 0.0F, 3.0F);
            scaledTextureWidthF *= scale;
            scaledTextureHeightF *= scale;
        }

        if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
            float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());
            float distanceScalingCutoff = HitIndicatorClientConfigs.DistanceScalingCutoff.get();
            float distanceScaling = (distanceFromPlayer <= distanceScalingCutoff)
                    ? 0F : ((distanceFromPlayer - distanceScalingCutoff) / 10.0F);

            distanceScaling = Mth.clamp(1.0F - distanceScaling, 0.0F, 1.0F);
            scaledTextureWidthF *= distanceScaling;
            scaledTextureHeightF *= distanceScaling;
        }

        return new Vec2(scaledTextureWidthF, scaledTextureHeightF);
    }

    private static void renderIndicator(PoseStack stack, HitIndicator hit, float centerX, float centerY, float rotAngle, int posX, int posY, int scaledTextureWidth, int scaledTextureHeight) {
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0F;
        opacity = Mth.clamp(opacity / 100.0F, 0.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bindTexture(hit);
        setColor(hit, opacity);

        stack.pushPose();
        if (hit.getType() != HitIndicatorType.ND_HIT) {
            stack.translate(centerX, centerY, 0);
            stack.mulPose(Vector3f.ZP.rotationDegrees(rotAngle));
            stack.translate(-centerX, -centerY, 0);
        }

        Gui.blit(stack, posX, posY,
                0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();

        RenderSystem.disableBlend();
    }

    private static void bindTexture(HitIndicator hit) {
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) RenderSystem.setShaderTexture(0, EDGE_INDICATOR);
        else if (hit.getType() == HitIndicatorType.ND_HIT) RenderSystem.setShaderTexture(0, ND_INDICATOR);
        else if (hit.getType() == HitIndicatorType.HIT) RenderSystem.setShaderTexture(0, INDICATOR);
        else RenderSystem.setShaderTexture(0, INDICATOR_BLOCK);
    }

    private static void setColor(HitIndicator hit, float opacity) {
        if (hit.getType() == HitIndicatorType.ND_HIT || hit.getType() == HitIndicatorType.HIT)
            RenderSystem.setShaderColor(hitColorR, hitColorG, hitColorB, opacity);
        else if(hit.getType() == HitIndicatorType.BLOCK)
            RenderSystem.setShaderColor(blockColorR, blockColorG, blockColorB, opacity);
    }

    private static void bindMarkerTexture(HitMarkerType type, int lifetime) {
        if (type == HitMarkerType.KILL) {
            if (lifetime > 6) {
                RenderSystem.setShaderTexture(0, MARKER_KILL[9 - lifetime]);
                return;
            }
            RenderSystem.setShaderTexture(0, MARKER_KILL[3]);
        } else {
            if (lifetime > 6) {
                RenderSystem.setShaderTexture(0, MARKER_CRIT[9 - lifetime]);
                return;
            }
            RenderSystem.setShaderTexture(0, MARKER_CRIT[3]);
        }
    }

    private static float angleBetween(Vec2 first, Vec2 second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;
        return (float)res;
    }

    private static float calculateDistanceFromPlayer(Vector3d damageLocation) {
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
