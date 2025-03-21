package io.github.lightman314.lctech.common.menu.traderstorage.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.common.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.slot.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nonnull;

public class EnergyStorageTab extends TraderStorageTab {

	public EnergyStorageTab(@Nonnull ITraderStorageMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new EnergyStorageClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<EasySlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	BatteryInputSlot drainSlot;
	BatteryInputSlot fillSlot;

	Container batterySlots = new SimpleContainer(2);
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		
		//Upgrade Slots
		if(this.menu.getTrader() instanceof EnergyTraderData trader)
		{
			for(int i = 0; i < trader.getUpgrades().getContainerSize(); ++i)
			{
				EasySlot upgradeSlot = new UpgradeInputSlot(trader.getUpgrades(), i, 176, 18 + 18 * i, trader);
				upgradeSlot.active = false;
				addSlot.apply(upgradeSlot);
				this.slots.add(upgradeSlot);
			}
		}
		
		//Battery Input Slot
		this.drainSlot = new BatteryInputSlot(this.batterySlots, 0, TraderMenu.SLOT_OFFSET + 8, 122);
		this.drainSlot.requireEnergy = true;
		this.slots.add(this.drainSlot);
		addSlot.apply(this.drainSlot);
		this.drainSlot.locked = true;
		//Battery Output Slot
		this.fillSlot = new BatteryInputSlot(this.batterySlots, 1, TraderMenu.SLOT_OFFSET + 44, 122);
		this.slots.add(this.fillSlot);
		addSlot.apply(this.fillSlot);
		this.fillSlot.locked = true;

		EasySlot.SetInactive(this.slots);
		
	}

	@Override
	public void onTabClose() {

		EasySlot.SetInactive(this.slots);
		NeoForge.EVENT_BUS.unregister(this);
		this.drainSlot.locked = true;
		this.fillSlot.locked = true;
		
	}
	
	@Override
	public void onMenuClose() {
		this.menu.clearContainer(this.batterySlots);
	}

	@Override
	public void onTabOpen() {

		EasySlot.SetActive(this.slots);
		NeoForge.EVENT_BUS.register(this);
		this.drainSlot.locked = false;
		this.fillSlot.locked = false;
		
	}
	
	@SubscribeEvent
	public void onWorldTick(LevelTickEvent.Pre event)
	{
		if(!event.getLevel().isClientSide && this.menu.getTrader() instanceof EnergyTraderData trader)
		{
			//Drain from slot 1
			if(!this.batterySlots.getItem(0).isEmpty())
			{
				ItemStack batteryStack = this.batterySlots.getItem(0);
				if(batteryStack.getCount() == 1)
				{
					EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyStorage -> {
						//Get extractable amount
						int extractedAmount = energyStorage.extractEnergy(trader.getMaxEnergy() - trader.getTotalEnergy(), false);
						if(extractedAmount > 0)
							trader.addEnergy(extractedAmount);
					});
				}
			}
			//Store into slot 2
			if(!this.batterySlots.getItem(1).isEmpty())
			{
				ItemStack batteryStack = this.batterySlots.getItem(1);
				if(batteryStack.getCount() == 1)
				{
					EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyStorage -> {
						int drainedAmount = energyStorage.receiveEnergy(trader.getAvailableEnergy(), false);
						if(drainedAmount > 0)
							trader.shrinkEnergy(drainedAmount);
					});
				}
			}
		}
	}
	
	//No messages to receive. All storage interactions are done via the battery slots or the upgrade slots.
	@Override
	public void receiveMessage(@Nonnull LazyPacketData lazyPacketData) { }

}
