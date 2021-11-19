package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.tileentities.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {
	
	private static final List<TileEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();
	
	//Fluid Tank
	public static final TileEntityType<FluidTankTileEntity> FLUID_TANK = buildType("fluid_tank", TileEntityType.Builder.create(FluidTankTileEntity::new, ModBlocks.IRON_TANK.block, ModBlocks.GOLD_TANK.block));
	
	//Fluid Trader
	public static final TileEntityType<FluidTraderTileEntity> FLUID_TRADER = buildType("fluid_trader", TileEntityType.Builder.create(FluidTraderTileEntity::new, ModBlocks.FLUID_TAP.block, ModBlocks.FLUID_TAP_BUNDLE.block));
	
	//Universal Fluid Trader
	public static final TileEntityType<UniversalFluidTraderTileEntity> UNIVERSAL_FLUID_TRADER = buildType("universal_fluid_trader", TileEntityType.Builder.create(UniversalFluidTraderTileEntity::new, ModBlocks.FLUID_SERVER_SML.block));
	
	private static <T extends TileEntity> TileEntityType<T> buildType(String id, TileEntityType.Builder<T> builder)
	{
		TileEntityType<T> type = builder.build(null);
		type.setRegistryName(LCTech.MODID, id);
		TILE_ENTITY_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<TileEntityType<?>> event)
	{
		TILE_ENTITY_TYPES.forEach(type -> event.getRegistry().register(type));
		TILE_ENTITY_TYPES.clear();
	}
	
}
