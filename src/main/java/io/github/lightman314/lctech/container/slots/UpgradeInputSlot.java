package io.github.lightman314.lctech.container.slots;

import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class UpgradeInputSlot extends Slot{

	private final IUpgradeable machine;
	private final IMessage onModified;
	
	public UpgradeInputSlot(IInventory inventory, int index, int x, int y, IUpgradeable machine, IMessage onModified)
	{
		super(inventory, index, x, y);
		this.machine = machine;
		this.onModified = onModified;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		Item item = stack.getItem();
		if(item instanceof UpgradeItem)
			return ((UpgradeItem)item).getUpgradeType().allowedForMachine(machine);
		return false;
	}
	
	@Override
	public int getSlotStackLimit() { return 1; }
	
	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		this.onModified.accept();
	}
	
	public interface IMessage
	{
		public void accept();
	}
	
}
