package io.github.lightman314.lctech.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.core.ModItems;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidShardItem extends Item{
	
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(5f, 2f, 7.51f, 7f, 12f, 0.98f, FluidSides.Create(Direction.SOUTH, Direction.NORTH));
	
	private static final List<FluidShardItem> SHARD_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static final List<ModelResourceLocation> getShardModelList(){
		
		List<ModelResourceLocation> list = Lists.newArrayList();
		SHARD_ITEMS.forEach(shardItem ->{
			list.add(new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(shardItem),"inventory"));
		});
		return list;
	}
	
	public final FluidRenderData renderData;
	
	public FluidShardItem(Properties properties)
	{
		super(properties.stacksTo(1));
		this.renderData = RENDER_DATA;
		SHARD_ITEMS.add(this);
	}
	
	public FluidShardItem(Properties properties, FluidRenderData renderData)
	{
		super(properties);
		this.renderData = renderData;
		SHARD_ITEMS.add(this);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		FluidStack fluid = GetFluid(stack);
		if(!fluid.isEmpty())
		{
			tooltip.add(FluidFormatUtil.getFluidName(fluid));
			tooltip.add(Component.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB").withStyle(ChatFormatting.GRAY));
		}
	}
	
	//Force the tank item to have it's tank data
	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
		if(GetFluid(stack).isEmpty() && entity instanceof Player)
		{
			//Remove the empty fluid shard from the players inventory
			Player player = (Player)entity;
			player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
		}
	}
	
	public static FluidStack GetFluid(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidShardItem)
		{
			CompoundTag tag = stack.getOrCreateTag();
			if(tag.contains("Tank", Tag.TAG_COMPOUND))
				return FluidStack.loadFluidStackFromNBT(tag.getCompound("Tank"));
		}
		return FluidStack.EMPTY;
	}
	
	public static ItemStack GetFluidShard(FluidStack stack)
	{
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack returnStack = new ItemStack(ModItems.FLUID_SHARD.get());
		WriteTankData(returnStack, stack);
		return returnStack;
		
	}
	
	public static void WriteTankData(ItemStack stack, FluidStack tank)
	{
		CompoundTag tag = stack.getOrCreateTag();
		tag.put("Tank", tank.writeToNBT(new CompoundTag()));
		stack.setTag(tag);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag compound)
	{
		return new FluidShardCapability(stack);
	}
	
	public static class FluidShardCapability implements IFluidHandlerItem, ICapabilityProvider
	{
		
		final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
		
		final ItemStack stack;
		
		private final FluidStack tank() { return GetFluid(this.stack); }
		private final void setTank(FluidStack tank) { WriteTankData(this.stack, tank); }
		
		public FluidShardCapability(ItemStack stack) { this.stack = stack; }

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
			return this.getFluidInTank(tank).getAmount();
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return false;
		}

		@Override
		//Cannot fill, only drain
		public int fill(FluidStack resource, FluidAction action) {
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
				if(tank.isEmpty())
					this.getContainer().shrink(1);
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
				if(tank.isEmpty())
					this.getContainer().shrink(1);
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
