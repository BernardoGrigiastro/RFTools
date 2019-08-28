package mcjty.rftools.commands;

import mcjty.lib.varia.Logging;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CmdDebugToggle extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "toggle";
    }

    @Override
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(PlayerEntity sender, String[] args) {
        Logging.debugMode = !Logging.debugMode;
        if (Logging.debugMode) {
            ITextComponent component = new StringTextComponent(TextFormatting.YELLOW + "RFTools Debug Mode enabled!");
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        } else {
            ITextComponent component = new StringTextComponent(TextFormatting.YELLOW + "RFTools Debug Mode disabled!");
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        }
    }
}
