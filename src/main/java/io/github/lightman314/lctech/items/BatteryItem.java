package io.github.lightman314.lctech.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.lightman314.lctech.util.EnergyUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class BatteryItem extends Item implements IBatteryItem{

	private final Supplier<Integer> maxEnergyStorage;
	public int getMaxEnergyStorage(ItemStack stack) { return this.maxEnergyStorage.get(); }
	
	public BatteryItem(Supplier<Integer> maxEnergyStorage, Properties properties)
	{
		super(properties.maxStackSize(1));
		this.maxEnergyStorage = maxEnergyStorage;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, world, tooltip, flagIn);
		tooltip.add(new StringTextComponent(EnergyUtil.formatEnergyAmount(IBatteryItem.getStoredEnergy(stack)) + "/" + EnergyUtil.formatEnergyAmount(this.getMaxEnergyStorage(stack))));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
	{
		return IBatteryItem.createCapability(stack);
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) { return true; }
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return 1d - ((double)IBatteryItem.getStoredEnergy(stack) / (double)this.getMaxEnergyStorage(stack));
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) { return TextFormatting.AQUA.getColor(); }
	
	
	
}
