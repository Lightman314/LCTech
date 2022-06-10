package io.github.lightman314.lctech.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class EnergyCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(Component.translatable("tooltip.lctech.upgrade.energy_capacity", EnergyUtil.formatEnergyAmount(data.getIntValue(CapacityUpgrade.CAPACITY))).withStyle(ChatFormatting.BLUE));
	}

}
