package io.github.lightman314.lctech.common.blocks.traderinterface;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blocks.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTraderInterfaceBlock extends TraderInterfaceBlock {

	public FluidTraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTraderInterfaceBlockEntity(pos, state);
	}
	
	@Override
	protected BlockEntityType<?> interfaceType() { return ModBlockEntities.TRADER_INTERFACE_FLUID.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_FLUID_TRADER_INTERFACE.asTooltip(); }
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity blockEntity) { }
	
}
