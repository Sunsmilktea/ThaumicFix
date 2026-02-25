package com.sunmilktea.fix.thaumic.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thaumcraft.api.aspects.Aspect;

public class ThreadCrashyAspectScanner extends Thread {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix Crashy Aspect Scanner");

    private final EntityPlayer notifyingPlayer;

    public ThreadCrashyAspectScanner(EntityPlayer player) {
        this.notifyingPlayer = player;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("开始扫描所有注册的 Thaumcraft Aspects...");
            if (notifyingPlayer != null) {
                notifyingPlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GOLD + "========================================"));
                notifyingPlayer
                    .addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "ThaumicFix Aspect 扫描结果"));
            }

            Collection<Aspect> allAspects = Aspect.aspects.values();
            List<String> goodAspects = new ArrayList<>();
            List<String> badAspects = new ArrayList<>();

            for (Aspect aspect : allAspects) {
                if (aspect == null) {
                    String errorMsg = "发现一个空的 Aspect 实例!";
                    badAspects.add(errorMsg);
                    LOGGER.error(errorMsg);
                    continue;
                }

                try {
                    // 尝试调用一些可能引发崩溃的方法
                    String name = aspect.getName();
                    String tag = aspect.getTag();
                    int color = aspect.getColor();
                    // aspect.getImage() 返回 ResourceLocation，也可能出错
                    String image = aspect.getImage()
                        .toString();

                    String aspectInfo = String
                        .format("OK: %s (Tag: %s, Color: #%06X, Image: %s)", name, tag, color, image);
                    goodAspects.add(aspectInfo);
                    LOGGER.info(aspectInfo);

                } catch (Throwable t) {
                    String errorMsg = String.format("!!! 潜在的崩溃 Aspect !!! Tag: %s", aspect.getTag());
                    badAspects.add(errorMsg);
                    LOGGER.error(errorMsg, t);
                    LOGGER.error(
                        "异常类型: " + t.getClass()
                            .getName() + " - " + t.getMessage());
                }
            }

            if (notifyingPlayer != null) {
                if (badAspects.isEmpty()) {
                    notifyingPlayer
                        .addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "恭喜！未发现任何有问题的 Aspect。"));
                    LOGGER.info("扫描完成，未发现有问题的 Aspect。");
                } else {
                    notifyingPlayer.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.RED + "发现 " + badAspects.size() + " 个可能导致崩溃的 Aspect！"));
                    for (String bad : badAspects) {
                        notifyingPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "- " + bad));
                    }
                    LOGGER.warn("扫描完成，发现 " + badAspects.size() + " 个有问题的 Aspect。请检查上面的日志获取详细堆栈信息。");
                }
                notifyingPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "扫描完成。详细信息请查看日志文件。"));
                notifyingPlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GOLD + "========================================"));
            }

        } catch (Exception e) {
            LOGGER.error("Aspect 扫描线程整体崩溃", e);
            if (notifyingPlayer != null) {
                notifyingPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "扫描失败，请查看日志。"));
            }
        }
    }
}
