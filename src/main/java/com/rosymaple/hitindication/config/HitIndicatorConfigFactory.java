package com.rosymaple.hitindication.config;

import com.rosymaple.hitindication.HitIndication;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HitIndicatorConfigFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) { }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new HitIndicatorConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public static class HitIndicatorConfigGui extends GuiConfig {
        public HitIndicatorConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), HitIndication.MODID, false, false, I18n.format("gui.config.main_title"));
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<>();
            list.add(new DummyConfigElement.DummyCategoryElement(
                    "Indicators",
                    "gui.config.category.indicators",
                    CategoryEntryIndicators.class));
            return list;
        }

        public static class CategoryEntryIndicators extends GuiConfigEntries.CategoryEntry {

            public CategoryEntryIndicators(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }

            @Override
            protected GuiScreen buildChildScreen() {
                Configuration config = HitIndicatorConfig.getConfig();
                ConfigElement categoryIndicators = new ConfigElement(config.getCategory(HitIndicatorConfig.CATEGORY_NAME_INDICATOR));
                List<IConfigElement> propertiesOnScreen = categoryIndicators.getChildElements();
                String windowTitle = "Indicators";
                return new GuiConfig(owningScreen, propertiesOnScreen,
                        owningScreen.modID,
                        this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart, windowTitle);
            }
        }
    }
}
