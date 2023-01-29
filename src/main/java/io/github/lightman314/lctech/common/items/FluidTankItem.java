package io.github.lightman314.lctech.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidTankItem extends BlockItem{
	
	private static final List<FluidTankItem> TANK_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static List<ModelResourceLocation> getTankModelList(){
		List<ModelResourceLocation> list = Lists.newArrayList();
		TANK_ITEMS.forEach(tankItem ->{
			list.add(new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(tankItem),"inventory"));
		});
		return list;
	}
	
	public FluidTankItem(Block block, Properties properties)
	{
		super(block, properties);
		TANK_ITEMS.add(this);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		FluidStack fluid = GetFluid(stack);
		if(!fluid.isEmpty())
		{
			tooltip.add(FluidFormatUtil.getFluidName(fluid));
			int capacity = GetCapacity(stack);
			tooltip.add(Component.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB / " + FluidFormatUtil.formatFluidAmount(capacity) + "mB").withStyle(ChatFormatting.GRAY));
		}
		else
		{
			tooltip.add(Component.translatable("tooltip.lctech.fluid_tank.capacity", FluidFormatUtil.formatFluidAmount(GetCapacity(stack))));
		}
	}
	
	//Force the tank item to have it's tank data
	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if(!stack.hasTag())
			InitTankData(stack);
	}
	
	//Force the tank item to have it's tank data
	@Override
	public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn) {
		InitTankData(stack);
	}
	
	public static FluidStack GetFluid(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidTankItem)
		{
			CompoundTag tag = stack.getOrCreateTag();
			if(tag.contains("Tank", Tag.TAG_COMPOUND))
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
	
	public static double GetTankFillPercent(ItemStack stack)
	{
		return MathUtil.clamp((double)GetFluid(stack).getAmount() / (double)GetCapacity(stack), 0d, 1d);
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
		CompoundTag tag = stack.getOrCreateTag();
		tag.put("Tank", tank.writeToNBT(new CompoundTag()));
		stack.setTag(tag);
	}
	
	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = new ItemStack(this);
		InitTankData(stack);
		return stack;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag compound)
	{
		return new FluidTankCapability(stack);
	}
	
	public static class FluidTankCapability implements IFluidHandlerItem, ICapabilityProvider
	{
		
		final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
		
		final ItemStack stack;
		
		private final FluidStack tank() { return GetFluid(this.stack); }
		private final void setTank(FluidStack tank) { WriteTankData(this.stack, tank); }
		private final int capacity() { return GetCapacity(this.stack); }
		private final int getTankSpace() { return this.capacity() - this.tank().getAmount(); }
		
		public FluidTankCapability(ItemStack stack) { this.stack = stack; }

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return tank == 0 ? this.tank().copy() : FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return tank == 0 ? this.capacity() : FluidTankBlockEntity.DEFAULT_CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return tank == 0 ? this.tank().isEmpty() || this.tank().isFluidEqual(stack) : false;
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
		public FluidStack drain(FluidStack resource, FluidAction action) {
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
		public FluidStack drain(int maxDrain, FluidAction action) {
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
		public ItemStack getContainer() {
			return this.stack;
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
			return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, holder);
		}
		
	}
	
}
