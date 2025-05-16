package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderDataManager;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTapBlock extends TraderBlockRotatable implements IFluidTraderBlock, IVariantBlock {

	public static final ResourceLocation DATA = VersionUtil.modResource(LCTech.MODID,"fluid_tap");

	public FluidTapBlock(Properties properties) { super(properties); }
	
	public FluidTapBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	public int getTradeRenderLimit() { return 1; }
	
	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index) { return FluidRenderDataManager.getDataOrEmpty(DATA); }

	@Override
	public int getRenderPositionIndex(BlockState state, int index) { return 0; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) {
		return new FluidTraderBlockEntity(pos, state, 1);
	}

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.FLUID_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_FLUID_TRADER.asTooltip(1); }
	
}
