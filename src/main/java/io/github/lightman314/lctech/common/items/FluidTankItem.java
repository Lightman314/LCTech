package io.github.lightman314.lctech.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.core.ModDataComponents;
import io.github.lightman314.lctech.common.items.data.FluidData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class FluidTankItem extends BlockItem {
	
	private static final List<FluidTankItem> TANK_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static List<ModelResourceLocation> getTankModelList(){
		List<ModelResourceLocation> list = Lists.newArrayList();
		TANK_ITEMS.forEach(tankItem ->
			list.add(new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(tankItem),ModelResourceLocation.INVENTORY_VARIANT))
		);
		return list;
	}
	
	public FluidTankItem(Block block, Properties properties)
	{
		super(block, properties);
		TANK_ITEMS.add(this);
	}

	@Override
	public void verifyComponentsAfterLoad(@Nonnull ItemStack stack) {
		if(!stack.has(ModDataComponents.FLUID_DATA))
			stack.set(ModDataComponents.FLUID_DATA,FluidData.EMPTY);
		super.verifyComponentsAfterLoad(stack);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  context,  tooltip,  flagIn);
		if(getData(stack).showTooltip)
		{
			FluidStack fluid = GetFluid(stack);
			if(!fluid.isEmpty())
			{
				tooltip.add(FluidFormatUtil.getFluidName(fluid));
				int capacity = GetCapacity(stack);
				tooltip.add(Component.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB / " + FluidFormatUtil.formatFluidAmount(capacity) + "mB").withStyle(ChatFormatting.GRAY));
			}
			else
			{
				tooltip.add(TechText.TOOLTIP_TANK_CAPACITY.get(FluidFormatUtil.formatFluidAmount(GetCapacity(stack))));
			}
		}
	}

	@Nonnull
	private static FluidData getData(@Nonnull ItemStack stack) { return stack.getOrDefault(ModDataComponents.FLUID_DATA,FluidData.EMPTY); }

	public static FluidStack GetFluid(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidTankItem)
			return getData(stack).getFluid();
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
		if(stack.getItem() instanceof FluidTankItem tank)
		{
			FluidTankBlock block = tank.getTankBlock();
			if(block != null)
				return block.getTankCapacity();
		}
		return FluidTankBlockEntity.DEFAULT_CAPACITY;
	}
	
	public static ItemStack GetItemFromTank(@Nonnull FluidTankBlockEntity blockEntity)
	{
		ItemStack returnStack = new ItemStack(blockEntity.getBlockState().getBlock().asItem());
		FluidStack tank = blockEntity.getTankContents();
		WriteTankData(returnStack, tank);
		return returnStack;
		
	}
	
	public static void WriteTankData(ItemStack stack, FluidStack tank)
	{
		FluidData data = getData(stack);
		stack.set(ModDataComponents.FLUID_DATA,data.withFluid(tank));
	}

	@Nullable
	public static IFluidHandlerItem createHandler(@Nonnull ItemStack stack)
	{
		if(stack.getItem() instanceof FluidTankItem)
			return new FluidTankCapability(stack);
		return null;
	}

	public static class FluidTankCapability implements IFluidHandlerItem
	{
		
		final ItemStack stack;
		
		private FluidStack tank() { return GetFluid(this.stack); }
		private void setTank(FluidStack tank) { WriteTankData(this.stack, tank); }
		private int capacity() { return GetCapacity(this.stack); }
		private int getTankSpace() { return this.capacity() - this.tank().getAmount(); }
		
		public FluidTankCapability(ItemStack stack) { this.stack = stack; }

		@Override
		public int getTanks() { return 1; }

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank) {
			return tank == 0 ? this.tank().copy() : FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return tank == 0 ? this.capacity() : FluidTankBlockEntity.DEFAULT_CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
			return tank == 0 && (this.tank().isEmpty() || FluidStack.isSameFluidSameComponents(this.tank(),stack));
		}

		@Override
		public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
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

		@Nonnull
		@Override
		public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
			if(this.tank().isEmpty() || !FluidStack.isSameFluidSameComponents(this.tank(),resource))
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

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, @Nonnull FluidAction action) {
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

		@Nonnull
		@Override
		public ItemStack getContainer() {
			return this.stack;
		}
		
	}
	
}
