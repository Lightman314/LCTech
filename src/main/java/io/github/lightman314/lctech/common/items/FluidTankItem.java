package io.github.lightman314.lctech.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidTankItem extends BlockItem {
	
	private static final List<FluidTankItem> TANK_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static List<ModelResourceLocation> getTankModelList(){
		List<ModelResourceLocation> list = Lists.newArrayList();
		TANK_ITEMS.forEach(tankItem -> list.add(new ModelResourceLocation(tankItem.getRegistryName(),"inventory")));
		return list;
	}
	
	public FluidTankItem(Block block, Properties properties)
	{
		super(block, properties);
		TANK_ITEMS.add(this);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		FluidStack fluid = GetFluid(stack);
		if(!fluid.isEmpty())
		{
			tooltip.add(FluidFormatUtil.getFluidName(fluid));
			int capacity = GetCapacity(stack);
			tooltip.add(EasyText.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB / " + FluidFormatUtil.formatFluidAmount(capacity) + "mB").withStyle(TextFormatting.GRAY));
		}
		else
		{
			tooltip.add(EasyText.translatable("tooltip.lctech.fluid_tank.capacity", FluidFormatUtil.formatFluidAmount(GetCapacity(stack))));
		}
	}
	
	//Force the tank item to have its tank data
	@Override
	public void inventoryTick(ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
		if(!stack.hasTag())
			InitTankData(stack);
	}
	
	//Force the tank item to have its tank data
	@Override
	public void onCraftedBy(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull PlayerEntity playerIn) {
		InitTankData(stack);
	}
	
	public static FluidStack GetFluid(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidTankItem)
		{
			CompoundNBT tag = stack.getOrCreateTag();
			if(tag.contains("Tank", Constants.NBT.TAG_COMPOUND))
				return FluidStack.loadFluidStackFromNBT(tag.getCompound("Tank"));
		}
		return FluidStack.EMPTY;
	}
	
	private FluidTankBlock getTankBlock() {
		Block block = this.getBlock();
		if(block instanceof FluidTankBlock)
			return (FluidTankBlock)block;
		return null;
	}
	
	public static int GetCapacity(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidTankItem)
		{
			FluidTankBlock block = ((FluidTankItem)stack.getItem()).getTankBlock();
			if(block != null)
				return block.getTankCapacity();
		}
		return FluidTankBlockEntity.DEFAULT_CAPACITY;
	}
	
	public static ItemStack GetItemFromTank(@Nullable FluidTankBlockEntity blockEntity)
	{
		if(blockEntity == null)
		{
			//Return a default tank
			ItemStack returnStack = new ItemStack(ModBlocks.IRON_TANK.get());
			InitTankData(returnStack);
			return returnStack;
		}
		
		ItemStack returnStack = new ItemStack(blockEntity.getBlockState().getBlock().asItem());
		FluidStack tank = blockEntity.getTankContents();
		WriteTankData(returnStack, tank);
		
		return returnStack;
		
	}
	
	public static void InitTankData(ItemStack stack)
	{
		WriteTankData(stack, FluidStack.EMPTY);
	}
	
	public static void WriteTankData(ItemStack stack, FluidStack tank)
	{
		CompoundNBT tag = stack.getOrCreateTag();
		tag.put("Tank", tank.writeToNBT(new CompoundNBT()));
		stack.setTag(tag);
	}
	
	@Override
	public @Nonnull ItemStack getDefaultInstance() {
		ItemStack stack = new ItemStack(this);
		InitTankData(stack);
		return stack;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT compound)
	{
		return new FluidTankCapability(stack);
	}
	
	public static class FluidTankCapability implements IFluidHandlerItem, ICapabilityProvider
	{
		
		final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
		
		final ItemStack stack;
		
		private FluidStack tank() { return GetFluid(this.stack); }
		private void setTank(FluidStack tank) { WriteTankData(this.stack, tank); }
		private int capacity() { return GetCapacity(this.stack); }
		private int getTankSpace() { return this.capacity() - this.tank().getAmount(); }
		
		public FluidTankCapability(ItemStack stack) { this.stack = stack; }

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public @Nonnull FluidStack getFluidInTank(int tank) {
			return tank == 0 ? this.tank().copy() : FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return tank == 0 ? this.capacity() : FluidTankBlockEntity.DEFAULT_CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
			return tank == 0 && (this.tank().isEmpty() || this.tank().isFluidEqual(stack));
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if(isFluidValid(0, resource))
			{
				int fillAmount = MathUtil.clamp(resource.getAmount(), 0, this.getTankSpace());
				if(action.execute())
				{
					if(this.tank().isEmpty())
						this.setTank(resource.copy());
					else
					{
						FluidStack tank = this.tank();
						tank.grow(fillAmount);
						this.setTank(tank);
					}
				}
				return fillAmount;
			}
			return 0;
		}

		@Override
		public @Nonnull FluidStack drain(FluidStack resource, FluidAction action) {
			if(this.tank().isEmpty() || !this.tank().isFluidEqual(resource))
				return FluidStack.EMPTY;
			//Tank is not empty, and the resource is equal to the tank contents
			int drainAmount = MathUtil.clamp(resource.getAmount(), 0, this.tank().getAmount());
			FluidStack resultStack = this.tank().copy();
			resultStack.setAmount(drainAmount);
			if(action.execute())
			{
				//Drain the tank
				FluidStack tank = this.tank();
				tank.shrink(drainAmount);
				if(tank.isEmpty())
					tank = FluidStack.EMPTY;
				this.setTank(tank);
			}
			return resultStack;
		}

		@Override
		public @Nonnull FluidStack drain(int maxDrain, FluidAction action) {
			if(this.tank().isEmpty())
				return FluidStack.EMPTY;
			//Tank is not empty, and the resource is equal to the tank contents
			int drainAmount = MathUtil.clamp(maxDrain, 0, this.tank().getAmount());
			FluidStack resultStack = this.tank().copy();
			resultStack.setAmount(drainAmount);
			if(action.execute())
			{
				//Drain the tank
				FluidStack tank = this.tank();
				tank.shrink(drainAmount);
				if(tank.isEmpty())
					tank = FluidStack.EMPTY;
				this.setTank(tank);
			}
			return resultStack;
		}

		@Override
		public @Nonnull ItemStack getContainer() {
			return this.stack;
		}
		
		@Override
		public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction side) {
			return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(capability, holder);
		}
		
	}
	
}
