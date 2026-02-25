package com.sunmilktea.fix.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandThaumicFix extends CommandBase {

    private final Map<String, ISubCommand> subCommands = new TreeMap<>();

    public CommandThaumicFix() {
        // 在这里注册所有子命令
        registerSubCommand(new SubCommandDumpAspects());
        registerSubCommand(new SubCommandDebugRecipes());
        registerSubCommand(new SubCommandScanAspects());
    }

    private void registerSubCommand(ISubCommand subCommand) {
        subCommands.put(subCommand.getCommandName(), subCommand);
        for (String alias : subCommand.getCommandAliases()) {
            subCommands.put(alias, subCommand);
        }
    }

    @Override
    public String getCommandName() {
        return "thaumicfix";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/thaumicfix <subcommand> [args]";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>(Arrays.asList("tf"));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            displayHelp(sender);
            return;
        }

        String subCommandName = args[0].toLowerCase();
        ISubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if (subCommand.canCommandSenderUseCommand(sender)) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                subCommand.processCommand(sender, subCommandArgs);
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "你没有权限使用此命令。"));
            }
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "未知子命令。使用 /tf help 查看帮助。"));
        }
    }

    private void displayHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "--- ThaumicFix 命令帮助 ---"));
        // 使用 TreeMap 的特性，命令会按名称排序
        for (ISubCommand cmd : new TreeMap<>(subCommands).values()) {
            // 防止重复显示别名
            if (cmd.getCommandName()
                .equals(
                    cmd.getCommandAliases()
                        .get(0))
                || cmd.getCommandName()
                    .equals(
                        cmd.getCommandAliases()
                            .get(0))) {
                sender.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.AQUA + "/tf "
                            + cmd.getCommandName()
                            + " "
                            + (cmd.getCommandAliases()
                                .size() > 1 ? Arrays.toString(
                                    cmd.getCommandAliases()
                                        .toArray())
                                    : "")
                            + EnumChatFormatting.WHITE
                            + " - "
                            + cmd.getCommandUsage(sender)));
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(
                args,
                subCommands.keySet()
                    .toArray(new String[0]));
        } else if (args.length > 1) {
            ISubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 父命令本身不需要权限，由子命令控制
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
