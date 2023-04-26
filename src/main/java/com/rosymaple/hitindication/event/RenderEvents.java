package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import com.rosymaple.hitindication.capability.latesthits.Hit;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT)
public class RenderEvents {
    private static final ResourceLocation INDICATOR_RED = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_red.png");
    private static final ResourceLocation INDICATOR_BLUE = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_blue.png");

    private static final int textureWidth = 42;
    private static final int textureHeight = 13;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getInstance();

        int screenMiddleX = event.getWindow().getGuiScaledWidth() / 2;
        int screenMiddleY = event.getWindow().getGuiScaledHeight() / 2;

        Vec2 lookVec = new Vec2((float)mc.player.getLookAngle().x, (float)mc.player.getLookAngle().z);
        Vec2 playerPos = new Vec2((float)mc.player.getX(), (float)mc.player.getZ());
        for(Hit hit : ClientLatestHits.latestHits) {
            drawIndicator(event.getMatrixStack(), hit, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }
    }

    private static void drawIndicator(PoseStack stack, Hit hit, int screenMiddleX, int screenMiddleY, Vec2 playerPos, Vec2 lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vec2 diff = new Vec2((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0f;
        opacity /= 100.0f;

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = (int)Math.floor(textureWidth * defaultScale);
        int scaledTextureHeight = (int)Math.floor(textureHeight * defaultScale);
        if(HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
            float scale = Mth.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
            scaledTextureWidth = (int)Math.floor(scaledTextureWidth * scale);
            scaledTextureHeight = (int)Math.floor(scaledTextureHeight * scale);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bindIndicatorTexture(hit.getIndicator());

        stack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, opacity);
        stack.translate(screenMiddleX, screenMiddleY, 0);
        stack.mulPose(Vector3f.ZP.rotationDegrees((float)angleBetween));
        stack.translate(-screenMiddleX, -screenMiddleY, 0);
        Gui.blit(stack, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 - 30 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        stack.popPose();
    }

    private static void bindIndicatorTexture(Indicator type) {
        switch(type) {
            case BLUE: RenderSystem.setShaderTexture(0, INDICATOR_BLUE);  break;
            default: RenderSystem.setShaderTexture(0, INDICATOR_RED);   break;
        }
    }

    private static double angleBetween(Vec2 first, Vec2 second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
    }

    private static Vec2 getLookVec(LocalPlayer player) {
        Vec2 vec = new Vec2((float)(-Math.sin(-player.getYRot() * Math.PI / 180.0 - Math.PI)), (float)(-Math.cos(-player.getYRot() * Math.PI / 180.0 - Math.PI)));

        //System.out.println(vec.x + " " + vec.y);
        return vec;
    }
}
