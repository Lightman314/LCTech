package io.github.lightman314.lctech.common.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
		
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(EasyText.literal(EnergyUtil.formatEnergyAmount(IBatteryItem.getStoredEnergy(stack))+ "/" + EnergyUtil.formatEnergyAmount(this.getMaxEnergyStorage(stack))).withStyle(ChatFormatting.AQUA));
		
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return IBatteryItem.createCapability(stack);
    }
	
	//Power Charge Bar
	public boolean isBarVisible(@Nonnull ItemStack stack) { return IBatteryItem.isEnergyBarVisible(stack); }

	public int getBarWidth(@Nonnull ItemStack stack) {
		return Math.round((float)Math.min(IBatteryItem.getStoredEnergy(stack), this.getMaxEnergyStorage(stack)) * 13.0F / (float)this.getMaxEnergyStorage(stack));
	}

	public int getBarColor(@Nonnull ItemStack stack) { return ChatFormatting.AQUA.getColor(); }
	
}
