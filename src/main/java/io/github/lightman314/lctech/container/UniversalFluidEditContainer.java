package io.github.lightman314.lctech.container;

import com.google.common.base.Supplier;

import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.trader.IFluidTrader;
import net.minecraft.world.entity.player.Inventory;

public class UniversalFluidEditContainer extends FluidEditContainer{

	public UniversalFluidEditContainer(int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource, int tradeIndex) {
		super(ModContainers.UNIVERSAL_FLUID_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
