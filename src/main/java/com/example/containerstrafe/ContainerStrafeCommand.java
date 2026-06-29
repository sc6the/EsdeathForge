package com.example.containerstrafe;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class ContainerStrafeCommand extends CommandBase {

    private static final List<String> SETTINGS = Arrays.asList(
            "enabled", "creativesearch", "left", "right", "backward", "forward"
    );
    private static final List<String> SUBS = Arrays.asList(
            "enable", "disable", "toggle", "set", "list", "help"
    );

    @Override
    public String getCommandName() {
        return "containerstrafe";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("cstrafe", "cs");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/containerstrafe <enable|disable|toggle|set <setting> <true|false>|list|help>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, SUBS);
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) return getListOfStringsMatchingLastWord(args, SETTINGS);
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) return getListOfStringsMatchingLastWord(args, Arrays.asList("true", "false"));
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ContainerStrafeForge mod = ContainerStrafeForge.INSTANCE;
        if (mod == null) { msg(sender, EnumChatFormatting.RED + "Mod not initialized"); return; }

        if (args.length == 0) { printList(sender, mod); return; }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "enable":
                mod.enabled = true;
                mod.saveConfig();
                msg(sender, EnumChatFormatting.GREEN + "Container Strafe enabled");
                break;
            case "disable":
                mod.enabled = false;
                mod.saveConfig();
                msg(sender, EnumChatFormatting.RED + "Container Strafe disabled");
                break;
            case "toggle":
                mod.enabled = !mod.enabled;
                mod.saveConfig();
                msg(sender, "Container Strafe " + (mod.enabled ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled"));
                break;
            case "list":
                printList(sender, mod);
                break;
            case "set":
                if (args.length < 3) { msg(sender, EnumChatFormatting.RED + "Usage: /containerstrafe set <setting> <true|false>"); return; }
                handleSet(sender, mod, args[1].toLowerCase(), args[2]);
                break;
            case "help":
            default:
                msg(sender, EnumChatFormatting.YELLOW + getCommandUsage(sender));
                msg(sender, EnumChatFormatting.GRAY + "Settings: " + String.join(", ", SETTINGS));
                break;
        }
    }

    private void handleSet(ICommandSender sender, ContainerStrafeForge mod, String setting, String valueStr) {
        boolean value;
        if (valueStr.equalsIgnoreCase("true") || valueStr.equals("1") || valueStr.equalsIgnoreCase("on")) value = true;
        else if (valueStr.equalsIgnoreCase("false") || valueStr.equals("0") || valueStr.equalsIgnoreCase("off")) value = false;
        else { msg(sender, EnumChatFormatting.RED + "Value must be true or false"); return; }

        switch (setting) {
            case "enabled": mod.enabled = value; break;
            case "creativesearch": mod.disableInCreativeInventorySearch = value; break;
            case "left": mod.left = value; break;
            case "right": mod.right = value; break;
            case "backward": mod.backward = value; break;
            case "forward": mod.forward = value; break;
            default:
                msg(sender, EnumChatFormatting.RED + "Unknown setting: " + setting);
                return;
        }
        mod.saveConfig();
        msg(sender, EnumChatFormatting.GREEN + setting + " = " + value);
    }

    private void printList(ICommandSender sender, ContainerStrafeForge mod) {
        msg(sender, EnumChatFormatting.GOLD + "--- Container Strafe ---");
        line(sender, "enabled", mod.enabled);
        line(sender, "creativesearch", mod.disableInCreativeInventorySearch);
        line(sender, "left", mod.left);
        line(sender, "right", mod.right);
        line(sender, "backward", mod.backward);
        line(sender, "forward", mod.forward);
    }

    private void line(ICommandSender sender, String name, boolean value) {
        msg(sender, EnumChatFormatting.GRAY + name + ": " + (value ? EnumChatFormatting.GREEN + "true" : EnumChatFormatting.RED + "false"));
    }

    private void msg(ICommandSender sender, String text) {
        if (sender != null) sender.addChatMessage(new ChatComponentText(text));
        else Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text));
    }
}
