package mcjty.rftools.blocks.monitor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.EnergyTools;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RFMonitorBlockTileEntity extends GenericTileEntity implements ITickableTileEntity {
    // Data that is saved
    private BlockPos monitor;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    public static PropertyInteger LEVEL = PropertyInteger.create("level", 0, 5);

    public static final String CMD_GETADJACENTBLOCKS = "getAdj";
    public static final String CLIENTCMD_ADJACENTBLOCKSREADY = "adjReady";

    // Temporary data
    private int counter = 20;

    private int rflevel = 0;
    private boolean inAlarm = false;

    public RFMonitorMode getAlarmMode() {
        return alarmMode;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public boolean isPowered() {
        return inAlarm;
    }

    public void setAlarm(RFMonitorMode mode, int level) {
        alarmMode = mode;
        alarmLevel = level;
        markDirtyClient();
    }

    public BlockPos getMonitor() {
        return monitor;
    }

    public boolean isValid() {
        return monitor != null;
    }

    public void setInvalid() {
        if (monitor == null) {
            return;
        }
        monitor = null;
        if (rflevel != 0) {
            rflevel = 0;
            markDirtyClient();
        } else {
            markDirty();
        }
        setRedstoneOut(false);
    }

    public void setMonitor(BlockPos c) {
        monitor = c;
        markDirtyClient();
    }

    public int getRflevel() {
        return rflevel;
    }

    public List<BlockPos> findAdjacentBlocks() {
        int x = getPos().getX();
        int y = getPos().getY();
        int z = getPos().getZ();
        List<BlockPos> adjacentBlocks = new ArrayList<>();
        for (int dy = -1 ; dy <= 1 ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < getWorld().getHeight()) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    int zz = z + dz;
                    for (int dx = -1 ; dx <= 1 ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            TileEntity tileEntity = getWorld().getTileEntity(new BlockPos(xx, yy, zz));
                            if (tileEntity != null) {
                                if (EnergyTools.isEnergyTE(tileEntity, null)) {
                                    adjacentBlocks.add(new BlockPos(xx, yy, zz));
                                }
                            }
                        }
                    }
                }
            }
        }
        return adjacentBlocks;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (!isValid()) {
            counter = 1;
            return;
        }

        counter--;
        if (counter > 0) {
            return;
        }
        counter = 20;

        TileEntity tileEntity = getWorld().getTileEntity(monitor);
        if (!EnergyTools.isEnergyTE(tileEntity, null)) {
            setInvalid();
            return;
        }
        EnergyTools.EnergyLevel energy = EnergyTools.getEnergyLevelMulti(tileEntity, null);
        long maxEnergy = energy.getMaxEnergy();
        int ratio = 0;  // Will be set as metadata;
        boolean alarm = false;

        if (maxEnergy > 0) {
            long stored = energy.getEnergy();
            ratio = (int) (1 + (stored * 5) / maxEnergy);
            if (ratio < 1) {
                ratio = 1;
            } else if (ratio > 5) {
                ratio = 5;
            }

            switch (alarmMode) {
                case MODE_OFF:
                    alarm = false;
                    break;
                case MODE_LESS:
                    alarm = ((stored * 100 / maxEnergy) < alarmLevel);
                    break;
                case MODE_MORE:
                    alarm = ((stored * 100 / maxEnergy) > alarmLevel);
                    break;
            }

        }

        if (rflevel != ratio) {
            rflevel = ratio;
            markDirtyClient();
        }
        if (alarm != inAlarm) {
            inAlarm = alarm;
            setRedstoneOut(inAlarm);
            markDirty();
        }
    }

    private void setRedstoneOut(boolean a) {
        getWorld().notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
    }


    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.contains("monitorX")) {
            monitor = new BlockPos(tagCompound.getInt("monitorX"), tagCompound.getInt("monitorY"), tagCompound.getInt("monitorZ"));
        } else {
            monitor = null;
        }
        inAlarm = tagCompound.getBoolean("inAlarm");
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        rflevel = tagCompound.getInt("rflevel");
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        if (monitor != null) {
            tagCompound.putInt("monitorX", monitor.getX());
            tagCompound.putInt("monitorY", monitor.getY());
            tagCompound.putInt("monitorZ", monitor.getZ());
        }
        tagCompound.putBoolean("inAlarm", inAlarm);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.putInt("rflevel", getRflevel());
        tagCompound.setByte("alarmMode", (byte) alarmMode.getIndex());
        tagCompound.setByte("alarmLevel", (byte) alarmLevel);
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETADJACENTBLOCKS.equals(command)) {
            return type.convert(findAdjacentBlocks());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_ADJACENTBLOCKSREADY.equals(command)) {
            GuiRFMonitor.fromServer_clientAdjacentBlocks = new ArrayList<>(Type.create(BlockPos.class).convert(list));
            return true;
        }
        return false;
    }


    @Override
    public BlockState getActualState(BlockState state) {
        int level = getRflevel();
        return state.withProperty(LEVEL, level);
    }

    @Override
    public int getRedstoneOutput(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Direction direction = state.getValue(BaseBlock.FACING);
        if (side == direction) {
            return isPowered() ? 15 : 0;
        }
        return 0;
    }
}
