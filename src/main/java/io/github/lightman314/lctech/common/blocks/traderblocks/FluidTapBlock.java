package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTapBlock extends TraderBlockRotatable implements IFluidTraderBlock {

	public static final FluidRenderData FLUID_RENDER = FluidRenderData.CreateFluidRender(4.01f, 0.01f, 4.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	
	public FluidTapBlock(Properties properties) { super(properties); }
	
	public FluidTapBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	public int getTradeRenderLimit() { return 1; }
	
	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index){ return FLUID_RENDER; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) {
		return new FluidTraderBlockEntity(pos, state, 1);
	}

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.FLUID_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_FLUID_TRADER.asTooltip(1); }
	
}
