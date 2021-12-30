package io.github.lightman314.lctech.container;

import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import net.minecraft.world.entity.player.Inventory;

public class FluidTraderContainerCR extends FluidTraderContainer implements ITraderCashRegisterMenu{

	public CashRegisterBlockEntity cashRegister;
	
	public FluidTraderContainerCR(int windowId, Inventory inventory, FluidTraderTileEntity tileEntity, CashRegisterBlockEntity cashRegister) {
		super(ModContainers.FLUID_TRADER_CR, windowId, inventory, tileEntity);
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
