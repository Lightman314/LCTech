package io.github.lightman314.lctech.common.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.util.EnergyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class BatteryItem extends Item implements IBatteryItem{

	private final Supplier<Integer> maxEnergyStorage;
	public int getMaxEnergyStorage(ItemStack stack) { return this.maxEnergyStorage.get(); }
	
	public BatteryItem(Supplier<Integer> maxEnergyStorage, Properties properties)
	{
		super(properties.stacksTo(1));
		this.maxEnergyStorage = maxEnergyStorage;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(new TextComponent(EnergyUtil.formatEnergyAmount(IBatteryItem.getStoredEnergy(stack))+ "/" + EnergyUtil.formatEnergyAmount(this.getMaxEnergyStorage(stack))).withStyle(ChatFormatting.AQUA));
		
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return IBatteryItem.createCapability(stack);
    }
	
	//Power Charge Bar
	public boolean isBarVisible(ItemStack stack) {
      return true;
	}

	public int getBarWidth(ItemStack stack) {
		return Math.round((float)Math.min(IBatteryItem.getStoredEnergy(stack), this.getMaxEnergyStorage(stack)) * 13.0F / (float)this.getMaxEnergyStorage(stack));
	}

	public int getBarColor(ItemStack stack) {
		return ChatFormatting.AQUA.getColor();
	}
	
	@Override
	 public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
		if (this.allowdedIn(tab)) {
			list.add(new ItemStack(this));
			list.add(IBatteryItem.getFullBattery(this));
		}
	}
	
}
