package io.github.lightman314.lctech.tileentities;

import java.util.concurrent.atomic.AtomicBoolean;

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
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
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
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
		ItemStack heldItem = hand == Hand.MAIN_HAND ? player.inventory.getCurrentItem() : player.inventory.offHandInventory.get(0);
		if(heldItem.getItem() instanceof BucketItem)
		{
			BucketItem bucket = (BucketItem)heldItem.getItem();
			if(bucket.getFluid() == Fluids.EMPTY && !this.tankContents.isEmpty() && this.tankContents.getAmount() >= FluidAttributes.BUCKET_VOLUME)
			{
				//Confirm that the fluid has a filled bucket item
				if(this.tankContents.getFluid().getFilledBucket() != null && this.tankContents.getFluid().getFilledBucket() != Items.AIR)
				{
					//Drain the tank
					this.tankContents.shrink(FluidAttributes.BUCKET_VOLUME);
					if(this.tankContents.isEmpty())
						this.tankContents = FluidStack.EMPTY;
					this.markDirty();
					//Fill the bucket
					if(!player.abilities.isCreativeMode)
						this.replacePlayerItem(player, hand, heldItem, new ItemStack(bucket.getFluid().getFilledBucket()));
				}
			}
			else if(this.tankContents.isEmpty() || bucket.getFluid() == this.tankContents.getFluid())
			{
				if(this.getTankSpace() >= FluidAttributes.BUCKET_VOLUME)
				{
					//Fill the tank
					if(this.tankContents.isEmpty())
						this.tankContents = new FluidStack(bucket.getFluid(), FluidAttributes.BUCKET_VOLUME);
					else
						this.tankContents.grow(FluidAttributes.BUCKET_VOLUME);
					this.markDirty();
					//Empty the bucket
					if(!player.abilities.isCreativeMode)
						this.replacePlayerItem(player, hand, heldItem, new ItemStack(Items.BUCKET));
				}
			}
			return ActionResultType.SUCCESS;
		}
		AtomicBoolean success = new AtomicBoolean(false);
		FluidUtil.getFluidHandler(heldItem).ifPresent(fluidHandler ->{
			if(this.tankContents.isEmpty())
			{
				//Attempt to drain any contents of the fluid handler
				FluidStack drainedStack = fluidHandler.drain(this.getTankSpace(), FluidAction.EXECUTE);
				if(!drainedStack.isEmpty())
				{
					this.tankContents = drainedStack;
					this.markDirty();
					success.set(true);
				}
			}
			else
			{
				//Attempt to drain a matching fluid of the tank
				FluidStack drainRequest = this.tankContents.copy();
				drainRequest.setAmount(this.getTankSpace());
				FluidStack drainedStack = fluidHandler.drain(drainRequest, FluidAction.EXECUTE);
				if(!drainedStack.isEmpty())
				{
					this.tankContents.grow(drainedStack.getAmount());
					this.markDirty();
					success.set(true);
				}
				else
				{
					//Drain attempt failed, attempt to fill it with the tanks fluid
					int fillAmount = fluidHandler.fill(this.tankContents.copy(), FluidAction.EXECUTE);
					if(fillAmount > 0)
					{
						this.tankContents.shrink(fillAmount);
						if(this.tankContents.isEmpty())
							this.tankContents = FluidStack.EMPTY;
						this.markDirty();
						success.set(true);
					}
				}
			}
		});
		if(FluidUtil.getFluidHandler(heldItem).isPresent())
			return ActionResultType.SUCCESS;
		return ActionResultType.PASS;
	}
	
	private void replacePlayerItem(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newItem)
	{
		oldStack.shrink(1);
		if(oldStack.isEmpty())
		{
			if(hand == Hand.MAIN_HAND)
				player.inventory.setInventorySlotContents(player.inventory.currentItem, newItem);
			else
				player.inventory.offHandInventory.set(0, newItem);
		}
		else //Give it to the player manually
		{
			if(!player.inventory.addItemStackToInventory(newItem))
			{
				if(!this.world.isRemote)
				{
					ItemEntity itemEntity = new ItemEntity(this.world, player.getPosX(), player.getPosY(), player.getPosZ(), newItem);
					world.addEntity(itemEntity);
				}
			}
		}
	}
	
	@Override
	public void markDirty()
	{
		TileEntityUtil.sendUpdatePacket(this);
		super.markDirty();
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
