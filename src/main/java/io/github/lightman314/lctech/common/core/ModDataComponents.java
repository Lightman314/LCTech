package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.common.items.data.BatteryData;
import io.github.lightman314.lctech.common.items.data.FluidData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Supplier;

public class ModDataComponents {

    public static void init() {

    }

    static {
        FLUID_DATA = ModRegistries.DATA_COMPONENTS.register("fluid_data", () -> new DataComponentType.Builder<FluidData>().persistent(FluidData.CODEC).networkSynchronized(FluidData.STREAM_CODEC).build());
        ENERGY_DATA = ModRegistries.DATA_COMPONENTS.register("energy_data", () -> new DataComponentType.Builder<BatteryData>().persistent(BatteryData.CODEC).networkSynchronized(BatteryData.STREAM_CODEC).build());
    }
    public static Supplier<DataComponentType<FluidData>> FLUID_DATA;
    public static Supplier<DataComponentType<BatteryData>> ENERGY_DATA;

}
