package me.djtheredstoner.perspectivemod.commands;

import me.djtheredstoner.perspectivemod.PerspectiveMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class PerspectiveModCommand extends CommandBase {

    private final String name;

    public PerspectiveModCommand(String name) {
        this.name = name;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name;
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
    public void processCommand(ICommandSender sender, String[] args) {
        // Don't open the GUI here: the chat screen closes itself right after the
        // command runs and would clobber it. Defer to the next client tick.
        PerspectiveMod.instance.requestOpenGui();
    }
}
