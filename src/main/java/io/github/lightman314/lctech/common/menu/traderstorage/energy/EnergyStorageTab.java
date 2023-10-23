package io.github.lightman314.lctech.common.menu.traderstorage.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.common.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnergyStorageTab extends TraderStorageTab{

	public EnergyStorageTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new EnergyStorageClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }

	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }

	BatteryInputSlot inputSlot;

	Container batterySlots = new SimpleContainer(2);

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {

		//Upgrade Slots
		if(this.menu.getTrader() instanceof EnergyTraderData trader)
		{
			for(int i = 0; i < trader.getUpgrades().getContainerSize(); ++i)
			{
				SimpleSlot upgradeSlot = new UpgradeInputSlot(trader.getUpgrades(), i, 176, 18 + 18 * i, trader);
				upgradeSlot.active = false;
				addSlot.apply(upgradeSlot);
				this.slots.add(upgradeSlot);
			}
		}

		//Battery Input Slot
		this.inputSlot = new BatteryInputSlot(this.batterySlots, 0, TraderMenu.SLOT_OFFSET + 8, 122);
		this.slots.add(this.inputSlot);
		addSlot.apply(this.inputSlot);
		this.inputSlot.locked = true;
		//Battery Output Slot
		SimpleSlot outputSlot = new OutputSlot(this.batterySlots, 1, TraderMenu.SLOT_OFFSET + 44, 122);
		this.slots.add(outputSlot);
		addSlot.apply(outputSlot);

		SimpleSlot.SetInactive(this.slots);

	}

	@Override
	public void onTabClose() {

		SimpleSlot.SetInactive(this.slots);
		MinecraftForge.EVENT_BUS.unregister(this);
		this.inputSlot.locked = true;

	}

	@Override
	public void onMenuClose() {
		this.menu.clearContainer(this.batterySlots);
	}

	@Override
	public void onTabOpen() {

		SimpleSlot.SetActive(this.slots);
		MinecraftForge.EVENT_BUS.register(this);
		this.inputSlot.locked = false;

	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.LevelTickEvent event)
	{
		if(event.side.isServer() && event.phase == TickEvent.Phase.START && this.menu.getTrader() instanceof EnergyTraderData trader)
		{
			if(!this.batterySlots.getItem(0).isEmpty() && this.batterySlots.getItem(1).isEmpty())
			{
				//Try to fill the energy storage with the battery, or vice-versa
				ItemStack batteryStack = this.batterySlots.getItem(0);
				ItemStack batteryOutput = trader.getEnergyHandler().batteryInteraction(batteryStack);
				if(batteryStack.getCount() > 1)
					batteryStack.shrink(1);
				else
					batteryStack = ItemStack.EMPTY;
				this.batterySlots.setItem(0, batteryStack);
				this.batterySlots.setItem(1, batteryOutput);
			}
		}
	}

	//No messages to receive. All storage interactions are done via the battery slots or the upgrade slots.
	@Override
	public void receiveMessage(LazyPacketData message) { }

}