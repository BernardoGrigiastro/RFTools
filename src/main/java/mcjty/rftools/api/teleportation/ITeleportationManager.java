package mcjty.rftools.api.teleportation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("rftools", "getTeleportationManager", "<whatever>.YourClass$GetDimensionManager");
 */
public interface ITeleportationManager {

    /**
     * Return the name of the receiver if there is a valid receiver at the given position and dimension.
     * If not this will return null. Note that a receiver without name will return an empty string ("")
     * and not null.
     */
    String getReceiverName(World world, BlockPos pos);

    /**
     * Create a receiver at a position. Returns false if this fails for whatever reason.
     * If power == -1 the receiver will be powered completely. Otherwise it will get
     * the specified power (capped at the maximum possible power).
     */
    boolean createReceiver(World world, BlockPos pos, String name, int power);

    /**
     * Teleport a player to a dimension at the given spot.
     */
    void teleportPlayer(PlayerEntity player, int dimension, BlockPos location);

    /**
     * Remove all destinations in a dimension.
     */
    void removeReceiverDestinations(World world, int dim);
}
