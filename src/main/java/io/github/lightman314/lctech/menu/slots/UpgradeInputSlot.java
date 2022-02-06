package io.github.lightman314.lctech.menu.slots;

import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeInputSlot extends Slot{

	private final IUpgradeable machine;
	private final IMessage onModified;
	
	public UpgradeInputSlot(Container inventory, int index, int x, int y, IUpgradeable machine, IMessage onModified)
	{
		super(inventory, index, x, y);
		this.machine = machine;
		this.onModified = onModified;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		Item item = stack.getItem();
		if(item instanceof UpgradeItem)
			return ((UpgradeItem)item).getUpgradeType().allowedForMachine(machine);
		return false;
	}
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public void setChanged() {
		super.setChanged();
		this.onModified.accept();
	}
	
	public interface IMessage
	{
		public void accept();
	}
	
}
