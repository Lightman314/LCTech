package io.github.lightman314.lctech.common.items;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.core.ModDataComponents;
import io.github.lightman314.lctech.common.items.data.BatteryData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public interface IBatteryItem {

	
	int getMaxEnergyStorage(ItemStack stack);
	
	static IEnergyStorage createCapability(ItemStack stack)
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
	
	static int getStoredEnergy(ItemStack stack) { return getData(stack).energy(); }

	@Nonnull
	static BatteryData getData(@Nonnull ItemStack stack) { return stack.getOrDefault(ModDataComponents.ENERGY_DATA,BatteryData.EMPTY); }
	
	static <T extends IBatteryItem & ItemLike> ItemStack getFullBattery(T item) {
		ItemStack newStack = new ItemStack(item);
		BatteryData data = getData(newStack);
		newStack.set(ModDataComponents.ENERGY_DATA, data.withEnergy(item.getMaxEnergyStorage(newStack)));
		return newStack;
	}

	static ItemStack HideEnergyBar(Supplier<? extends ItemLike> item) { return HideEnergyBar(new ItemStack(item.get())); }
	static ItemStack HideEnergyBar(ItemLike item) { return HideEnergyBar(new ItemStack(item)); }
	static ItemStack HideEnergyBar(ItemStack stack) {
		BatteryData data = getData(stack);
		stack.set(ModDataComponents.ENERGY_DATA,data.withEnergyBarVisible(false));
		return stack;
	}

	static boolean isEnergyBarVisible(ItemStack batteryStack)
	{
		if(batteryStack.getItem() instanceof IBatteryItem)
			return getData(batteryStack).energyBarVisible();
		return false;
	}
	
	class BatteryEnergyStorage implements IEnergyStorage
	{
		
		private final ItemStack stack;
		
		private BatteryEnergyStorage(ItemStack stack)
		{
			this.stack = stack;
			//Create the tag should it not exist
			if(this.getEnergyStored() == 0)
				this.setEnergyStored(0);
		}

		private void setEnergyStored(int energyStored) {
			BatteryData data = getData(this.stack);
			stack.set(ModDataComponents.ENERGY_DATA, data.withEnergy(energyStored));
		}
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int receiveAmount = Math.min(maxReceive, this.getMaxEnergyStored() - this.getEnergyStored());
			if(!simulate)
				this.setEnergyStored(this.getEnergyStored() + receiveAmount);
			return receiveAmount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int extractAmount = Math.min(maxExtract, this.getEnergyStored());
			if(!simulate)
				this.setEnergyStored(this.getEnergyStored() - extractAmount);
			return extractAmount;
		}

		@Override
		public int getEnergyStored() { return IBatteryItem.getStoredEnergy(this.stack); }

		@Override
		public int getMaxEnergyStored() {
			if(this.stack.getItem() instanceof IBatteryItem battery)
				return battery.getMaxEnergyStorage(this.stack);
			return 0;
		}

		@Override
		public boolean canExtract() { return true; }

		@Override
		public boolean canReceive() { return true; }
		
	}
	
}
