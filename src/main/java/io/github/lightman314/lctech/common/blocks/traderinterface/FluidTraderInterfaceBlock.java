package io.github.lightman314.lctech.common.blocks.traderinterface;

import java.util.List;

import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates.TraderInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;

public class FluidTraderInterfaceBlock extends TraderInterfaceBlock {

	public FluidTraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTraderInterfaceBlockEntity(pos, state);
	}
	
	@Override
	protected BlockEntityType<?> interfaceType() { 
		return ModBlockEntities.TRADER_INTERFACE_FLUID.get();
	}
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return TechTooltips.FLUID_TRADER_INTERFACE; }
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity blockEntity) { }
	
}
