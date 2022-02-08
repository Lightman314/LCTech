package io.github.lightman314.lctech.container;

import com.google.common.base.Supplier;

import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import net.minecraft.entity.player.PlayerInventory;

public class UniversalFluidEditContainer extends FluidEditContainer{

	public UniversalFluidEditContainer(int windowId, PlayerInventory inventory, Supplier<IFluidTrader> traderSource, int tradeIndex) {
		super(ModContainers.UNIVERSAL_FLUID_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
