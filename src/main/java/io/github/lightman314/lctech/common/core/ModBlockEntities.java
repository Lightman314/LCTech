package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.common.blockentities.*;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.*;
import io.github.lightman314.lctech.common.blockentities.trader.*;
import io.github.lightman314.lctech.common.core.util.TechBlockEntityBlockHelper;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

@SuppressWarnings("deprecation")
public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		FLUID_TANK = ModRegistries.BLOCK_ENTITIES.register("fluid_tank", () -> TileEntityType.Builder.of(FluidTankBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.FLUID_TANK_TYPE)).build(null));
		
		FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("fluid_trader", () -> TileEntityType.Builder.of(FluidTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.FLUID_TRADER_TYPE)).build(null));
		
		ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("energy_trader", () -> TileEntityType.Builder.of(EnergyTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.ENERGY_TRADER_TYPE)).build(null));
		
		UNIVERSAL_FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_fluid_trader", () -> TileEntityType.Builder.of(FluidTraderBlockEntity::new, ModBlocks.FLUID_NETWORK_TRADER_1.get(), ModBlocks.FLUID_NETWORK_TRADER_2.get(), ModBlocks.FLUID_NETWORK_TRADER_3.get(), ModBlocks.FLUID_NETWORK_TRADER_4.get()).build(null));
		
		UNIVERSAL_ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_energy_trader", () -> TileEntityType.Builder.of(EnergyTraderBlockEntity::new, ModBlocks.ENERGY_NETWORK_TRADER.get()).build(null));
		
		TRADER_INTERFACE_FLUID = ModRegistries.BLOCK_ENTITIES.register("trader_interface_fluid", () -> TileEntityType.Builder.of(FluidTraderInterfaceBlockEntity::new, ModBlocks.FLUID_TRADER_INTERFACE.get()).build(null));
		
		TRADER_INTERFACE_ENERGY = ModRegistries.BLOCK_ENTITIES.register("trader_interface_energy", () -> TileEntityType.Builder.of(EnergyTraderInterfaceBlockEntity::new, ModBlocks.ENERGY_TRADER_INTERFACE.get()).build(null));
		
	}
	
	//Fluid Tank
	public static final RegistryObject<TileEntityType<FluidTankBlockEntity>> FLUID_TANK;
	
	//Fluid Trader
	public static final RegistryObject<TileEntityType<FluidTraderBlockEntity>> FLUID_TRADER;

	//Energy Trader
	public static final RegistryObject<TileEntityType<EnergyTraderBlockEntity>> ENERGY_TRADER;
	
	//Universal Fluid Trader
	@Deprecated
	public static final RegistryObject<TileEntityType<FluidTraderBlockEntity>> UNIVERSAL_FLUID_TRADER;
	
	//Universal Energy Trader
	@Deprecated
	public static final RegistryObject<TileEntityType<EnergyTraderBlockEntity>> UNIVERSAL_ENERGY_TRADER;
	
	//Fluid Trader Interface
	public static final RegistryObject<TileEntityType<FluidTraderInterfaceBlockEntity>> TRADER_INTERFACE_FLUID;
	
	//Energy Trader Interface
	public static final RegistryObject<TileEntityType<EnergyTraderInterfaceBlockEntity>> TRADER_INTERFACE_ENERGY;
	
}