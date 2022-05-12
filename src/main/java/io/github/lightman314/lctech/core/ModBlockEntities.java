package io.github.lightman314.lctech.core;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LCTech.MODID)
public class ModBlockEntities {
	
	public static void init() {
		
		ModRegistries.BLOCK_ENTITIES.register("fluid_tank", () -> BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.IRON_TANK, ModBlocks.GOLD_TANK, ModBlocks.DIAMOND_TANK).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("fluid_trader", () -> BlockEntityType.Builder.of(FluidTraderBlockEntity::new, ModBlocks.FLUID_TAP, ModBlocks.FLUID_TAP_BUNDLE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("energy_trader", () -> BlockEntityType.Builder.of(EnergyTraderBlockEntity::new, ModBlocks.BATTERY_SHOP).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("universal_fluid_trader", () -> BlockEntityType.Builder.of(UniversalFluidTraderBlockEntity::new, ModBlocks.FLUID_SERVER_SML, ModBlocks.FLUID_SERVER_MED, ModBlocks.FLUID_SERVER_LRG, ModBlocks.FLUID_SERVER_XLRG).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("universal_energy_trader", () -> BlockEntityType.Builder.of(UniversalEnergyTraderBlockEntity::new, ModBlocks.ENERGY_SERVER).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("trader_interface_fluid", () -> BlockEntityType.Builder.of(FluidTraderInterfaceBlockEntity::new, ModBlocks.FLUID_TRADER_INTERFACE).build(null));
		
		ModRegistries.BLOCK_ENTITIES.register("trader_interface_energy", () -> BlockEntityType.Builder.of(EnergyTraderInterfaceBlockEntity::new, ModBlocks.ENERGY_TRADER_INTERFACE).build(null));
		
	}
	
	//Fluid Tank
	public static final BlockEntityType<FluidTankBlockEntity> FLUID_TANK = null;
	
	//Fluid Trader
	public static final BlockEntityType<FluidTraderBlockEntity> FLUID_TRADER = null;
	
	//Energy Trader
	public static final BlockEntityType<EnergyTraderBlockEntity> ENERGY_TRADER = null;
	
	//Universal Fluid Trader
	public static final BlockEntityType<UniversalFluidTraderBlockEntity> UNIVERSAL_FLUID_TRADER = null;
	
	//Universal Energy Trader
	public static final BlockEntityType<UniversalEnergyTraderBlockEntity> UNIVERSAL_ENERGY_TRADER = null;
	
	//Fluid Trader Interface
	public static final BlockEntityType<FluidTraderInterfaceBlockEntity> TRADER_INTERFACE_FLUID = null;
	
	//Energy Trader Interface
	public static final BlockEntityType<EnergyTraderInterfaceBlockEntity> TRADER_INTERFACE_ENERGY = null;	
	
}
