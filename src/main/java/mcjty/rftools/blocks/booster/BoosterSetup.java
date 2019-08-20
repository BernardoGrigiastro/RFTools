package mcjty.rftools.blocks.booster;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;



public class BoosterSetup {
    public static BaseBlock<BoosterTileEntity, GenericContainer> boosterBlock;

    public static void init() {
        boosterBlock = ModBlocks.builderFactory.<BoosterTileEntity> builder("booster")
                .tileEntityClass(BoosterTileEntity.class)
                .container(BoosterTileEntity.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.NONE)
                .flags(BlockFlags.REDSTONE_CHECK)
                .guiId(GuiProxy.GUI_BOOSTER)
                .infusable()
                .moduleSupport(BoosterTileEntity.MODULE_SUPPORT)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.booster")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        boosterBlock.initModel();
        boosterBlock.setGuiFactory(GuiBooster::new);
    }
}
