package io.github.lightman314.lctech.items;

import io.github.lightman314.lctech.upgrades.CapacityUpgrade;
import io.github.lightman314.lctech.upgrades.UpgradeType.UpgradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

public class CapacityUpgradeItem extends UpgradeItem{

	private final int capacityAmount;
	
	public CapacityUpgradeItem(CapacityUpgrade upgradeType, int capacityAmount, Properties properties)
	{
		super(upgradeType, properties);
		this.capacityAmount = MathUtil.clamp(capacityAmount, 1, Integer.MAX_VALUE);
	}

	@Override
	public void fillUpgradeData(UpgradeData data) {
		data.setValue(CapacityUpgrade.CAPACITY, this.capacityAmount);
	}
	
}
