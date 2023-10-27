package io.github.lightman314.lctech.common.menu.traderstorage.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid.FluidStorageClientTab;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;

public class FluidStorageTab extends TraderStorageTab{

	public FluidStorageTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new FluidStorageClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		//Upgrade Slots
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			for(int i = 0; i < trader.getUpgrades().getContainerSize(); ++i)
			{
				SimpleSlot upgradeSlot = new UpgradeInputSlot(trader.getUpgrades(), i, 176, 18 + 18 * i, trader);
				upgradeSlot.active = false;
				addSlot.apply(upgradeSlot);
				this.slots.add(upgradeSlot);
			}
		}
	}

	@Override
	public void onTabClose() {
		
		SimpleSlot.SetInactive(this.slots);
		
	}

	@Override
	public void onTabOpen() {
		
		SimpleSlot.SetActive(this.slots);
		
	}

	public void interactWithTank(int tank, boolean shiftHeld) {
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			
			if(this.menu.isClient())
			{
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("InteractWithTank", tank)
						.setBoolean("ShiftHeld", shiftHeld));
				return;
			}

			ItemStack heldStack = this.menu.getCarried();
			if(heldStack.isEmpty()) //If held stack is empty, do nothing
				return;
			
			//Try and fill the tank
			FluidActionResult result = FluidUtil.tryEmptyContainer(heldStack, trader.getStorage(), Integer.MAX_VALUE, this.menu.player, true);
			if(result.isSuccess())
			{
				//LCTech.LOGGER.info("Successfuly filled the tank with some of the held fluid.");
				
				//If creative, and the item was a bucket, don't move the items around
				if(this.menu.player.isCreative() && result.getResult().getItem() == Items.BUCKET)
				{
					if(shiftHeld)
					{
						FluidStack fluid = FluidUtil.getFluidContained(heldStack).orElse(FluidStack.EMPTY);
						FluidEntry entry = trader.getStorage().getTank(fluid);
						if(entry != null)
							entry.setAmount(trader.getTankCapacity());
					}
					trader.markStorageDirty();
					return;
				}
				trader.markStorageDirty();
				if(heldStack.getCount() > 1)
				{
					heldStack.shrink(1);
					this.menu.setCarried(heldStack);
					ItemHandlerHelper.giveItemToPlayer(this.menu.player, result.getResult());
				}
				else
				{
					this.menu.setCarried(result.getResult());
				}
			}
			else
			{
				//Failed to fill the tank, now attempt to drain it
				if(tank < 0 || tank >= trader.getStorage().getTanks())
					return;
				FluidEntry tankEntry = trader.getStorage().getContents().get(tank);
				result = FluidUtil.tryFillContainer(heldStack, tankEntry, Integer.MAX_VALUE, this.menu.player, true);
				if(result.isSuccess())
				{
					//LCTech.LOGGER.info("Successfully drained some of the tanks fluids.");
					
					//If creative, and the item was a bucket, don't move the items around
					if(this.menu.player.isCreative() && heldStack.getItem() == Items.BUCKET)
					{
						if(shiftHeld)
						{
							FluidStack fluid = FluidUtil.getFluidContained(result.getResult()).orElse(FluidStack.EMPTY);
							FluidEntry entry = trader.getStorage().getTank(fluid);
							if(entry != null)
								entry.setAmount(0);
						}
						trader.getStorage().clearInvalidTanks();
						trader.markStorageDirty();
						return;
					}
					
					trader.getStorage().clearInvalidTanks();
					trader.markStorageDirty();
					if(heldStack.getCount() > 1)
					{
						heldStack.shrink(1);
						this.menu.setCarried(heldStack);
						ItemHandlerHelper.giveItemToPlayer(this.menu.player, result.getResult());
					}
					else
					{
						this.menu.setCarried(result.getResult());
					}
				}
					
			}
		}
	}
	
	public void toggleDrainFillState(int tank, boolean drainState, boolean newValue) {
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{

			if(!trader.drainCapable())
				return;
			
			if(tank < 0 || tank >= trader.getStorage().getTanks())
				return;
			
			FluidEntry entry = trader.getStorage().getContents().get(tank);
			if(drainState)
				entry.drainable = newValue;
			else
				entry.fillable = newValue;
			trader.markStorageDirty();
			
			if(this.menu.isClient())
			{
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("ToggleDrainFillSlot", tank)
						.setBoolean("DrainState", drainState)
						.setBoolean("NewValue", newValue));
			}
		}
	}
	
	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("InteractWithTank", LazyPacketData.TYPE_INT))
		{
			this.interactWithTank(message.getInt("InteractWithTank"), message.getBoolean("ShiftHeld"));
		}
		else if(message.contains("ToggleDrainFillSlot", LazyPacketData.TYPE_INT))
		{
			int tank = message.getInt("ToggleDrainFillSlot");
			boolean drainState = message.getBoolean("DrainState");
			boolean newValue = message.getBoolean("NewValue");
			this.toggleDrainFillState(tank, drainState, newValue);
		}
	}

}