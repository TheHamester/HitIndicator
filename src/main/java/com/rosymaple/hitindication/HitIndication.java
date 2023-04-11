package com.rosymaple.hitindication;

import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = HitIndication.MODID, name = HitIndication.NAME, version = HitIndication.VERSION, guiFactory = HitIndication.GUI_FACTORY)
public class HitIndication
{
    public static final String MODID = "hitindication";
    public static final String NAME = "Hit Indication";
    public static final String VERSION = "1.1.1";
    public static final String GUI_FACTORY = "com.rosymaple.hitindication.config.HitIndicatorConfigFactory";
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        HitIndicatorConfig.preInit();

        ModPackets.register();
        LatestHitsProvider.register();
    }
}
