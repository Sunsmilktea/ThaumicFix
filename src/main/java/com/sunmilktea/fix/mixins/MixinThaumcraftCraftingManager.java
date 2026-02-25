package com.sunmilktea.fix.mixins;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

@Mixin(value = ThaumcraftCraftingManager.class, remap = false)
public abstract class MixinThaumcraftCraftingManager {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");

    @Inject(method = "generateTagsFromArcaneRecipes", at = @At("HEAD"), cancellable = true)
    private static void generateTagsFromArcaneRecipes(Item item, int meta, ArrayList<List> history,
        CallbackInfoReturnable<AspectList> cir) {

        // 1. 过滤 null item
        if (item == null) {
            LOGGER.debug("ThaumicFix: 跳过 null Item 的奥术标签生成");
            cir.setReturnValue(new AspectList());
            return;
        }

        try {
            AspectList result = new AspectList();

            for (Object obj : ThaumcraftApi.getCraftingRecipes()) {
                if (!(obj instanceof IArcaneRecipe)) continue;

                IArcaneRecipe recipe = (IArcaneRecipe) obj;
                ItemStack output = recipe.getRecipeOutput();

                // 2. ★ 关键修复：在访问 output 前检查其是否为 null
                if (output == null || output.getItem() == null) {
                    continue; // 跳过这个有问题的配方
                }

                // 3. 使用传入的 item 和 meta 进行匹配
                if (output.getItem() == item && output.getItemDamage() == meta) {
                    AspectList recipeAspects = recipe.getAspects();
                    if (recipeAspects != null) {
                        for (Aspect aspect : recipeAspects.getAspects()) {
                            result.add(aspect, recipeAspects.getAmount(aspect));
                        }
                    }
                }
            }

            cir.setReturnValue(result);

        } catch (Throwable t) {
            // 创建一个临时的 ItemStack 用于日志记录
            ItemStack tempStack = new ItemStack(item, 1, meta);
            LOGGER.error(
                "ThaumicFix 在奥术标签生成过程中捕获到意外错误！物品: {}",
                tempStack.getDisplayName() != null ? tempStack.getDisplayName() : "未知物品",
                t);
            cir.setReturnValue(new AspectList()); // 返回空列表以防崩溃
        }
    }
}
