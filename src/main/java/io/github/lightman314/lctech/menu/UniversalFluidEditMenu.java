package io.github.lightman314.lctech.menu;

import com.google.common.base.Supplier;

import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.trader.IFluidTrader;
import net.minecraft.world.entity.player.Inventory;

public class UniversalFluidEditMenu extends FluidEditMenu{

	public UniversalFluidEditMenu(int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource, int tradeIndex) {
		super(ModMenus.UNIVERSAL_FLUID_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
