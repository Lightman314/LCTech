package io.github.lightman314.lctech.trader.upgrades;

import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class CapacityUpgrade extends UpgradeType{

	public static final String CAPACITY = "capacity";
	private static final List<String> DATA_TAGS = ImmutableList.of(CAPACITY);

	@Override
	protected List<String> getDataTags() {
		return DATA_TAGS;
	}

	@Override
	protected Object defaultTagValue(String tag) {
		if(tag == CAPACITY)
			return 1;
		return null;
	}

	
	
}
