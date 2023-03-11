package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.message.fluid_tank.CMessageRequestTankStackSync;
import io.github.lightman314.lightmanscurrency.common.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class FluidTankBlockEntity extends EasyBlockEntity implements ITickableTileEntity {

	public static final int DEFAULT_CAPACITY = 10 * FluidAttributes.BUCKET_VOLUME;

	FluidStack tankContents = FluidStack.EMPTY;
	public FluidStack getTankContents() { return this.tankContents; }
	public void setTankContents(@Nonnull FluidStack newContents) { this.tankContents = newContents; this.setChanged(); }
	public int getTankCapacity()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof FluidTankBlock)
			return ((FluidTankBlock)block).getTankCapacity();
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
	public final void sendTankStackPacket(ServerPlayerEntity player) { LCTechPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> player), this.stackCache.getSyncPacket()); }

	public final FluidTankFluidHandler handler = new FluidTankFluidHandler(this);
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> this.handler);

	public FluidTankBlockEntity() { this(ModBlockEntities.FLUID_TANK.get()); }

	protected FluidTankBlockEntity(TileEntityType<?> type) { super(type); }

	public ActionResultType onInteraction(PlayerEntity player, Hand hand)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		if(!FluidUtil.getFluidHandler(heldItem).isPresent())
			return ActionResultType.PASS;

		//Try to fill the tank first
		FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, this.handler, Integer.MAX_VALUE, player, true);
		if(result.isSuccess())
		{
			//If creative, and the item was a bucket, don't move the items around
			if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
				return ActionResultType.SUCCESS;
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
					return ActionResultType.SUCCESS;
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
		return ActionResultType.SUCCESS;
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
	@Nonnull
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);

		compound.put("Tank", this.tankContents.writeToNBT(new CompoundNBT()));

		return compound;

	}

	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);

		if(compound.contains("Tank", Constants.NBT.TAG_COMPOUND))
			this.tankContents = FluidStack.loadFluidStackFromNBT(compound.getCompound("Tank"));
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
		if(block instanceof IFluidTankBlock)
			return ((IFluidTankBlock)block).getRenderData(this.level.getBlockState(this.worldPosition), this.stackCache.isLighterThanAir(), this, this.stackCache.getTankAbove(this));
		return null;
	}

	@Override
	public void onLoad() {
		assert this.level != null;
		if(this.level.isClientSide)
		{
			BlockEntityUtil.requestUpdatePacket(this.level, this.worldPosition);
			LCTechPacketHandler.instance.sendToServer(new CMessageRequestTankStackSync(this.worldPosition));
		}
		else if(this.stackCache == TankStackCache.DUMMY)//Refactor tank stack on load to auto-stack existing tanks
			this.enqueTankStackRefactor(); //Force it to wait a tick, otherwise it will incorporate it in a stack before it's contents are loaded from the item stack.
	}

	@Override
	public void tick() {
		if(this.isServer() && this.refactorTankStack)
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
			BlockPos queryPos = this.atY(bottomY);
			TileEntity be = this.level.getBlockEntity(queryPos);
			if(be instanceof FluidTankBlockEntity) {
				FluidTankBlockEntity tank = (FluidTankBlockEntity)be;
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
			BlockPos queryPos = this.atY(topY);
			TileEntity be = this.level.getBlockEntity(queryPos);
			if(be instanceof FluidTankBlockEntity) {
				FluidTankBlockEntity tank = (FluidTankBlockEntity)be;
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
			this.level.setBlockAndUpdate(atY(bottomY), this.level.getBlockState(this.atY(bottomY)).setValue(FluidTankBlock.TANK_STATE, TankStackState.BOTTOM));
			this.level.setBlockAndUpdate(this.atY(topY), this.level.getBlockState(this.atY(topY)).setValue(FluidTankBlock.TANK_STATE, TankStackState.TOP));
			for(int y = bottomY + 1; y < topY; ++y)
			{
				BlockPos middlePos = new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ());
				this.level.setBlockAndUpdate(middlePos, this.level.getBlockState(middlePos).setValue(FluidTankBlock.TANK_STATE, TankStackState.MIDDLE));
			}
			//Set up tank stack cache
			TankStackCache.create(this.level, this.worldPosition, bottomY, topY).init(true);
		}
	}

	private BlockPos atY(int y) { return atY(this.worldPosition, y); }

	private static BlockPos atY(BlockPos pos, int y) { return new BlockPos(pos.getX(), y, pos.getZ()); }

	private boolean allowInStack(FluidTankBlockEntity tank, FluidStack mostRelevantFluid) {
		if(tank.getBlockState().getBlock() != this.getBlockState().getBlock())
			return false;
		FluidStack contents = tank.getTankContents();
		return contents.isFluidEqual(mostRelevantFluid) || contents.isEmpty() || mostRelevantFluid.isEmpty();
	}

	/**
	 * Returns the list of fluid tanks in the tank stack from bottom to top.
	 */
	public final List<FluidTankBlockEntity> getTankStack() { return this.stackCache.getOrderedTanks(); }

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		//Return the fluid handler capability
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, this.holder);

		//Otherwise return none
		return super.getCapability(cap, side);
	}

}