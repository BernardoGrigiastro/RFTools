package mcjty.rftools.compat;

import com.google.common.base.Function;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsdim.api.dimension.IDimensionManager;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Check if a given dimension is an RFTools dimension
 */
public class RFToolsDimensionChecker {
    public static IDimensionManager dimensionManager;

    public static boolean isRFToolsDimension(World world, int id) {
        if (dimensionManager == null) {
            Logging.logError("Dimension manager cannot be null here! Report to author");
            return false;
        }
        return dimensionManager.isRFToolsDimension(world, id);
    }

    public static class GetDimensionManager implements Function<IDimensionManager, Void> {
        @Nullable
        @Override
        public Void apply(IDimensionManager manager) {
            dimensionManager = manager;
            return null;
        }
    }

}
