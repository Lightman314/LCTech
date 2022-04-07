package io.github.lightman314.lctech.items;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public interface IBatteryItem {

	
	public int getMaxEnergyStorage(ItemStack stack);
	
	public static ICapabilityProvider createCapability(ItemStack stack)
	{
		if(stack.getItem() instanceof IBatteryItem)
		{
			return new BatteryEnergyStorage(stack);
		}
		else
		{
			LCTech.LOGGER.warn("Attempted to make a battery storage capability for an item that doesn't implement IBatteryItem.");
			return null;
		}
	}
	
	public static int getStoredEnergy(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("Battery", Tag.TAG_INT))
			return tag.getInt("Battery");
		return 0;
	}
	
	public static <T extends IBatteryItem & ItemLike> ItemStack getFullBattery(T item) {
		ItemStack newStack = new ItemStack(item);
		newStack.getOrCreateTag().putInt("Battery", item.getMaxEnergyStorage(newStack));
		return newStack;
	}
	
	public static class BatteryEnergyStorage implements IEnergyStorage, ICapabilityProvider
	{
		
		private final ItemStack stack;
		private final LazyOptional<IEnergyStorage> optional;
		
		private BatteryEnergyStorage(ItemStack stack)
		{
			this.stack = stack;
			this.optional = LazyOptional.of(() -> this);
			//Create the tag should it not exist
			if(this.getEnergyStored() == 0)
				this.setEnergyStored(0);
		}
		
		public ItemStack getContainer() { return this.stack; }

		private void setEnergyStored(int energyStored) {
			CompoundTag tag = stack.getOrCreateTag();
			tag.putInt("Battery", energyStored);
			stack.setTag(tag);
		}
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int receiveAmount = Math.min(maxReceive, this.getMaxEnergyStored() - this.getEnergyStored());
			if(!simulate)
			{
				this.setEnergyStored(this.getEnergyStored() + receiveAmount);
			}
			return receiveAmount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int extractAmount = Math.min(maxExtract, this.getEnergyStored());
			if(!simulate)
			{
				this.setEnergyStored(this.getEnergyStored() - extractAmount);
			}
			return extractAmount;
		}

		@Override
		public int getEnergyStored() {
			return IBatteryItem.getStoredEnergy(this.stack);
		}

		@Override
		public int getMaxEnergyStored() {
			if(this.stack.getItem() instanceof IBatteryItem)
				return ((IBatteryItem)this.stack.getItem()).getMaxEnergyStorage(this.stack);
			return 0;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return CapabilityEnergy.ENERGY.orEmpty(cap, this.optional);
		}
		
	}
	
}
