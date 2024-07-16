package io.github.lightman314.lctech.common.crafting.condition;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.SimpleCraftingCondition;

public class TechCraftingConditions {

	public static void init() {}

	public static class FluidTrader extends SimpleCraftingCondition {
		public static final FluidTrader INSTANCE = new FluidTrader();
		private static final MapCodec<FluidTrader> CODEC = MapCodec.unit(() -> INSTANCE);
		private FluidTrader() { super(() -> CODEC,TechConfig.COMMON.canCraftFluidTraders); }
	}
	
	public static class FluidTank extends SimpleCraftingCondition {
		public static final FluidTank INSTANCE = new FluidTank();
		private static final MapCodec<FluidTank> CODEC = MapCodec.unit(() -> INSTANCE);
		private FluidTank() { super(() -> CODEC, TechConfig.COMMON.canCraftFluidTanks); }
	}

	public static class VoidTank extends SimpleCraftingCondition {
		public static final VoidTank INSTANCE = new VoidTank();
		private static final MapCodec<VoidTank> CODEC = MapCodec.unit(() -> INSTANCE);
		private VoidTank() { super(() -> CODEC, TechConfig.COMMON.canCraftVoidTanks); }
	}
	
	public static class EnergyTrader extends SimpleCraftingCondition {
		public static final EnergyTrader INSTANCE = new EnergyTrader();
		private static final MapCodec<EnergyTrader> CODEC = MapCodec.unit(() -> INSTANCE);
		private EnergyTrader() { super(() -> CODEC, TechConfig.COMMON.canCraftEnergyTraders); }
	}
	
	public static class Batteries extends SimpleCraftingCondition {
		public static final Batteries INSTANCE = new Batteries();
		private static final MapCodec<Batteries> CODEC = MapCodec.unit(() -> INSTANCE);
		private Batteries() { super(() -> CODEC, TechConfig.COMMON.canCraftBatteries); }
	}

	static {
		ModRegistries.CRAFTING_CONDITIONS.register("fluid_trader_craftable", () -> FluidTrader.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("fluid_tank_craftable", () -> FluidTank.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("void_tank_craftable", () -> VoidTank.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("energy_trader_craftable", () -> EnergyTrader.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("batteries_craftable", () -> Batteries.CODEC);
	}
	
}
