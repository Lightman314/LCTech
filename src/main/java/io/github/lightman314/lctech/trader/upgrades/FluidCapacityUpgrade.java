package io.github.lightman314.lctech.trader.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidCapacityUpgrade extends CapacityUpgrade{

	@Override
	public boolean allowedForMachine(IUpgradeable machine) {
		return machine instanceof IFluidTrader;
	}
	
	@Override
	public List<ITextComponent> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(new TranslationTextComponent("tooltip.lctech.upgrade.fluid_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)).mergeStyle(TextFormatting.BLUE));
	}

}
