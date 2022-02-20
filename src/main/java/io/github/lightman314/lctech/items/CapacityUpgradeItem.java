package io.github.lightman314.lctech.items;

import java.util.function.Supplier;

import io.github.lightman314.lctech.upgrades.CapacityUpgrade;
import io.github.lightman314.lctech.upgrades.UpgradeType.UpgradeData;

public class CapacityUpgradeItem extends UpgradeItem{

	private final Supplier<Integer> capacityAmount;
	
	public CapacityUpgradeItem(CapacityUpgrade upgradeType, int capacityAmount, Properties properties)
	{
		this(upgradeType, () -> capacityAmount, properties);
	}
	
	public CapacityUpgradeItem(CapacityUpgrade upgradeType, Supplier<Integer> capacityAmount, Properties properties)
	{
		super(upgradeType, properties);
		this.capacityAmount = capacityAmount;
	}

	@Override
	public void fillUpgradeData(UpgradeData data) {
		data.setValue(CapacityUpgrade.CAPACITY, Math.max(this.capacityAmount.get(), 1));
	}
	
}
