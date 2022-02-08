package io.github.lightman314.lctech.trader.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class FluidCapacityUpgrade extends CapacityUpgrade{

	@Override
	public boolean allowedForMachine(IUpgradeable machine) {
		return machine instanceof IFluidTrader;
	}
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(new TranslatableComponent("tooltip.lctech.upgrade.fluid_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)).withStyle(ChatFormatting.BLUE));
	}

}
