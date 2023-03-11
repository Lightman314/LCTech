package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTraderBlockEntity extends TraderBlockEntity<FluidTraderData> {
	
	protected int tradeCount;
	protected boolean networkTrader;
	
	public FluidTraderBlockEntity() { this(1, false); }
	public FluidTraderBlockEntity(int tradeCount) { this(tradeCount, false); }
	public FluidTraderBlockEntity(int tradeCount, boolean networkTrader) {
		super(ModBlockEntities.FLUID_TRADER.get());
		this.tradeCount = tradeCount;
		this.networkTrader = networkTrader;
	}
	
	public FluidTraderData buildNewTrader() {
		FluidTraderData trader = new FluidTraderData(this.tradeCount, this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
		return compound;
	}
	
	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
	}
	
	@Override @Deprecated
	protected FluidTraderData createTraderFromOldData(CompoundNBT compound) {
		FluidTraderData newTrader = new FluidTraderData(1, this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}

	@Override
	protected void loadAsFormerNetworkTrader(@Nullable FluidTraderData fluidTraderData, CompoundNBT compoundNBT) {
		this.tradeCount = fluidTraderData.getTradeCount();
		this.networkTrader = true;
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