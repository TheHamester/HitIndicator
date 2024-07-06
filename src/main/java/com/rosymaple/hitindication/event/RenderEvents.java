package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.latesthits.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.List;

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
    private static final int ND_TEXTURE_SIZE = 66;
    private static final int INDICATOR_WIDTH = 42;
    private static final int INDICATOR_HEIGHT = 13;
    private static final int MARKER_WIDTH = 20;
    private static final int MARKER_HEIGHT = 20;
    private static final int EDGE_INDICATOR_WIDTH = 16;
    private static final int EDGE_INDICATOR_HEIGHT = 16;

    private static String lastHitColorString = "FF0000";
    private static String lastBlockColorString = "0000FF";
    private static String lastProximityColorString = "2F86C4";
    private static float hitColorR = 1.0F, hitColorG = 0.0F, hitColorB = 0.0F;
    private static float blockColorR = 0.0F, blockColorG = 0.0F, blockColorB = 1.0F;
    private static float proximityColorR = 0.18431F, proximityColorG = 0.52549F, proximityColorB = 0.76862F;

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
                drawIndicatorEdge(event.getMatrixStack(), hit, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);
        } else {
            for (HitIndicator hit : ClientLatestHits.latestHitIndicators)
                drawIndicator(event.getMatrixStack(), hit, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }

        if(HitIndicatorClientConfigs.EnableProximityIndicators.get()) {
            List<Entity> entities = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(mc.player.getPosition()).grow(HitIndicatorClientConfigs.ProximityIndicatorRadius.get()));
            for(Entity e : entities) {
                if(!(e instanceof ProjectileEntity && (Math.abs(e.prevPosX - e.getPosX()) > 0.1F || Math.abs(e.prevPosY - e.getPosY()) > 0.1F || Math.abs(e.prevPosZ - e.getPosZ()) > 0.1F)) && !(e instanceof IMob))
                    continue;

                HitIndicator indicator = new HitIndicator(e.getPosX(), e.getPosY(), e.getPosZ(), HitIndicatorType.PROXIMITY, 0);
                drawIndicator(event.getMatrixStack(), indicator, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);
            }
        }
        if(ClientLatestHits.currentHitMarker != null)
            drawHitMarker(event.getMatrixStack(), textureManager, ClientLatestHits.currentHitMarker, screenMiddleX, screenMiddleY);
    }

    private static void updateColorsIfNeeded() {
        String currentHitColor = HitIndicatorClientConfigs.HitIndicatorColor.get();
        String currentBlockColor = HitIndicatorClientConfigs.BlockIndicatorColor.get();
        String currentProximityColor = HitIndicatorClientConfigs.ProximityIndicatorColor.get();
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

        if (!lastProximityColorString.equals(currentProximityColor)) {
            try {
                int parsedValue = Integer.parseInt(currentProximityColor, 16);
                proximityColorR = (parsedValue >> 16 & 0xFF) / 255.0F;
                proximityColorG = ((parsedValue >> 8) & 0xFF) / 255.0F;
                proximityColorB = (parsedValue & 0xFF) / 255.0F;
            } catch (Exception e) {
                proximityColorR = 0.18431F;
                proximityColorG = 0.52549F;
                proximityColorB = 0.76862F;
            }
            lastProximityColorString = currentProximityColor;
        }
    }

    private static void drawHitMarker(MatrixStack stack, TextureManager textureManager, HitMarker hitMarker, int screenMiddleX, int screenMiddleY) {
        float opacity = hitMarker.getType() == HitMarkerType.CRIT ? 30 : 60;
        opacity /= 100.0f;

        bindMarkerTexture(textureManager, hitMarker.getType(), hitMarker.getLifeTime());

        float defaultScale = 1;
        int scaledTextureWidth = (int)Math.floor(MARKER_WIDTH * defaultScale);
        int scaledTextureHeight = (int)Math.floor(MARKER_HEIGHT * defaultScale);
        GL11.glColor4f(1, 1, 1, opacity);
        AbstractGui.blit(stack, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void drawIndicator(MatrixStack stack, HitIndicator hit, TextureManager textureManager, int screenMiddleX, int screenMiddleY, Vector2f playerPos, Vector2f lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vector2f diff = new Vector2f((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);
        int distanceFromCrosshair = HitIndicatorClientConfigs.DistanceFromCrosshair.get();

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = hit.getType() != HitIndicatorType.ND_HIT ? (int)Math.floor((hit.getType() == HitIndicatorType.PROXIMITY ? EDGE_INDICATOR_WIDTH : INDICATOR_WIDTH) * defaultScale) : (int)Math.floor(ND_TEXTURE_SIZE * 1.25);
        int scaledTextureHeight = hit.getType() != HitIndicatorType.ND_HIT ? (int)Math.floor((hit.getType() == HitIndicatorType.PROXIMITY ? EDGE_INDICATOR_HEIGHT : INDICATOR_HEIGHT) * defaultScale) : (int)Math.floor(ND_TEXTURE_SIZE * 1.25);

        float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());
        if(hit.getType() == HitIndicatorType.PROXIMITY) {
            distanceFromCrosshair -= 10;
            scaledTextureWidth *= 0.75F;
            scaledTextureHeight *= 0.75F;
        }

        if(hit.getType() != HitIndicatorType.ND_HIT && hit.getType() != HitIndicatorType.PROXIMITY) {
            if (HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
                float scale = MathHelper.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
                scaledTextureWidth = (int) Math.floor(scaledTextureWidth * scale);
                scaledTextureHeight = (int) Math.floor(scaledTextureHeight * scale);
            }

            if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
                float distanceScalingCutoff = HitIndicatorClientConfigs.DistanceScalingCutoff.get();
                float distanceScaling = 1.0f - (distanceFromPlayer <= distanceScalingCutoff ? 0f : (distanceFromPlayer - distanceScalingCutoff) / 10.0f);
                if (distanceScaling > 1) distanceScaling = 1;
                if (distanceScaling < 0) distanceScaling = 0;
                scaledTextureWidth = (int) Math.floor(scaledTextureWidth * distanceScaling);
                scaledTextureHeight = (int) Math.floor(scaledTextureHeight * distanceScaling);
            }
        }

        float opacity = hit.getType() != HitIndicatorType.PROXIMITY ?
                (hit.getLifeTime() >= 25
                        ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                        : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0f) / 100.0F
                : 1.0F - distanceFromPlayer / HitIndicatorClientConfigs.ProximityIndicatorRadius.get();
        opacity = MathHelper.clamp(opacity, 0.0F, 1.0F);
        int border = 2 * HitIndicatorClientConfigs.ProximityIndicatorBorder.get();

        bindTexture(textureManager, hit);

        GL11.glPushMatrix();
        GL11.glTranslatef(screenMiddleX, screenMiddleY, 0);
        if(hit.getType() != HitIndicatorType.ND_HIT)
            GL11.glRotatef((float)angleBetween, 0, 0, 1);
        GL11.glTranslatef(-screenMiddleX, -screenMiddleY, 0);

        if(hit.getType() == HitIndicatorType.PROXIMITY && border > 0) {
            GL11.glColor4f(1, 1, 1, opacity);
            AbstractGui.blit(stack, screenMiddleX - (scaledTextureWidth + border) / 2, screenMiddleY - (scaledTextureHeight + border) / 2 - (hit.getType() == HitIndicatorType.ND_HIT ? 0 : distanceFromCrosshair), 0, 0, scaledTextureWidth + border, scaledTextureHeight + border, scaledTextureWidth + border, scaledTextureHeight + border);
        }

        setColor(hit, opacity);
        AbstractGui.blit(stack, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 - (hit.getType() == HitIndicatorType.ND_HIT ? 0 : distanceFromCrosshair), 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private static void drawIndicatorEdge(MatrixStack stack, HitIndicator hit, TextureManager textureManager, int screenMiddleX, int screenMiddleY, Vector2f playerPos, Vector2f lookVec) {
        if (hit.getType() == HitIndicatorType.ND_HIT && HitIndicatorClientConfigs.EdgeOfScreenMode.get())
            return;

        Vector3d sourceVec3d = hit.getLocation();
        Vector2f diff = new Vector2f((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = (int)Math.floor(EDGE_INDICATOR_WIDTH * defaultScale);
        int scaledTextureHeight = (int)Math.floor(EDGE_INDICATOR_HEIGHT * defaultScale);

        float distanceFromPlayer = calculateDistanceFromPlayer(hit.getLocation());

        if (HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
            float scale = MathHelper.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
            scaledTextureWidth = (int) Math.floor(scaledTextureWidth * scale);
            scaledTextureHeight = (int) Math.floor(scaledTextureHeight * scale);
        }

        if (HitIndicatorClientConfigs.EnableDistanceScaling.get()) {
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
            blitY = (int)MathHelper.lerp((targetAngle - 45.0F) / (90.0F), 0, 2 * screenMiddleY - scaledTextureHeight);
        } else if (targetAngle <= -45.0F && targetAngle >= -135.0F) {
            blitX = 2 * screenMiddleX - scaledTextureWidth;
            blitY = (int)MathHelper.lerp((targetAngle + 45.0F) / (-90.0F), 0, 2 * screenMiddleY - scaledTextureHeight);
        } else if (targetAngle >= 0.0F && targetAngle <= 45.0) {
            blitX = (int)MathHelper.lerp(targetAngle / 45.0F, screenMiddleX, 0);
            blitY = 0;
        } else if (targetAngle >= -45.0 && targetAngle <= 0.0F) {
            blitX = (int)MathHelper.lerp(targetAngle / (-45.0F), screenMiddleX, 2 * screenMiddleX - scaledTextureWidth);
            blitY = 0;
        } else if (targetAngle <= 180F && targetAngle >= 135.0F) {
            blitX = (int)MathHelper.lerp((targetAngle - 135.0F) / (45.0F), 0, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight;
        } else {
            blitX = (int)MathHelper.lerp((targetAngle + 135.0F) / (-45.0F), 2 * screenMiddleX - scaledTextureWidth, screenMiddleX);
            blitY = 2 * screenMiddleY - scaledTextureHeight;
        }

        float opacity = hit.getType() != HitIndicatorType.PROXIMITY ?
                (hit.getLifeTime() >= 25
                        ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                        : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0f) / 100.0F
                : 1.0F - distanceFromPlayer / HitIndicatorClientConfigs.ProximityIndicatorRadius.get();
        opacity = MathHelper.clamp(opacity, 0.0F, 1.0F);

        bindTexture(textureManager, hit);
        setColor(hit, opacity);

        GL11.glPushMatrix();
        GL11.glTranslatef(blitX + scaledTextureWidth / 2.0F, blitY + scaledTextureHeight / 2.0F, 0);
        GL11.glRotatef((float)angleBetween, 0, 0, 1);
        GL11.glTranslatef(-blitX - scaledTextureWidth / 2.0F, -blitY - scaledTextureHeight / 2.0F, 0);

        AbstractGui.blit(stack, blitX, blitY,
                0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }


    private static void bindTexture(TextureManager textureManager, HitIndicator hit) {
        if (HitIndicatorClientConfigs.EdgeOfScreenMode.get()) textureManager.bindTexture(EDGE_INDICATOR);
        else if (hit.getType() == HitIndicatorType.ND_HIT) textureManager.bindTexture(ND_HIT_INDICATOR);
        else if (hit.getType() == HitIndicatorType.HIT) textureManager.bindTexture(HIT_INDICATOR);
        else if (hit.getType() == HitIndicatorType.PROXIMITY) textureManager.bindTexture(EDGE_INDICATOR);
        else textureManager.bindTexture(BLOCK_INDICATOR);
    }

    private static void setColor(HitIndicator hit, float opacity) {
        if (hit.getType() == HitIndicatorType.ND_HIT || hit.getType() == HitIndicatorType.HIT)
            GL11.glColor4f(hitColorR, hitColorG, hitColorB, opacity);
        else if(hit.getType() == HitIndicatorType.BLOCK)
            GL11.glColor4f(blockColorR, blockColorG, blockColorB, opacity);
        else
            GL11.glColor4f(proximityColorR, proximityColorG, proximityColorB, opacity);
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

    private static double angleBetween(Vector2f first, Vector2f second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
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
