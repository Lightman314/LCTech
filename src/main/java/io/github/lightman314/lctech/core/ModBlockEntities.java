package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlockEntities {
	
	private static final List<BlockEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();
	
	//Fluid Tank
	public static final BlockEntityType<FluidTankBlockEntity> FLUID_TANK = buildType("fluid_tank", BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.IRON_TANK.block, ModBlocks.GOLD_TANK.block));
	
	//Fluid Trader
	public static final BlockEntityType<FluidTraderBlockEntity> FLUID_TRADER = buildType("fluid_trader", BlockEntityType.Builder.of(FluidTraderBlockEntity::new, ModBlocks.FLUID_TAP.block, ModBlocks.FLUID_TAP_BUNDLE.block));
	
	//Energy Trader
	public static final BlockEntityType<EnergyTraderBlockEntity> ENERGY_TRADER = buildType("energy_trader", BlockEntityType.Builder.of(EnergyTraderBlockEntity::new, ModBlocks.BATTERY_SHOP.block));
	
	//Universal Fluid Trader
	public static final BlockEntityType<UniversalFluidTraderBlockEntity> UNIVERSAL_FLUID_TRADER = buildType("universal_fluid_trader", BlockEntityType.Builder.of(UniversalFluidTraderBlockEntity::new, ModBlocks.FLUID_SERVER_SML.block, ModBlocks.FLUID_SERVER_MED.block, ModBlocks.FLUID_SERVER_LRG.block, ModBlocks.FLUID_SERVER_XLRG.block));
	
	//Universal Energy Trader
	public static final BlockEntityType<UniversalEnergyTraderBlockEntity> UNIVERSAL_ENERGY_TRADER = buildType("universal_energy_trader", BlockEntityType.Builder.of(UniversalEnergyTraderBlockEntity::new, ModBlocks.ENERGY_SERVER.block));
	
	
	private static <T extends BlockEntity> BlockEntityType<T> buildType(String id, BlockEntityType.Builder<T> builder)
	{
		BlockEntityType<T> type = builder.build(null);
		type.setRegistryName(LCTech.MODID, id);
		TILE_ENTITY_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<BlockEntityType<?>> event)
	{
		TILE_ENTITY_TYPES.forEach(type -> event.getRegistry().register(type));
		TILE_ENTITY_TYPES.clear();
	}
	
}
