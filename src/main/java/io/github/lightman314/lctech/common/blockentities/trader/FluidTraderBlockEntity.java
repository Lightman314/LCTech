package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTraderBlockEntity extends TraderBlockEntity<FluidTraderData> {
	
	protected int tradeCount;
	protected boolean networkTrader;
	
	public FluidTraderBlockEntity(BlockPos pos, BlockState state) { this(pos, state, 1, false); }
	public FluidTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount) { this(pos, state, tradeCount, false); }
	public FluidTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount, boolean networkTrader) {
		super(ModBlockEntities.FLUID_TRADER.get(), pos, state);
		this.tradeCount = tradeCount;
		this.networkTrader = networkTrader;
	}

	@Nullable
	@Override
	protected FluidTraderData castOrNullify(@Nonnull TraderData traderData) {
		if(traderData instanceof FluidTraderData ft)
			return ft;
		return null;
	}

	@Nonnull
	public FluidTraderData buildNewTrader() {
		FluidTraderData trader = new FluidTraderData(this.tradeCount, this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.saveAdditional(compound,lookup);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.loadAdditional(compound,lookup);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@OnlyIn(Dist.CLIENT)
	public int getTradeRenderLimit()
	{
		if(this.getBlockState().getBlock() instanceof IFluidTraderBlock b)
		{
			return b.getTradeRenderLimit();
		}
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public FluidRenderData getRenderPosition(int index)
	{
		if(this.getBlockState().getBlock() instanceof IFluidTraderBlock b)
		{
			return b.getRenderPosition(this.getBlockState(), index);
		}
		return null;
	}
	
}
