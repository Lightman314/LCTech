package io.github.lightman314.lctech.blockentities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.blocks.FluidTankBlock;
import io.github.lightman314.lctech.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.util.PlayerUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class FluidTankBlockEntity extends BlockEntity implements IFluidHandler{

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
	
	public FluidTankBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.FLUID_TANK, pos, state); }
	
	protected FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
	
	public InteractionResult onInteraction(Player player, InteractionHand hand)
	{
		ItemStack heldItem = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelected() : player.getInventory().offhand.get(0);
		FluidUtil.getFluidHandler(heldItem).ifPresent(fluidHandler ->{
			if(fluidHandler instanceof FluidBucketWrapper) //Bucket interactions
			{
				FluidBucketWrapper bucketWrapper = (FluidBucketWrapper)fluidHandler;
				
				FluidStack bucketFluid = bucketWrapper.getFluid();
				if(!bucketWrapper.getFluid().isEmpty())
				{
					//Attempt to fill the tank
					if(this.tankContents.isEmpty() || this.tankContents.isFluidEqual(bucketFluid))
					{
						//Fluid is valid; Attempt to fill the tank
						if(this.getTankSpace() >= bucketFluid.getAmount())
						{
							//Has space, fill the tank
							if(this.tankContents.isEmpty())
								this.tankContents = bucketFluid;
							else
								this.tankContents.grow(bucketFluid.getAmount());
							this.setChanged();
							
							//Drain the bucket (unless we're in creative mode)
							if(!player.isCreative())
							{
								ItemStack emptyBucket = new ItemStack(Items.BUCKET);
								this.replacePlayerItem(player, hand, heldItem, emptyBucket);
							}
						}
					}
				} else
				{
					//Attempt to drain the tank
					if(!this.tankContents.isEmpty() && this.tankContents.getAmount() >= FluidAttributes.BUCKET_VOLUME)
					{
						//Tank has more than 1 bucket of fluid, drain the tank
						Item filledBucket = this.tankContents.getFluid().getBucket();
						if(filledBucket != null && filledBucket != Items.AIR)
						{
							//Filled bucket is not null or air; Fill the bucket, and drain the tank
							this.tankContents.shrink(FluidAttributes.BUCKET_VOLUME);
							if(this.tankContents.isEmpty())
								this.tankContents = FluidStack.EMPTY;
							this.setChanged();
							
							//Fill the bucket (unless we're in creative mode)
							if(!player.isCreative())
							{
								ItemStack newBucket = new ItemStack(filledBucket, 1);
								this.replacePlayerItem(player, hand, heldItem, newBucket);
							}
							
						}
					}
				}
			}
			else //Normal fluid handler interaction
			{
				//Attempt to fill the tank first
				FluidStack drainResults = FluidStack.EMPTY;
				if(this.tankContents.isEmpty())
				{
					//Attempt to drain anything
					drainResults = fluidHandler.drain(this.getTankCapacity(), FluidAction.EXECUTE);
				}
				else
				{
					//Attempt to drain a fluid that matches the tank
					FluidStack drainRequest = this.tankContents.copy();
					drainRequest.setAmount(this.getTankSpace());
					
					drainResults = fluidHandler.drain(drainRequest, FluidAction.EXECUTE);
				}
				if(!drainResults.isEmpty())
				{
					if(this.tankContents.isEmpty())
						this.tankContents = drainResults;
					else
						this.tankContents.grow(drainResults.getAmount());
					this.setChanged();
				}
				else if(!this.tankContents.isEmpty())
				{
					//Deposit failed, attempt to drain the fluid then
					FluidStack fillRequest = this.tankContents.copy();
					
					int drainedAmount = fluidHandler.fill(fillRequest, FluidAction.EXECUTE);
					if(drainedAmount > 0)
					{
						this.tankContents.shrink(drainedAmount);
						if(this.tankContents.isEmpty())
							this.tankContents = FluidStack.EMPTY;
						this.setChanged();
					}
				}
			}
		});
		if(FluidUtil.getFluidHandler(heldItem).isPresent())
			return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}
	
	private void replacePlayerItem(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newItem)
	{
		oldStack.shrink(1);
		if(oldStack.isEmpty())
		{
			if(hand == InteractionHand.MAIN_HAND)
				player.getInventory().setItem(player.getInventory().selected, newItem);
			else
				player.getInventory().offhand.set(0, newItem);
		}
		else //Give it to the player manually
		{
			PlayerUtil.givePlayerItem(player, newItem);
		}
	}
	
	@Override
	public void setChanged()
	{
		if(!this.level.isClientSide)
		{
			TileEntityUtil.sendUpdatePacket(this);
			super.setChanged();
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		
		compound.put("Tank", this.tankContents.writeToNBT(new CompoundTag()));
		
		super.saveAdditional(compound);
		
	}
	
	public CompoundTag superWrite(CompoundTag compound) { return super.save(compound); }
	
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
			TileEntityUtil.requestUpdatePacket(this.level, this.worldPosition);
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
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
		
		//Otherwise return none
		return super.getCapability(cap, side);
    }
	
}
