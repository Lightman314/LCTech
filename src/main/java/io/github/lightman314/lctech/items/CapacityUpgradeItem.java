package io.github.lightman314.lctech.items;

import io.github.lightman314.lctech.trader.upgrades.CapacityUpgrade;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.UpgradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

public class CapacityUpgradeItem extends UpgradeItem{

	private final int upgradeAmount;
	
	public CapacityUpgradeItem(CapacityUpgrade upgradeType, int upgradeAmount, Properties properties) {
		super(upgradeType, properties);
		this.upgradeAmount = MathUtil.clamp(upgradeAmount, 1, Integer.MAX_VALUE);
	}

	@Override
	public void fillUpgradeData(UpgradeData data) {
		data.setValue(CapacityUpgrade.CAPACITY, this.upgradeAmount);
	}

}
