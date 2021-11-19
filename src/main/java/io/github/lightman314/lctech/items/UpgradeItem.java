package io.github.lightman314.lctech.items;

import io.github.lightman314.lctech.trader.upgrades.UpgradeType;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeItem;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.UpgradeData;
import net.minecraft.item.Item;

public abstract class UpgradeItem extends Item implements IUpgradeItem{

	private final UpgradeType upgradeType;
	
	public UpgradeItem(UpgradeType upgradeType, Properties properties)
	{
		super(properties);
		this.upgradeType = upgradeType;
	}

	@Override
	public UpgradeType getUpgradeType() { return this.upgradeType; }
	
	@Override
	public UpgradeData getUpgradeData()
	{
		UpgradeData data = this.upgradeType.getDefaultData();
		this.fillUpgradeData(data);
		return data;
	}
	
	protected abstract void fillUpgradeData(UpgradeData data);
	
}
