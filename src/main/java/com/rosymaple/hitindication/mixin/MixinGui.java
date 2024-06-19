package com.rosymaple.hitindication.mixin;


import com.rosymaple.hitindication.client.HitIndicationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Inject(method="<init>(Lnet/minecraft/client/Minecraft;)V", at=@At("TAIL"))
    private void addLayer(Minecraft mc, CallbackInfo info) {
        IMixinGui gui = (IMixinGui)this;
        gui.getLayers().add(HitIndicationRenderer::render);
    }
}
