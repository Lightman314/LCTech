package io.github.lightman314.lctech.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.util.FluidFormatUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidCapacityUpgrade extends CapacityUpgrade{

	@Override
	public List<ITextComponent> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(new TranslationTextComponent("tooltip.lctech.upgrade.fluid_capacity", FluidFormatUtil.formatFluidAmount(data.getIntValue(CapacityUpgrade.CAPACITY))).mergeStyle(TextFormatting.BLUE));
	}

}
