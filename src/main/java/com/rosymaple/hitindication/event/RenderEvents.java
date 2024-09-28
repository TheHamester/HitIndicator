package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.latesthits.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;


@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT)
public class RenderEvents {
    private static final ResourceLocation HIT_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator.png");
    private static final ResourceLocation EDGE_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/edge_indicator.png");
    private static final ResourceLocation BLOCK_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_block.png");
    private static final ResourceLocation ND_HIT_INDICATOR = new ResourceLocation(HitIndication.MODID, "textures/hit/nd_person_damage.png");

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
        if(event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getInstance();
        TextureManager textureManager = mc.getTextureManager();
        if(mc.player == null || mc.world == null)
            return;

        int screenMiddleX = event.getWindow().getScaledWidth() / 2;
        int screenMiddleY = event.getWindow().getScaledHeight() / 2;

        updateColorsIfNeeded();

        Vector3d viewVec = calculateViewVector(0, mc.player.getYaw(0));
        Vector2f lookVec = new Vector2f((float)viewVec.x, (float)viewVec.z);
        Vector2f playerPos = new Vector2f((float)mc.player.getPosX(), (float)mc.player.getPosZ());
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicatorEdge(event.getMatrixStack(), textureManager, hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        } else {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicator(event.getMatrixStack(), textureManager, hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }

        if(ClientLatestHits.currentHitMarker != null)
            drawHitMarker(event.getMatrixStack(), textureManager, ClientLatestHits.currentHitMarker, screenMiddleX, screenMiddleY);
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

    private static void drawHitMarker(MatrixStack stack, TextureManager textureManager, HitMarker hitMarker, int screenMiddleX, int screenMiddleY) {
        float opacity = hitMarker.getType() == HitMarkerType.CRIT ? 0.3F : 0.6F;

        bindMarkerTexture(textureManager, hitMarker.getType(), hitMarker.getLifeTime());

        RenderSystem.enableBlend();

        GL11.glColor4f(1, 1, 1, opacity);
        AbstractGui.blit(stack, screenMiddleX - MARKER_WIDTH / 2, screenMiddleY - MARKER_HEIGHT / 2 , 0, 0, MARKER_WIDTH, MARKER_HEIGHT, MARKER_WIDTH, MARKER_HEIGHT);
        GL11.glColor4f(1, 1, 1, 1);

        RenderSystem.disableBlend();
    }

    private static void drawIndicator(MatrixStack stack, TextureManager textureManager, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vector2f playerPos, Vector2f lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vector2f diff = new Vector2f((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        float angleBetween = angleBetween(lookVec, diff);
        int distanceFromCrosshair = HitIndicatorClientConfigs.DistanceFromCrosshair.get();

        Vector2f textureScale = calculateIndicatorScale(hit);
        int scaledTextureWidth = (int)Math.floor(textureScale.x);
        int scaledTextureHeight = (int)Math.floor(textureScale.y);

        renderIndicator(stack, textureManager, hit, screenMiddleX, screenMiddleY, angleBetween,
                screenMiddleX - scaledTextureWidth / 2,
                screenMiddleY - scaledTextureHeight / 2 - (hit.getType() == HitIndicatorType.ND_HIT ? 0 : distanceFromCrosshair),
                scaledTextureWidth, scaledTextureHeight);
    }

    private static void drawIndicatorEdge(MatrixStack stack, TextureManager textureManager, HitIndicator hit, int screenMiddleX, int screenMiddleY, Vector2f playerPos, Vector2f lookVec) {
        if (hit.getType() == HitIndicatorType.ND_HIT)
            return;

        Vector3d sourceVec3d = hit.getLocation();
        Vector2f diff = new Vector2f((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        float angleBetween = angleBetween(lookVec, diff);

        Vector2f textureScale = calculateIndicatorScale(hit);
        int scaledTextureWidth = (int)Math.floor(textureScale.x);
        int scaledTextureHeight = (int)Math.floor(textureScale.y);

        int blitX, blitY;
        double targetAngle = -angleBetween;
        if (targetAngle >= 45.0F && targetAngle <= 135.0F) {
            blitX = 0;
            blitY = (int)MathHelper.lerp((targetAngle - 45.0F) / (90.0F), 0, 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT);
        } else if (targetAngle <= -45.0F && targetAngle >= -135.0F) {
            blitX = 2 * screenMiddleX - scaledTextureWidth;
            blitY = (int)MathHelper.lerp((targetAngle + 45.0F) / (-90.0F), 0, 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT);
        } else if (targetAngle >= 0.0F && targetAngle <= 45.0) {
            blitX = (int)MathHelper.lerp(targetAngle / 45.0F, screenMiddleX, 0);
            blitY = 0;
        } else if (targetAngle >= -45.0 && targetAngle <= 0.0F) {
            blitX = (int)MathHelper.lerp(targetAngle / (-45.0F), screenMiddleX, 2 * screenMiddleX - scaledTextureWidth);
            blitY = 0;
        } else if (targetAngle <= 180F && targetAngle >= 135.0F) {
            blitX = (int)MathHelper.lerp((targetAngle - 135.0F) / (45.0F), 0, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT;
        } else {
            blitX = (int)MathHelper.lerp((targetAngle + 135.0F) / (-45.0F), 2 * screenMiddleX - scaledTextureWidth, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight - HUD_HEIGHT;
        }

        renderIndicator(stack, textureManager, hit, blitX + scaledTextureWidth / 2.0F, blitY + scaledTextureHeight / 2.0F,
                angleBetween, blitX, blitY, scaledTextureWidth, scaledTextureHeight);
    }

    private static Vector2f calculateIndicatorScale(HitIndicator hit) {
        if(hit.getType() == HitIndicatorType.ND_HIT)
            return new Vector2f(ND_INDICATOR_WIDTH * ND_INDICATOR_DEFAULT_SCALE, ND_INDICATOR_WIDTH * ND_INDICATOR_DEFAULT_SCALE);

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
            scale = MathHelper.clamp(scale, 0.0F, 3.0F);
            scaledTextureWidthF *= scale;
            scaledTextureHeightF *= scale;
        }

        if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
            float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());
            float distanceScalingCutoff = HitIndicatorClientConfigs.DistanceScalingCutoff.get();
            float distanceScaling = (distanceFromPlayer <= distanceScalingCutoff)
                    ? 0F : ((distanceFromPlayer - distanceScalingCutoff) / 10.0F);

            distanceScaling = MathHelper.clamp(1.0F - distanceScaling, 0.0F, 1.0F);
            scaledTextureWidthF *= distanceScaling;
            scaledTextureHeightF *= distanceScaling;
        }

        return new Vector2f(scaledTextureWidthF, scaledTextureHeightF);
    }

    private static void renderIndicator(MatrixStack stack, TextureManager textureManager, HitIndicator hit, float centerX, float centerY, float rotAngle, int posX, int posY, int scaledTextureWidth, int scaledTextureHeight) {
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0F;
        opacity = MathHelper.clamp(opacity / 100.0F, 0.0F, 1.0F);

        RenderSystem.enableBlend();
        bindTexture(textureManager, hit);
        setColor(hit, opacity);

        GL11.glPushMatrix();
        if (hit.getType() != HitIndicatorType.ND_HIT) {
            GL11.glTranslatef(centerX, centerY, 0);
            GL11.glRotatef(rotAngle, 0, 0, 1);
            GL11.glTranslatef(-centerX, -centerY, 0);
        }

        AbstractGui.blit(stack, posX, posY,
                0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();

        RenderSystem.disableBlend();
    }


    private static void bindTexture(TextureManager textureManager, HitIndicator hit) {
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) textureManager.bindTexture(EDGE_INDICATOR);
        else if (hit.getType() == HitIndicatorType.ND_HIT) textureManager.bindTexture(ND_HIT_INDICATOR);
        else if (hit.getType() == HitIndicatorType.HIT) textureManager.bindTexture(HIT_INDICATOR);
        else textureManager.bindTexture(BLOCK_INDICATOR);
    }

    private static void setColor(HitIndicator hit, float opacity) {
        if (hit.getType() == HitIndicatorType.ND_HIT || hit.getType() == HitIndicatorType.HIT)
            GL11.glColor4f(hitColorR, hitColorG, hitColorB, opacity);
        else if(hit.getType() == HitIndicatorType.BLOCK)
            GL11.glColor4f(blockColorR, blockColorG, blockColorB, opacity);
    }

    private static void bindMarkerTexture(TextureManager textureManager, HitMarkerType type, int lifetime) {
        if (type == HitMarkerType.KILL) {
            if (lifetime > 6) {
                textureManager.bindTexture(MARKER_KILL[9 - lifetime]);
                return;
            }
            textureManager.bindTexture(MARKER_KILL[3]);
        } else {
            if (lifetime > 6) {
                textureManager.bindTexture(MARKER_CRIT[9 - lifetime]);
                return;
            }
            textureManager.bindTexture(MARKER_CRIT[3]);
        }
    }

    private static float angleBetween(Vector2f first, Vector2f second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;
        return (float)res;
    }

    private static Vector3d calculateViewVector(float pPitch, float pYaw) {
        float f = pPitch * ((float) Math.PI / 180F);
        float f1 = -pYaw * ((float) Math.PI / 180F);
        double f2 = Math.cos(f1);
        double f3 = Math.sin(f1);
        double f4 = Math.cos(f);
        double f5 = Math.sin(f);
        return new Vector3d(f3 * f4, -f5, f2 * f4);
    }

    private static float calculateDistanceFromPlayer(Vector3d damageLocation) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        double d0 = damageLocation.x - player.getPosX();
        double d1 = damageLocation.y - player.getPosY();
        double d2 = damageLocation.z - player.getPosZ();
        return (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
}
