package io.github.lightman314.lctech.menu;

import io.github.lightman314.lctech.blockentities.FluidTraderBlockEntity;
import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import net.minecraft.world.entity.player.Inventory;

public class FluidTraderMenuCR extends FluidTraderMenu implements ITraderCashRegisterMenu{

	public CashRegisterBlockEntity cashRegister;
	
	public FluidTraderMenuCR(int windowId, Inventory inventory, FluidTraderBlockEntity tileEntity, CashRegisterBlockEntity cashRegister) {
		super(ModMenus.FLUID_TRADER_CR, windowId, inventory, tileEntity);
		this.cashRegister = cashRegister;
	}
	
	public int getThisIndex() { return this.cashRegister.getTraderIndex(this.tileEntity); }
	
	public int getTotalCount() { return this.cashRegister.getPairedTraderSize(); }
	
	public void OpenNextContainer(int direction) { this.cashRegister.OpenContainer(getThisIndex(), getThisIndex() + direction, direction, this.player); }
	
	public void OpenContainerIndex(int index) {
		int previousIndex = index - 1;
		if(previousIndex < 0)
			previousIndex = this.cashRegister.getPairedTraderSize() - 1;
		this.cashRegister.OpenContainer(previousIndex, index, 1,  this.player);
	}
	
}
