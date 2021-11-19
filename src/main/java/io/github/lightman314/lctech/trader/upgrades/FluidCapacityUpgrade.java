package io.github.lightman314.lctech.trader.upgrades;

import io.github.lightman314.lctech.trader.IFluidTrader;

public class FluidCapacityUpgrade extends CapacityUpgrade{

	@Override
	public boolean allowedForMachine(IUpgradeable machine) {
		return machine instanceof IFluidTrader;
	}

}
