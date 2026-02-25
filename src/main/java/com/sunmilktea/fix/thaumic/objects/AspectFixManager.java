
package com.sunmilktea.fix.thaumic.objects;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class AspectFixManager {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix AspectFixManager");
    private static final Gson GSON = new Gson();
    private static final File CONFIG_FILE = new File("config/ThaumicFixAspects.json");

    private static final Map<String, AspectList> aspectFixes = new HashMap<>();

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            createDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type configType = new TypeToken<Config>() {}.getType();
            Config config = GSON.fromJson(reader, configType);

            if (config != null && config.fixes != null) {
                for (AspectFixDefinition fix : config.fixes) {
                    if (fix.item == null || fix.aspects == null) {
                        LOGGER.warn("Invalid aspect fix entry found. 'item' and 'aspects' are required.");
                        continue;
                    }

                    AspectList aspectList = new AspectList();
                    for (Map.Entry<String, Integer> entry : fix.aspects.entrySet()) {
                        Aspect aspect = Aspect.getAspect(entry.getKey());
                        if (aspect != null) {
                            aspectList.add(aspect, entry.getValue());
                        } else {
                            LOGGER.warn("Unknown aspect '{}' for item '{}'.", entry.getKey(), fix.item);
                        }
                    }

                    if (aspectList.size() > 0) {
                        // 解析物品标识符
                        String[] parts = fix.item.split("@");
                        if (parts.length < 2) {
                            LOGGER.warn("Invalid item key format for '{}'. Expected 'modid:name@meta'.", fix.item);
                            continue;
                        }
                        Item item = GameRegistry.findItem(parts[0].split(":")[0], parts[0].split(":")[1]);
                        int meta = Integer.parseInt(parts[1]);

                        if (item != null) {
                            ThaumcraftApi.registerObjectTag(new ItemStack(item, 1, meta), aspectList);
                            LOGGER.info(
                                "Registered manual aspects for '{}' with aspects: {}",
                                fix.item,
                                aspectList.getAspects());
                        } else {
                            LOGGER.warn("Could not find item for key '{}' to register aspects.", fix.item);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load aspect fix config.", e);
        }
    }

    private static String generateItemStackKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "null";
        }
        String key = Item.itemRegistry.getNameForObject(stack.getItem()) + "@" + stack.getItemDamage();
        // 注意：API注册不支持NBT，所以我们只使用物品和meta
        return key;
    }

    private static void createDefaultConfig() {
        Config defaultConfig = new Config();
        AspectFixDefinition exampleFix = new AspectFixDefinition();

        // 为了生成正确的key，我们需要一个实际的ItemStack对象
        Item ieMetalDevice = GameRegistry.findItem("ImmersiveEngineering", "metalDevice");
        if (ieMetalDevice != null) {
            ItemStack stack = new ItemStack(ieMetalDevice, 1, 3);
            exampleFix.item = generateItemStackKey(stack);
        } else {
            // 如果找不到物品，就使用硬编码的字符串作为备用
            exampleFix.item = "ImmersiveEngineering:metalDevice@3";
            LOGGER.warn(
                "Could not find item 'ImmersiveEngineering:metalDevice' to generate default config key. Using fallback string.");
        }

        exampleFix.aspects = new HashMap<>();
        exampleFix.aspects.put("metallum", 15);
        exampleFix.aspects.put("machina", 10);
        exampleFix.aspects.put("ordo", 5);

        defaultConfig.fixes = Collections.singletonList(exampleFix);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(defaultConfig, writer);
            LOGGER.info("Created default aspect fix config file at: {}", CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to create default aspect fix config.", e);
        }
    }

    private static class Config {

        List<AspectFixDefinition> fixes;
    }

    private static class AspectFixDefinition {

        String item;
        Map<String, Integer> aspects;
    }
}
