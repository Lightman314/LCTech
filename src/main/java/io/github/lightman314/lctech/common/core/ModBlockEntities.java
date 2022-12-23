package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.blockentities.old.UniversalEnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.blockentities.old.UniversalFluidTraderBlockEntity;
import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.core.util.TechBlockEntityBlockHelper;
import io.github.lightman314.lightmanscurrency.core.util.BlockEntityBlockHelper;
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
		
		UNIVERSAL_FLUID_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_fluid_trader", () -> BlockEntityType.Builder.of(UniversalFluidTraderBlockEntity::new, ModBlocks.FLUID_NETWORK_TRADER_1.get(), ModBlocks.FLUID_NETWORK_TRADER_2.get(), ModBlocks.FLUID_NETWORK_TRADER_3.get(), ModBlocks.FLUID_NETWORK_TRADER_4.get()).build(null));
		
		UNIVERSAL_ENERGY_TRADER = ModRegistries.BLOCK_ENTITIES.register("universal_energy_trader", () -> BlockEntityType.Builder.of(UniversalEnergyTraderBlockEntity::new, ModBlocks.ENERGY_NETWORK_TRADER.get()).build(null));
		
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
	@Deprecated
	public static final RegistryObject<BlockEntityType<UniversalFluidTraderBlockEntity>> UNIVERSAL_FLUID_TRADER;
	
	//Universal Energy Trader
	@Deprecated
	public static final RegistryObject<BlockEntityType<UniversalEnergyTraderBlockEntity>> UNIVERSAL_ENERGY_TRADER;
	
	//Fluid Trader Interface
	public static final RegistryObject<BlockEntityType<FluidTraderInterfaceBlockEntity>> TRADER_INTERFACE_FLUID;
	
	//Energy Trader Interface
	public static final RegistryObject<BlockEntityType<EnergyTraderInterfaceBlockEntity>> TRADER_INTERFACE_ENERGY;	
	
}
