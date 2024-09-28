package com.rosymaple.hitindication;

import com.rosymaple.hitindication.client.ModKeyBindings;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = HitIndication.MODID, name = HitIndication.NAME, version = HitIndication.VERSION, guiFactory = HitIndication.GUI_FACTORY)
public class HitIndication
{
    public static final String MODID = "hitindication";
    public static final String NAME = "Hit Indication";
    public static final String VERSION = "2.0";
    public static final String GUI_FACTORY = "com.rosymaple.hitindication.config.HitIndicatorConfigFactory";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        HitIndicatorConfig.preInit();
        ModPackets.register();
        ModKeyBindings.init();
    }
}
