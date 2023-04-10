package com.rosymaple.hitindication.config;

import com.rosymaple.hitindication.HitIndication;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HitIndicatorConfig {
    private static Configuration config = null;
    public static final String CATEGORY_NAME_INDICATOR = "indicators";
    public static int MaxIndicatorCount;
    public static boolean DisplayHitsFromNegativePotions;
    public static int FadeRate;
    public static int IndicatorOpacity;
    public static boolean ShowBlueIndicators;

    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), "HitIndication.cfg");
        config = new Configuration(configFile);
        syncFromFiles();
    }

    public static Configuration getConfig() { return config; }

    public static void syncFromFiles() {
        syncConfig(true, true);
    }

    public static void syncFromGui() {
        syncConfig(false, true);
    }

    private static void syncConfig(boolean loadFromConfigFile, boolean readFromConfigFile) {
        if(loadFromConfigFile)
            config.load();

        Property maxIndicatorCount = config.get(CATEGORY_NAME_INDICATOR, "max_indicator_count", 0);
        maxIndicatorCount.setLanguageKey("gui.config.indicators.max_indicator_count");
        maxIndicatorCount.setComment(I18n.format("gui.config.indicators.max_indicator_count.comment"));
        maxIndicatorCount.setMinValue(0);

        Property displayHitsFromNegativePotions
                = config.get(CATEGORY_NAME_INDICATOR, "display_hits_from_negative_potions", true);
        displayHitsFromNegativePotions.setLanguageKey("gui.config.indicators.display_hits_from_negative_potions");
        displayHitsFromNegativePotions.setComment(I18n.format("gui.config.indicators.display_hits_from_negative_potions.comment"));

        Property fadeRate = config.get(CATEGORY_NAME_INDICATOR, "fade_rate", 50);
        fadeRate.setLanguageKey("gui.config.indicators.fade_rate");
        fadeRate.setComment(I18n.format("gui.config.indicators.fade_rate.comment"));
        fadeRate.setMinValue(0);

        Property indicatorOpacity = config.get(CATEGORY_NAME_INDICATOR, "indicator_opacity", 25);
        indicatorOpacity.setLanguageKey("gui.config.indicators.indicator_opacity");
        indicatorOpacity.setComment(I18n.format("gui.config.indicators.indicator_opacity.comment"));
        indicatorOpacity.setMinValue(0);
        indicatorOpacity.setMaxValue(100);

        Property displayBlueIndicators = config.get(CATEGORY_NAME_INDICATOR, "display_blue_indicators", true);
        displayBlueIndicators.setLanguageKey("gui.config.indicators.display_blue_indicators");
        displayBlueIndicators.setComment(I18n.format("gui.config.indicators.display_blue_indicators.comment"));

        List<String> propertyOrderIndicators = new ArrayList<>();
        propertyOrderIndicators.add(maxIndicatorCount.getName());
        propertyOrderIndicators.add(displayHitsFromNegativePotions.getName());
        propertyOrderIndicators.add(fadeRate.getName());
        propertyOrderIndicators.add(indicatorOpacity.getName());
        propertyOrderIndicators.add(displayBlueIndicators.getName());
        config.setCategoryPropertyOrder(CATEGORY_NAME_INDICATOR, propertyOrderIndicators);

        if(readFromConfigFile) {
            MaxIndicatorCount = maxIndicatorCount.getInt();
            DisplayHitsFromNegativePotions = displayHitsFromNegativePotions.getBoolean();
            FadeRate = fadeRate.getInt();
            IndicatorOpacity = indicatorOpacity.getInt();
            ShowBlueIndicators = displayBlueIndicators.getBoolean();
        }

        maxIndicatorCount.set(MaxIndicatorCount);
        displayHitsFromNegativePotions.set(DisplayHitsFromNegativePotions);
        fadeRate.set(FadeRate);
        indicatorOpacity.set(IndicatorOpacity);
        displayBlueIndicators.set(ShowBlueIndicators);

        if(config.hasChanged())
            config.save();
    }

    @Mod.EventBusSubscriber(modid = HitIndication.MODID)
    public static class ConfigEventHandler {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
            if(event.getModID() == HitIndication.MODID) {
                syncFromGui();
            }
        }
    }
}
