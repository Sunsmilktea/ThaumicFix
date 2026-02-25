package com.sunmilktea.fix.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.sunmilktea.fix.thaumic.objects.ThreadCrashyAspectScanner;

public class SubCommandScanAspects implements ISubCommand {

    private static long mLastScanTime = 0;

    @Override
    public String getCommandName() {
        return "scanaspects";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>(Arrays.asList("sa", "scan"));
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "scanaspects - 扫描所有注册的Aspect，找出可能导致崩溃的Aspect。";
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
            Thread t = new ThreadCrashyAspectScanner(player);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "开始扫描所有注册的 Aspects..."));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "请勿关闭游戏，完成后会通知你。结果会输出到日志和聊天框。"));
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
