package io.github.lightman314.lctech.common.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
		
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(EasyText.literal(EnergyUtil.formatEnergyAmount(IBatteryItem.getStoredEnergy(stack))+ "/" + EnergyUtil.formatEnergyAmount(this.getMaxEnergyStorage(stack))).withStyle(TextFormatting.AQUA));
		
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        return IBatteryItem.createCapability(stack);
    }
	
	//Power Charge Bar
	@Override
	public boolean showDurabilityBar(ItemStack stack) { return true; }

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1d - ((double)IBatteryItem.getStoredEnergy(stack) / (double)this.getMaxEnergyStorage(stack));
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return TextFormatting.AQUA.getColor();
	}
	
	@Override
	 public void fillItemCategory(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> list) {
		if (this.allowdedIn(tab)) {
			list.add(new ItemStack(this));
			list.add(IBatteryItem.getFullBattery(this));
		}
	}
	
}
