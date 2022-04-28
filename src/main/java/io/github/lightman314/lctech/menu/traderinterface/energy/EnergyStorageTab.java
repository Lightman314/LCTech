package io.github.lightman314.lctech.menu.traderinterface.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.menu.util.MenuUtil;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.EnergyUtil.EnergyActionResult;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

public class EnergyStorageTab extends TraderInterfaceTab{

	public EnergyStorageTab(TraderInterfaceMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new EnergyStorageClientTab(screen, this); }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	BatteryInputSlot inputSlot;
	Container batterySlots = new SimpleContainer(2);
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	@Override
	public void onTabOpen() {
		SimpleSlot.SetActive(this.slots);
		MinecraftForge.EVENT_BUS.register(this);
		this.inputSlot.locked = false;
	}
	
	@Override
	public void onTabClose() {
		SimpleSlot.SetInactive(this.slots);
		MinecraftForge.EVENT_BUS.unregister(this);
		this.inputSlot.locked = true;
	}
	
	@Override
	public void addStorageMenuSlots(Function<Slot,Slot> addSlot) {
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity)
		{
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			for(int i = 0; i < be.getUpgradeInventory().getContainerSize(); ++i)
			{
				SimpleSlot upgradeSlot = new UpgradeInputSlot(be.getUpgradeInventory(), i, 176, 18 + 18 * i, be, this::onUpgradeModified);
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
	
	private void onUpgradeModified() {
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity) {
			((EnergyTraderInterfaceBlockEntity)this.menu.getBE()).setUpgradesDirty();
		}
	}
	
	@Override
	public void onMenuClose() {
		MenuUtil.clearContainer(this.menu.player, this.batterySlots);
	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side.isServer() && event.phase == TickEvent.Phase.START && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity)
		{
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			if(!this.batterySlots.getItem(0).isEmpty() && this.batterySlots.getItem(1).isEmpty())
			{
				//Try to fill the energy storage with the battery, or vice-versa
				ItemStack batteryStack = this.batterySlots.getItem(0);
				ItemStack batteryOutput = batteryInteraction(be, batteryStack);
				if(batteryStack.getCount() > 1)
					batteryStack.shrink(1);
				else
					batteryStack = ItemStack.EMPTY;
				this.batterySlots.setItem(0, batteryStack);
				this.batterySlots.setItem(1, batteryOutput);
			}
		}
	}
	
	private ItemStack batteryInteraction(EnergyTraderInterfaceBlockEntity be, ItemStack batteryStack)
	{
		EnergyActionResult result = EnergyUtil.tryEmptyContainer(batteryStack, be.getEnergyHandler().tradeHandler, Integer.MAX_VALUE, true);
		if(result.success())
		{
			//Don't need to mark dirty, as the addEnergy/drainEnergy functions mark it dirty automatically
			return result.getResult();
		}
		else
		{
			result = EnergyUtil.tryFillContainer(batteryStack, be.getEnergyHandler().tradeHandler, Integer.MAX_VALUE, true);
			if(result.success())
			{
				return result.getResult();
			}
		}
		return batteryStack;
	}
	
	public void toggleInputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity) {
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			be.getEnergyHandler().toggleInputSide(side);
			be.setHandlerDirty(be.getEnergyHandler());
		}
	}
	
	public void toggleOutputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity) {
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			be.getEnergyHandler().toggleOutputSide(side);
			be.setHandlerDirty(be.getEnergyHandler());
		}
	}
	
	//No messages to receive. All storage interactions are done via the battery slots or the upgrade slots.
	@Override
	public void receiveMessage(CompoundTag message) { }
	
}
