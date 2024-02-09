package io.github.lightman314.lctech.common.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class EnergyCapacityUpgrade extends CapacityUpgrade {
	
	@Nonnull
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(EasyText.translatable("tooltip.lctech.upgrade.energy_capacity", EnergyUtil.formatEnergyAmount(data.getIntValue(CapacityUpgrade.CAPACITY))).withStyle(ChatFormatting.BLUE));
	}

}
