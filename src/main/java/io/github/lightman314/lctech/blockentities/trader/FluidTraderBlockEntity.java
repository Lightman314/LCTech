package io.github.lightman314.lctech.blockentities.trader;

import io.github.lightman314.lctech.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidTraderBlockEntity extends TraderBlockEntity<FluidTraderData> {
	
	protected int tradeCount = 1;
	protected boolean networkTrader = false;
	
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
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(CompoundTag compound)
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
	
	@Override @Deprecated
	protected FluidTraderData createTraderFromOldData(CompoundTag compound) {
		FluidTraderData newTrader = new FluidTraderData(1, this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
	@OnlyIn(Dist.CLIENT)
	public int getTradeRenderLimit()
	{
		if(this.getBlockState().getBlock() instanceof IFluidTraderBlock)
		{
			IFluidTraderBlock b = (IFluidTraderBlock)this.getBlockState().getBlock();
			return b.getTradeRenderLimit();
		}
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public FluidRenderData getRenderPosition(int index)
	{
		if(this.getBlockState().getBlock() instanceof IFluidTraderBlock)
		{
			IFluidTraderBlock b = (IFluidTraderBlock)this.getBlockState().getBlock();
			return b.getRenderPosition(this.getBlockState(), index);
		}
		return null;
	}
	
}