package io.github.lightman314.lctech.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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

public class FluidShardItem extends Item {
	
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(5f, 2f, 7.51f, 7f, 12f, 0.98f, FluidSides.Create(Direction.SOUTH, Direction.NORTH));
	
	private static final List<FluidShardItem> SHARD_ITEMS = Lists.newArrayList();
	@OnlyIn(Dist.CLIENT)
	public static List<ModelResourceLocation> getShardModelList(){
		
		List<ModelResourceLocation> list = Lists.newArrayList();
		SHARD_ITEMS.forEach(shardItem -> list.add(new ModelResourceLocation(shardItem.getRegistryName(),"inventory")));
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
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		FluidStack fluid = GetFluid(stack);
		if(!fluid.isEmpty())
		{
			tooltip.add(FluidFormatUtil.getFluidName(fluid));
			tooltip.add(EasyText.literal(FluidFormatUtil.formatFluidAmount(fluid.getAmount()) + "mB").withStyle(TextFormatting.GRAY));
		}
	}
	
	//Force the tank item to have its tank data
	@Override
	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
		if(GetFluid(stack).isEmpty() && entity instanceof PlayerEntity)
		{
			//Remove the empty fluid shard from the players inventory
			PlayerEntity player = (PlayerEntity)entity;
			player.inventory.setItem(itemSlot, ItemStack.EMPTY);
		}
	}
	
	public static FluidStack GetFluid(ItemStack stack)
	{
		if(stack.getItem() instanceof FluidShardItem)
		{
			CompoundNBT tag = stack.getOrCreateTag();
			if(tag.contains("Tank", Constants.NBT.TAG_COMPOUND))
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
		CompoundNBT tag = stack.getOrCreateTag();
		tag.put("Tank", tank.writeToNBT(new CompoundNBT()));
		stack.setTag(tag);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT compound)
	{
		return new FluidShardCapability(stack);
	}
	
	public static class FluidShardCapability implements IFluidHandlerItem, ICapabilityProvider
	{
		
		final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
		
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
		public int fill(FluidStack resource, FluidAction action) {
			return 0;
		}

		@Nonnull
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

		@Nonnull
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

		@Nonnull
		@Override
		public ItemStack getContainer() {
			return this.stack;
		}
		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction side) {
			return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(capability, holder);
		}
		
	}
	
}
