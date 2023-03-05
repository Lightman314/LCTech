package io.github.lightman314.lctech.common.crafting.condition;

import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.SimpleCraftingCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class TechCraftingConditions {

	public static class FluidTrader extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_trader_craftable");
		private static final FluidTrader INSTANCE = new FluidTrader();
		public static final IConditionSerializer<FluidTrader> SERIALIZER = new Serializer();
		private FluidTrader() { super(TYPE, TechConfig.COMMON.canCraftFluidTraders); }
		private static class Serializer implements IConditionSerializer<FluidTrader> {
			@Override
			public void write(JsonObject json, FluidTrader value) {}
			@Override
			public FluidTrader read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}
	
	public static class FluidTank extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_tank_craftable");
		private static final FluidTank INSTANCE = new FluidTank();
		public static final IConditionSerializer<FluidTank> SERIALIZER = new Serializer();
		private FluidTank() { super(TYPE, TechConfig.COMMON.canCraftFluidTanks); }
		private static class Serializer implements IConditionSerializer<FluidTank> {
			@Override
			public void write(JsonObject json, FluidTank value) {}
			@Override
			public FluidTank read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}
	
	public static class EnergyTrader extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_trader_craftable");
		private static final EnergyTrader INSTANCE = new EnergyTrader();
		public static final IConditionSerializer<EnergyTrader> SERIALIZER = new Serializer();
		private EnergyTrader() { super(TYPE, TechConfig.COMMON.canCraftEnergyTraders); }
		private static class Serializer implements IConditionSerializer<EnergyTrader> {
			@Override
			public void write(JsonObject json, EnergyTrader value) {}
			@Override
			public EnergyTrader read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}
	
	public static class Batteries extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "batteries_craftable");
		private static final Batteries INSTANCE = new Batteries();
		public static final IConditionSerializer<Batteries> SERIALIZER = new Serializer();
		private Batteries() { super(TYPE, TechConfig.COMMON.canCraftBatteries); }
		private static class Serializer implements IConditionSerializer<Batteries> {
			@Override
			public void write(JsonObject json, Batteries value) {}
			@Override
			public Batteries read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}
	
}