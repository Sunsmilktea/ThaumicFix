package com.sunmilktea.fix.commands;

import java.util.List;

import net.minecraft.command.ICommandSender;

public interface ISubCommand {

    String getCommandName();

    List<String> getCommandAliases();

    String getCommandUsage(ICommandSender sender);

    void processCommand(ICommandSender sender, String[] args);

    boolean canCommandSenderUseCommand(ICommandSender sender);

    List<String> addTabCompletionOptions(ICommandSender sender, String[] args);
}
