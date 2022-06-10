package io.github.lightman314.lctech.core;

import io.github.lightman314.lctech.blockentities.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		FLUID_TANK = ModRegistries.BLOCK_ENTITIES.register("fluid_tank", () -> BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.IRON_TANK.get(), ModBlocks.GOLD_TANK.get(), ModBlocks.DIAMOND_TANK.get()).build(null));
		
		FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("fluid_trader", () -> BlockEntityType.Builder.of(FluidTraderBlockEntity::new, ModBlocks.FLUID_TAP.get(), ModBlocks.FLUID_TAP_BUNDLE.get()).build(null));
		
		ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("energy_trader", () -> BlockEntityType.Builder.of(EnergyTraderBlockEntity::new, ModBlocks.BATTERY_SHOP.get()).build(null));
		
		UNIVERSAL_FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_fluid_trader", () -> BlockEntityType.Builder.of(UniversalFluidTraderBlockEntity::new, ModBlocks.FLUID_SERVER_SML.get(), ModBlocks.FLUID_SERVER_MED.get(), ModBlocks.FLUID_SERVER_LRG.get(), ModBlocks.FLUID_SERVER_XLRG.get()).build(null));
		
		UNIVERSAL_ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_energy_trader", () -> BlockEntityType.Builder.of(UniversalEnergyTraderBlockEntity::new, ModBlocks.ENERGY_SERVER.get()).build(null));
		
		TRADER_INTERFACE_FLUID = ModRegistries.BLOCK_ENTITIES.register("trader_interface_fluid", () -> BlockEntityType.Builder.of(FluidTraderInterfaceBlockEntity::new, ModBlocks.FLUID_TRADER_INTERFACE.get()).build(null));
		
		TRADER_INTERFACE_ENERGY = ModRegistries.BLOCK_ENTITIES.register("trader_interface_energy", () -> BlockEntityType.Builder.of(EnergyTraderInterfaceBlockEntity::new, ModBlocks.ENERGY_TRADER_INTERFACE.get()).build(null));
		
	}
	
	//Fluid Tank
	public static final RegistryObject<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK;
	
	//Fluid Trader
	public static final RegistryObject<BlockEntityType<FluidTraderBlockEntity>> FLUID_TRADER;
	
	//Energy Trader
	public static final RegistryObject<BlockEntityType<EnergyTraderBlockEntity>> ENERGY_TRADER;
	
	//Universal Fluid Trader
	public static final RegistryObject<BlockEntityType<UniversalFluidTraderBlockEntity>> UNIVERSAL_FLUID_TRADER;
	
	//Universal Energy Trader
	public static final RegistryObject<BlockEntityType<UniversalEnergyTraderBlockEntity>> UNIVERSAL_ENERGY_TRADER;
	
	//Fluid Trader Interface
	public static final RegistryObject<BlockEntityType<FluidTraderInterfaceBlockEntity>> TRADER_INTERFACE_FLUID;
	
	//Energy Trader Interface
	public static final RegistryObject<BlockEntityType<EnergyTraderInterfaceBlockEntity>> TRADER_INTERFACE_ENERGY;	
	
}
