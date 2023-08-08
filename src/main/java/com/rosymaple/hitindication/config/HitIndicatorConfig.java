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
import java.util.Map;

public class HitIndicatorConfig {
    private static Configuration config = null;
    public static final String CATEGORY_NAME_INDICATOR = "client";
    public static int MaxIndicatorCount;
    public static boolean DisplayHitsFromNegativePotions;
    public static int FadeRate;
    public static int IndicatorOpacity;
    public static boolean ShowBlueIndicators;
    public static boolean SizeDependsOnDamage;
    public static int IndicatorDefaultScale;
    public static boolean EnableHitMarkers;

    public static boolean EnableHitIndication;
    public static boolean EnableDistanceScaling;
    public static int DistanceScalingCutoff;
    public static boolean EnableNonDirectionalDamage;

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

        Property enableHitIndication = config.get(CATEGORY_NAME_INDICATOR, "enable_hit_indication", true);
        enableHitIndication.setLanguageKey("hitindication.gui.config.indicators.enable_hit_indication");

        Property maxIndicatorCount = config.get(CATEGORY_NAME_INDICATOR, "max_indicator_count", 0);
        maxIndicatorCount.setLanguageKey("hitindication.gui.config.indicators.max_indicator_count");
        maxIndicatorCount.setMinValue(0);

        Property displayHitsFromNegativePotions
                = config.get(CATEGORY_NAME_INDICATOR, "display_hits_from_negative_potions", true);
        displayHitsFromNegativePotions.setLanguageKey("hitindication.gui.config.indicators.display_hits_from_negative_potions");

        Property fadeRate = config.get(CATEGORY_NAME_INDICATOR, "fade_rate", 50);
        fadeRate.setLanguageKey("hitindication.gui.config.indicators.fade_rate");
        fadeRate.setMinValue(0);

        Property indicatorOpacity = config.get(CATEGORY_NAME_INDICATOR, "indicator_opacity", 25);
        indicatorOpacity.setLanguageKey("hitindication.gui.config.indicators.indicator_opacity");
        indicatorOpacity.setMinValue(0);
        indicatorOpacity.setMaxValue(100);

        Property indicatorDefaultScale = config.get(CATEGORY_NAME_INDICATOR, "indicator_default_scale", 25);
        indicatorDefaultScale.setLanguageKey("hitindication.gui.config.indicators.indicator_default_scale");
        indicatorDefaultScale.setMinValue(0);
        indicatorDefaultScale.setMaxValue(100);

        Property displayBlueIndicators = config.get(CATEGORY_NAME_INDICATOR, "display_blue_indicators", true);
        displayBlueIndicators.setLanguageKey("hitindication.gui.config.indicators.display_blue_indicators");

        Property sizeDependsOnDamage = config.get(CATEGORY_NAME_INDICATOR, "size_depends_on_damage", false);
        sizeDependsOnDamage.setLanguageKey("hitindication.gui.config.indicators.size_depends_on_damage");

        Property enableDistanceScaling = config.get(CATEGORY_NAME_INDICATOR, "enable_distance_scaling", true);
        enableDistanceScaling.setLanguageKey("hitindication.gui.config.indicators.enable_distance_scaling");

        Property distanceScalingCutoff = config.get(CATEGORY_NAME_INDICATOR, "distance_scaling_cutoff", 10);
        distanceScalingCutoff.setLanguageKey("hitindication.gui.config.indicators.distance_scaling_cutoff");
        distanceScalingCutoff.setMinValue(0);

        Property enableHitMarkers = config.get(CATEGORY_NAME_INDICATOR, "enable_hit_markers", false);
        enableHitMarkers.setLanguageKey("hitindication.gui.config.indicators.enable_hit_markers");

        Property enableNonDirectionalDamage = config.get(CATEGORY_NAME_INDICATOR, "enable_non_directional_damage", false);
        enableNonDirectionalDamage.setLanguageKey("hitindication.gui.config.indicators.enable_non_directional_damage");

        List<String> propertyOrderIndicators = new ArrayList<>();
        propertyOrderIndicators.add(enableHitIndication.getName());
        propertyOrderIndicators.add(maxIndicatorCount.getName());
        propertyOrderIndicators.add(displayHitsFromNegativePotions.getName());
        propertyOrderIndicators.add(displayBlueIndicators.getName());
        propertyOrderIndicators.add(fadeRate.getName());
        propertyOrderIndicators.add(indicatorOpacity.getName());
        propertyOrderIndicators.add(indicatorDefaultScale.getName());
        propertyOrderIndicators.add(sizeDependsOnDamage.getName());
        propertyOrderIndicators.add(enableNonDirectionalDamage.getName());
        propertyOrderIndicators.add(enableDistanceScaling.getName());
        propertyOrderIndicators.add(distanceScalingCutoff.getName());
        propertyOrderIndicators.add(enableHitMarkers.getName());
        config.setCategoryPropertyOrder(CATEGORY_NAME_INDICATOR, propertyOrderIndicators);

        if(readFromConfigFile) {
            EnableHitIndication = enableHitIndication.getBoolean();
            MaxIndicatorCount = maxIndicatorCount.getInt();
            DisplayHitsFromNegativePotions = displayHitsFromNegativePotions.getBoolean();
            FadeRate = fadeRate.getInt();
            IndicatorOpacity = indicatorOpacity.getInt();
            ShowBlueIndicators = displayBlueIndicators.getBoolean();
            SizeDependsOnDamage = sizeDependsOnDamage.getBoolean();
            IndicatorDefaultScale = indicatorDefaultScale.getInt();
            EnableDistanceScaling = enableDistanceScaling.getBoolean();
            DistanceScalingCutoff = distanceScalingCutoff.getInt();
            EnableHitMarkers = enableHitMarkers.getBoolean();
            EnableNonDirectionalDamage = enableNonDirectionalDamage.getBoolean();
        }

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
