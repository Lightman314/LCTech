package io.github.lightman314.lctech.blockentities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.blocks.FluidTankBlock;
import io.github.lightman314.lctech.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FluidTankBlockEntity extends BlockEntity implements IFluidHandler{

	public static final int DEFAULT_CAPACITY = 10 * FluidType.BUCKET_VOLUME;
	
	FluidStack tankContents = FluidStack.EMPTY;
	public FluidStack getTankContents() { return this.tankContents; }
	public int getTankCapacity()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof FluidTankBlock)
			return ((FluidTankBlock)block).getTankCapacity();
		return DEFAULT_CAPACITY;
	}
	public int getTankSpace() { return this.getTankCapacity() - this.tankContents.getAmount(); }
	public double getTankFillPercent() { return (double)this.tankContents.getAmount()/(double)this.getTankCapacity(); }
	
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> this);
	
	public FluidTankBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.FLUID_TANK.get(), pos, state); }
	
	protected FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
	
	public InteractionResult onInteraction(Player player, InteractionHand hand)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		if(!FluidUtil.getFluidHandler(heldItem).isPresent())
			return InteractionResult.PASS;
		
		//Try to fill the tank first
		FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, this, Integer.MAX_VALUE, player, true);
		if(result.isSuccess())
		{
			this.setChanged();
			//If creative, and the item was a bucket, don't move the items around
			if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
				return InteractionResult.SUCCESS;
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
			result = FluidUtil.tryFillContainer(heldItem, this, Integer.MAX_VALUE, player, true);
			if(result.isSuccess())
			{
				this.setChanged();
				//If creative, and the item was a bucket, don't move the items around
				if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
					return InteractionResult.SUCCESS;
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
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setChanged()
	{
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this);
			super.setChanged();
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		
		compound.put("Tank", this.tankContents.writeToNBT(new CompoundTag()));
		
		super.saveAdditional(compound);
		
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		
		if(compound.contains("Tank", Tag.TAG_COMPOUND))
			this.tankContents = FluidStack.loadFluidStackFromNBT(compound.getCompound("Tank"));
		
		super.load(compound);
	}
	
	public void loadFromItem(ItemStack stack)
	{
		this.tankContents = FluidTankItem.GetFluid(stack);
		if(!this.level.isClientSide)
			BlockEntityUtil.sendUpdatePacket(this);
		this.setChanged();
	}
	
	public FluidRenderData getRenderPosition()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IFluidTankBlock)
		{
			return ((IFluidTankBlock)block).getRenderData();
		}
		return FluidTankBlock.RENDER_DATA;
	}
	
	@Override
	public CompoundTag getUpdateTag() {return this.saveWithFullMetadata(); }
	
	@Override
	public void onLoad() {
		if(this.level.isClientSide)
			BlockEntityUtil.requestUpdatePacket(this.level, this.worldPosition);
	}
	
	//IFluidHandler Functions
	@Override
	public int getTanks() {
		return 1;
	}
	
	@Override
	public FluidStack getFluidInTank(int tank) {
		return tank == 0 ? this.tankContents.copy() : FluidStack.EMPTY;
	}
	
	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? this.getTankCapacity() : 0;
	}
	
	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return tank == 0 ? this.tankContents.isEmpty() || this.tankContents.isFluidEqual(stack) : false;
	}
	
	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if(isFluidValid(0, resource))
		{
			int fillAmount = MathUtil.clamp(resource.getAmount(), 0, this.getTankSpace());
			if(action.execute())
			{
				if(this.tankContents.isEmpty())
					this.tankContents = resource.copy();
				else
					this.tankContents.grow(fillAmount);
				this.setChanged();
			}
			return fillAmount;
		}
		return 0;
	}
	
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if(this.tankContents.isEmpty() || !this.tankContents.isFluidEqual(resource))
			return FluidStack.EMPTY;
		//Tank is not empty, and the resource is equal to the tank contents
		int drainAmount = MathUtil.clamp(resource.getAmount(), 0, this.tankContents.getAmount());
		FluidStack resultStack = this.tankContents.copy();
		resultStack.setAmount(drainAmount);
		if(action.execute())
		{
			//Drain the tank
			this.tankContents.shrink(drainAmount);
			if(this.tankContents.isEmpty())
				this.tankContents = FluidStack.EMPTY;
			this.setChanged();
		}
		return resultStack;
	}
	
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if(this.tankContents.isEmpty())
			return FluidStack.EMPTY;
		FluidStack drainStack = this.tankContents.copy();
		drainStack.setAmount(maxDrain);
		return drain(drainStack, action);
	}
	
	@Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
		//Return the fluid handler capability
		if(cap == ForgeCapabilities.FLUID_HANDLER)
			return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, this.holder);
		
		//Otherwise return none
		return super.getCapability(cap, side);
    }
	
}
