package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

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
	
	public FluidTraderData buildNewTrader() {
		FluidTraderData trader = new FluidTraderData(this.tradeCount, this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(@NotNull CompoundTag compound)
	{
		super.load(compound);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@Override
	public AABB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
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
