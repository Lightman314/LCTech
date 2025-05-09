package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lctech.network.message.fluid_tank.CMessageRequestTankStackSync;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidTankBlockEntity extends EasyBlockEntity implements IServerTicker {

	public static final int DEFAULT_CAPACITY = 10 * FluidType.BUCKET_VOLUME;

	FluidStack tankContents = FluidStack.EMPTY;
	public FluidStack getTankContents() { return this.tankContents; }
	public void setTankContents(@NotNull FluidStack newContents) { this.tankContents = newContents; this.setChanged(); }
	public int getTankCapacity()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof FluidTankBlock tankBlock)
			return tankBlock.getTankCapacity();
		return DEFAULT_CAPACITY;
	}

	public double getTankFillPercent() { return (double)this.tankContents.getAmount()/(double)this.getTankCapacity(); }

	private boolean refactorTankStack = false;

	private TankStackCache stackCache = TankStackCache.DUMMY;
	public final void setTankStack(TankStackCache tankStack) {
		//Remove pending refactorings
		this.refactorTankStack = false;
		TankStackCache oldStack = this.stackCache;
		this.stackCache = tankStack;
		oldStack.refactorExcluded(this.stackCache);
	}
	public final void sendTankStackPacket(Player player) { this.stackCache.getSyncPacket().sendTo(player); }

	public final FluidTankFluidHandler handler = new FluidTankFluidHandler(this);

	public FluidTankBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.FLUID_TANK.get(), pos, state); }
	
	protected FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

	@Nonnull
	public ItemInteractionResult onInteraction(@Nonnull ItemStack heldItem, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if(FluidUtil.getFluidHandler(heldItem).isEmpty())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		
		//Try to fill the tank first
		FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, this.handler, Integer.MAX_VALUE, player, true);
		if(result.isSuccess())
		{
			//If creative, and the item was a bucket, don't move the items around
			if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
				return ItemInteractionResult.SUCCESS;
			if(heldItem.getCount() > 1)
			{
				heldItem.shrink(1);
				player.setItemInHand(hand, heldItem);
				ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
			}
			else
			{
				player.setItemInHand(hand, result.getResult());
			}
		}
		else
		{
			//Now try to empty the tank
			result = FluidUtil.tryFillContainer(heldItem, this.handler, Integer.MAX_VALUE, player, true);
			if(result.isSuccess())
			{
				//If creative, and the item was a bucket, don't move the items around
				if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
					return ItemInteractionResult.SUCCESS;
				if(heldItem.getCount() > 1)
				{
					heldItem.shrink(1);
					player.setItemInHand(hand, heldItem);
					ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
				}
				else
				{
					player.setItemInHand(hand, result.getResult());
				}
			}
		}
		return ItemInteractionResult.SUCCESS;
	}
	
	@Override
	public void setChanged()
	{
		assert this.level != null;
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this);
			super.setChanged();
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
	{
		tag.put("Tank", this.tankContents.saveOptional(lookup));
		super.saveAdditional(tag,lookup);
	}

	@Override
	public void loadAdditional(CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
	{
		if(tag.contains("Tank", Tag.TAG_COMPOUND))
			this.tankContents = FluidStack.parseOptional(lookup,tag.getCompound("Tank"));
		super.loadAdditional(tag,lookup);
	}

	public void loadFromItem(ItemStack stack)
	{
		this.tankContents = FluidTankItem.GetFluid(stack);
		assert this.level != null;
		if(!this.level.isClientSide)
			BlockEntityUtil.sendUpdatePacket(this);
		this.setChanged();
	}

	public FluidRenderData getRenderPosition()
	{
		Block block = this.level.getBlockState(this.worldPosition).getBlock();
		if(block instanceof IFluidTankBlock tankBlock)
			return tankBlock.getRenderData(this.level.getBlockState(this.worldPosition), this.stackCache.isLighterThanAir(), this, this.stackCache.getTankAbove(this));
		return null;
	}
	
	@Override
	public void onLoad() {
		assert this.level != null;
		if(this.level.isClientSide)
		{
			BlockEntityUtil.requestUpdatePacket(this.level, this.worldPosition);
			new CMessageRequestTankStackSync(this.worldPosition).send();
		}
		else if(this.stackCache == TankStackCache.DUMMY)//Refactor tank stack on load to auto-stack existing tanks
			this.enqueTankStackRefactor(); //Force it to wait a tick, otherwise it will incorporate it in a stack before it's contents are loaded from the item stack.
	}

	@Override
	public void serverTick() {
		if(this.refactorTankStack)
		{
			this.refactorTankStack = false;
			this.refactorTankStack();
		}
	}

	public final void enqueTankStackRefactor() { this.refactorTankStack = true; }

	//Fluid Tank Stacking Functions
	public final void refactorTankStack() {

		if(this.level == null || this.level.isClientSide)
			return;

		int bottomY = this.worldPosition.getY();
		int topY = this.worldPosition.getY();
		FluidStack mostRelevantFluid = this.tankContents.copy();

		//Find bottom block
		while(true)
		{
			BlockPos queryPos = this.worldPosition.atY(bottomY - 1);
			BlockEntity be = this.level.getBlockEntity(queryPos);
			if(be instanceof FluidTankBlockEntity tank) {
				if(this.allowInStack(tank, mostRelevantFluid))
				{
					if(mostRelevantFluid.isEmpty())
						mostRelevantFluid = tank.getTankContents().copy();
					bottomY = queryPos.getY();
				}
				else
					break;
			}
			else
				break;
		}

		//Find top block
		while(true)
		{
			BlockPos queryPos = this.worldPosition.atY(topY + 1);
			BlockEntity be = this.level.getBlockEntity(queryPos);
			if(be instanceof FluidTankBlockEntity tank) {
				if(this.allowInStack(tank, mostRelevantFluid))
				{
					if(mostRelevantFluid.isEmpty())
						mostRelevantFluid = tank.getTankContents().copy();
					topY = queryPos.getY();
				}
				else
					break;
			}
			else
				break;
		}

		//Set block states as appropriate
		if(bottomY == topY)
		{
			//Solo stack
			this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(FluidTankBlock.TANK_STATE, TankStackState.SOLO));
			//Set up tank stack cache
			TankStackCache.solo(this).init(true);
		}
		else
		{
			//Build new stack cache
			this.level.setBlockAndUpdate(this.worldPosition.atY(bottomY), this.level.getBlockState(this.worldPosition.atY(bottomY)).setValue(FluidTankBlock.TANK_STATE, TankStackState.BOTTOM));
			this.level.setBlockAndUpdate(this.worldPosition.atY(topY), this.level.getBlockState(this.worldPosition.atY(topY)).setValue(FluidTankBlock.TANK_STATE, TankStackState.TOP));
			for(int y = bottomY + 1; y < topY; ++y)
			{
				BlockPos middlePos = new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ());
				this.level.setBlockAndUpdate(middlePos, this.level.getBlockState(middlePos).setValue(FluidTankBlock.TANK_STATE, TankStackState.MIDDLE));
			}
			//Set up tank stack cache
			TankStackCache.create(this.level, this.worldPosition, bottomY, topY).init(true);
		}
	}

	private boolean allowInStack(FluidTankBlockEntity tank, FluidStack mostRelevantFluid) {
		if(tank.getBlockState().getBlock() != this.getBlockState().getBlock())
			return false;
		FluidStack contents = tank.getTankContents();
		return FluidStack.isSameFluidSameComponents(contents,mostRelevantFluid) || contents.isEmpty() || mostRelevantFluid.isEmpty();
	}

	/**
	 * Returns the list of fluid tanks in the tank stack from bottom to top.
	 */
	public final List<FluidTankBlockEntity> getTankStack() { return this.stackCache.getOrderedTanks(); }
	public final List<FluidTankBlockEntity> getTankStack(FluidStack fluid) { return this.stackCache.getOrderedTanks(fluid); }
	
}
