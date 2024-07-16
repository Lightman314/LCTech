package io.github.lightman314.lctech.common.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.core.ModDataComponents;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.data.FluidData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class FluidShardItem extends Item{
	
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(5f, 2f, 7.51f, 7f, 12f, 0.98f, FluidSides.Create(Direction.SOUTH, Direction.NORTH));
	
	private static final List<FluidShardItem> SHARD_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static List<ModelResourceLocation> getShardModelList(){
		
		List<ModelResourceLocation> list = Lists.newArrayList();
		SHARD_ITEMS.forEach(shardItem ->
			list.add(new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(shardItem),ModelResourceLocation.INVENTORY_VARIANT))
		);
		return list;
	}
	
	public final FluidRenderData renderData;
	
	public FluidShardItem(Properties properties) { this(properties,RENDER_DATA); }
	
	public FluidShardItem(Properties properties, FluidRenderData renderData)
	{
		super(properties.stacksTo(1));
		this.renderData = renderData;
		SHARD_ITEMS.add(this);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		super.appendHoverText(stack, context, tooltip, flagIn);
		if(getData(stack).showTooltip)
		{
			FluidStack fluid = GetFluid(stack);
			if(!fluid.isEmpty())
			{
				tooltip.add(FluidFormatUtil.getFluidName(fluid));
				tooltip.add(Component.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB").withStyle(ChatFormatting.GRAY));
			}
		}
	}
	
	//Force the tank item to have its tank data
	@Override
	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
		if(GetFluid(stack).isEmpty())
		{
			//Remove the empty fluid shard from the players inventory
			stack.setCount(0);
		}
	}

	public static FluidStack GetFluid(ItemStack stack) { return getData(stack).getFluid(); }
	
	public static ItemStack GetFluidShard(FluidStack stack)
	{
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack returnStack = new ItemStack(ModItems.FLUID_SHARD.get());
		WriteTankData(returnStack, stack);
		return returnStack;
		
	}

	@Nonnull
	private static FluidData getData(@Nonnull ItemStack stack) { return stack.getOrDefault(ModDataComponents.FLUID_DATA,FluidData.EMPTY); }

	public static void WriteTankData(ItemStack stack, FluidStack tank)
	{
		FluidData data = getData(stack);
		stack.set(ModDataComponents.FLUID_DATA, data.withFluid(tank));
	}

	public static IFluidHandlerItem createHandler(@Nonnull ItemStack stack)
	{
		if(stack.getItem() instanceof FluidShardItem)
			return new FluidShardCapability(stack);
		return null;
	}

	public static class FluidShardCapability implements IFluidHandlerItem
	{
		
		final ItemStack stack;
		
		private FluidStack tank() { return GetFluid(this.stack); }
		private void setTank(FluidStack tank) { WriteTankData(this.stack, tank); }
		
		public FluidShardCapability(ItemStack stack) { this.stack = stack; }

		@Override
		public int getTanks() {
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank) {
			return tank == 0 ? this.tank().copy() : FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return this.getFluidInTank(tank).getAmount();
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
			return false;
		}

		@Override
		//Cannot fill, only drain
		public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
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
				if(tank.isEmpty())
					this.getContainer().shrink(1);
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
				if(tank.isEmpty())
					this.getContainer().shrink(1);
			}
			return resultStack;
		}

		@Nonnull
		@Override
		public ItemStack getContainer() { return this.stack; }
		
	}
	
}
