package com.sunmilktea.fix.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.sunmilktea.fix.thaumic.objects.ThreadAspectScanner;

public class SubCommandDumpAspects implements ISubCommand {

    private static long mLastScanTime = 0;

    @Override
    public String getCommandName() {
        return "dumpaspects";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>(Arrays.asList("da", "dump"));
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dumpaspects - 扫描并导出所有物品的要素信息。";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText("该命令只能由玩家执行。"));
            return;
        }
        EntityPlayer player = (EntityPlayer) sender;
        long now = System.currentTimeMillis();
        long diff = now - mLastScanTime;
        int secondsLeft = (int) ((30_000 - diff) / 1000);

        if (diff >= 30_000) {
            Thread t = new ThreadAspectScanner(player);
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "开始扫描所有物品/方块的 Thaumcraft aspects..."));
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.YELLOW + "请勿关闭游戏，完成后会通知你。输出文件在 config/ThaumicFix/AspectDump.txt"));
            t.start();
            mLastScanTime = now;
        } else {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "上次扫描不足 30 秒，请等待 " + secondsLeft + " 秒。"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(2, getCommandName());
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }
}
