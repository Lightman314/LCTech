package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.tileentities.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {
	
	private static final List<BlockEntityType<?>> TILE_ENTITY_TYPES = new ArrayList<>();
	
	//Fluid Tank
	public static final BlockEntityType<FluidTankTileEntity> FLUID_TANK = buildType("fluid_tank", BlockEntityType.Builder.of(FluidTankTileEntity::new, ModBlocks.IRON_TANK.block, ModBlocks.GOLD_TANK.block));
	
	//Fluid Trader
	public static final BlockEntityType<FluidTraderTileEntity> FLUID_TRADER = buildType("fluid_trader", BlockEntityType.Builder.of(FluidTraderTileEntity::new, ModBlocks.FLUID_TAP.block, ModBlocks.FLUID_TAP_BUNDLE.block));
	
	//Universal Fluid Trader
	public static final BlockEntityType<UniversalFluidTraderTileEntity> UNIVERSAL_FLUID_TRADER = buildType("universal_fluid_trader", BlockEntityType.Builder.of(UniversalFluidTraderTileEntity::new, ModBlocks.FLUID_SERVER_SML.block));
	
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
