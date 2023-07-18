package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.common.blockentities.*;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.*;
import io.github.lightman314.lctech.common.blockentities.trader.*;
import io.github.lightman314.lctech.common.core.util.TechBlockEntityBlockHelper;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("deprecation")
public class ModBlockEntities {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		FLUID_TANK = ModRegistries.BLOCK_ENTITIES.register("fluid_tank", () -> BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.IRON_TANK.get(), ModBlocks.GOLD_TANK.get(), ModBlocks.DIAMOND_TANK.get()).build(null));
		
		FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("fluid_trader", () -> BlockEntityType.Builder.of(FluidTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.FLUID_TRADER_TYPE)).build(null));
		
		ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("energy_trader", () -> BlockEntityType.Builder.of(EnergyTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.ENERGY_TRADER_TYPE)).build(null));

		TRADER_INTERFACE_FLUID = ModRegistries.BLOCK_ENTITIES.register("trader_interface_fluid", () -> BlockEntityType.Builder.of(FluidTraderInterfaceBlockEntity::new, ModBlocks.FLUID_TRADER_INTERFACE.get()).build(null));
		
		TRADER_INTERFACE_ENERGY = ModRegistries.BLOCK_ENTITIES.register("trader_interface_energy", () -> BlockEntityType.Builder.of(EnergyTraderInterfaceBlockEntity::new, ModBlocks.ENERGY_TRADER_INTERFACE.get()).build(null));
		
	}
	
	//Fluid Tank
	public static final RegistryObject<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK;
	
	//Fluid Trader
	public static final RegistryObject<BlockEntityType<FluidTraderBlockEntity>> FLUID_TRADER;
	
	//Energy Trader
	public static final RegistryObject<BlockEntityType<EnergyTraderBlockEntity>> ENERGY_TRADER;
	
	//Fluid Trader Interface
	public static final RegistryObject<BlockEntityType<FluidTraderInterfaceBlockEntity>> TRADER_INTERFACE_FLUID;
	
	//Energy Trader Interface
	public static final RegistryObject<BlockEntityType<EnergyTraderInterfaceBlockEntity>> TRADER_INTERFACE_ENERGY;	
	
}