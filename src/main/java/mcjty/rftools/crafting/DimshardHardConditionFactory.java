package mcjty.rftools.crafting;

import com.google.gson.JsonObject;
import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

import static mcjty.rftools.config.GeneralConfiguration.CRAFT_HARD;

public class DimshardHardConditionFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> {
            if (RFTools.setup.rftoolsDimensions) {
                return GeneralConfiguration.dimensionalShardRecipeWithDimensions == CRAFT_HARD;
            } else {
                return GeneralConfiguration.dimensionalShardRecipeWithoutDimensions == CRAFT_HARD;
            }
        };
    }
}
