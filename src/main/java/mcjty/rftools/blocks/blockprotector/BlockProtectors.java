package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class BlockProtectors extends AbstractWorldData<BlockProtectors> {

    public static final String PROTECTORS_NAME = "RFToolsBlockProtectors";

    private final Map<Integer,GlobalCoordinate> protectorById = new HashMap<>();
    private final Map<GlobalCoordinate,Integer> protectorIdByCoordinate = new HashMap<>();
    private int lastId = 0;

    public BlockProtectors(String name) {
        super(name);
    }

    @Override
    public void clear() {
        protectorById.clear();
        protectorIdByCoordinate.clear();
        lastId = 0;
    }

    public static Collection<GlobalCoordinate> getProtectors(World world, int x, int y, int z) {
        if (world.isRemote) {
            return Collections.emptyList();
        }
        BlockProtectors blockProtectors = getProtectors(world);
        return blockProtectors.findProtectors(x, y, z, world.provider.getDimension(), 2);
    }

    public static boolean checkHarvestProtection(int x, int y, int z, IBlockReader world, Collection<GlobalCoordinate> protectors) {
        for (GlobalCoordinate protector : protectors) {
            TileEntity te = world.getTileEntity(protector.getCoordinate());
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                BlockPos relative = blockProtectorTileEntity.absoluteToRelative(x, y, z);
                boolean b = blockProtectorTileEntity.isProtected(relative);
                if (b) {
                    if (blockProtectorTileEntity.attemptHarvestProtection()) {
                        return true;
                    } else {
                        blockProtectorTileEntity.removeProtection(relative);
                    }
                }
            }
        }
        return false;
    }


    public static BlockProtectors getProtectors(World world) {
        return AbstractWorldData.getData(world, BlockProtectors.class, PROTECTORS_NAME);
    }

    // Set an old id to a new position (after moving a receiver).
    public void assignId(GlobalCoordinate key, int id) {
        protectorById.put(id, key);
        protectorIdByCoordinate.put(key, id);
    }

    public int getNewId(GlobalCoordinate key) {
        if (protectorIdByCoordinate.containsKey(key)) {
            return protectorIdByCoordinate.get(key);
        }
        lastId++;
        protectorById.put(lastId, key);
        protectorIdByCoordinate.put(key, lastId);
        return lastId;
    }

    // Get the id from a coordinate.
    public Integer getIdForCoordinate(GlobalCoordinate key) {
        return protectorIdByCoordinate.get(key);
    }

    public GlobalCoordinate getCoordinateForId(int id) {
        return protectorById.get(id);
    }

    public void removeDestination(BlockPos coordinate, int dimension) {
        GlobalCoordinate key = new GlobalCoordinate(coordinate, dimension);
        Integer id = protectorIdByCoordinate.get(key);
        if (id != null) {
            protectorById.remove(id);
            protectorIdByCoordinate.remove(key);
        }
    }

    public Collection<GlobalCoordinate> findProtectors(int x, int y, int z, int dimension, int radius) {
        List<GlobalCoordinate> protectors = new ArrayList<>();
        for (GlobalCoordinate coordinate : protectorIdByCoordinate.keySet()) {
            if (coordinate.getDimension() == dimension) {
                BlockPos c = coordinate.getCoordinate();
                if (Math.abs(x-c.getX()) <= (16+radius+1) && Math.abs(y-c.getY()) <= (16+radius+1) && Math.abs(z-c.getZ()) <= (16+radius+1)) {
                    protectors.add(coordinate);
                }
            }
        }

        return protectors;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        protectorById.clear();
        protectorIdByCoordinate.clear();
        lastId = tagCompound.getInteger("lastId");
        readDestinationsFromNBT(tagCompound);
    }

    private void readDestinationsFromNBT(CompoundNBT tagCompound) {
        ListNBT lst = tagCompound.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            CompoundNBT tc = lst.getCompoundTagAt(i);
            BlockPos c = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            int dim = tc.getInteger("dim");

            GlobalCoordinate gc = new GlobalCoordinate(c, dim);
            int id = tc.getInteger("id");
            protectorById.put(id, gc);
            protectorIdByCoordinate.put(gc, id);
        }
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (GlobalCoordinate destination : protectorIdByCoordinate.keySet()) {
            CompoundNBT tc = new CompoundNBT();
            BlockPos c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            Integer id = protectorIdByCoordinate.get(new GlobalCoordinate(c, destination.getDimension()));
            tc.setInteger("id", id);
            lst.appendTag(tc);
        }
        tagCompound.setTag("blocks", lst);
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }

}
