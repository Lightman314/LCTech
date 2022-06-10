package io.github.lightman314.lctech.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FluidCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(Component.translatable("tooltip.lctech.upgrade.fluid_capacity", FluidFormatUtil.formatFluidAmount(data.getIntValue(CapacityUpgrade.CAPACITY))).withStyle(ChatFormatting.BLUE));
	}

}
