package mcjty.rftools.commands;

import mcjty.lib.font.FontLoader;
import mcjty.lib.font.TrueTypeFont;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.setup.ClientProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CmdFont extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<name>,<size>";
    }

    @Override
    public String getCommand() {
        return "font";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(PlayerEntity sender, String[] args) {
        if (args.length < 3) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Several parameters are missing!");
            if (sender != null) {
                sender.sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        } else if (args.length > 3) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Too many parameters!");
            if (sender != null) {
                sender.sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        // @todo, fix the 1.13 way
//        ScreenConfiguration.font = fetchString(sender, args, 1, "rftools:fonts/ubuntu.ttf");
//        ScreenConfiguration.fontSize = fetchFloat(sender, args, 2, 40);
        TrueTypeFont font = FontLoader.createFont(new ResourceLocation(ScreenConfiguration.font.get()),
                (float) (double) ScreenConfiguration.fontSize.get(), false);
        if (font == null) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Could not load font!");
            if (sender != null) {
                sender.sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }
        ClientProxy.font = font;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
