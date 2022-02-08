package io.github.lightman314.lctech.tileentities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.blocks.FluidTankBlock;
import io.github.lightman314.lctech.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FluidTankTileEntity extends TileEntity implements IFluidHandler{

	public static final int DEFAULT_CAPACITY = 10 * FluidAttributes.BUCKET_VOLUME;
	
	FluidStack tankContents = FluidStack.EMPTY;
	public FluidStack getTankContents() { return this.tankContents; }
	public int getTankCapacity()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof FluidTankBlock)
			return ((FluidTankBlock)block).tankCapacity;
		return DEFAULT_CAPACITY;
	}
	public int getTankSpace() { return this.getTankCapacity() - this.tankContents.getAmount(); }
	public double getTankFillPercent() { return (double)this.tankContents.getAmount()/(double)this.getTankCapacity(); }
	
	private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> this);
	
	public FluidTankTileEntity() { this(ModTileEntities.FLUID_TANK); }
	
	protected FluidTankTileEntity(TileEntityType<?> type) { super(type); }
	
	public ActionResultType onInteraction(PlayerEntity player, Hand hand)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		if(!FluidUtil.getFluidHandler(heldItem).isPresent())
			return ActionResultType.PASS;
		
		//Try to fill the tank first
		FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, this, Integer.MAX_VALUE, player, true);
		if(result.isSuccess())
		{
			this.markDirty();
			//If creative, and the item was a bucket, don't move the items around
			if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
				return ActionResultType.SUCCESS;
			if(heldItem.getCount() > 1)
			{
				heldItem.shrink(1);
				player.setHeldItem(hand, heldItem);
				ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
			}
			else
			{
				player.setHeldItem(hand, result.getResult());
			}
		}
		else
		{
			//Now try to empty the tank
			result = FluidUtil.tryFillContainer(heldItem, this, Integer.MAX_VALUE, player, true);
			if(result.isSuccess())
			{
				this.markDirty();
				//If creative, and the item was a bucket, don't move the items around
				if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldItem.getItem() == Items.BUCKET))
					return ActionResultType.SUCCESS;
				if(heldItem.getCount() > 1)
				{
					heldItem.shrink(1);
					player.setHeldItem(hand, heldItem);
					ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
				}
				else
				{
					player.setHeldItem(hand, result.getResult());
				}
			}
		}
		
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void markDirty()
	{
		if(!this.world.isRemote)
		{
			TileEntityUtil.sendUpdatePacket(this);
			super.markDirty();
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		compound.put("Tank", this.tankContents.writeToNBT(new CompoundNBT()));
		
		return super.write(compound);
	}
	
	public CompoundNBT superWrite(CompoundNBT compound) { return super.write(compound); }
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		if(compound.contains("Tank", Constants.NBT.TAG_COMPOUND))
			this.tankContents = FluidStack.loadFluidStackFromNBT(compound.getCompound("Tank"));
		
		super.read(state, compound);
	}
	
	public void loadFromItem(ItemStack stack)
	{
		this.tankContents = FluidTankItem.GetFluid(stack);
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
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 0, this.write(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		CompoundNBT compound = pkt.getNbtCompound();
		this.read(this.getBlockState(),  compound);
	}
	
	@Override
	public void onLoad() {
		if(this.world.isRemote)
			TileEntityUtil.requestUpdatePacket(this.world, this.pos);
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
				this.markDirty();
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
			this.markDirty();
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
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
		
		//Otherwise return none
		return super.getCapability(cap, side);
    }
	
}
