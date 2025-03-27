package io.github.lightman314.lctech.common.menu.traderinterface.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.fluid.FluidStorageClientTab;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.slot.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.core.Direction;
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

import javax.annotation.Nonnull;

public class FluidStorageTab extends TraderInterfaceTab {

	public FluidStorageTab(TraderInterfaceMenu menu) { super(menu); }
	
	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new FluidStorageClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<EasySlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	@Override
	public void onTabOpen() { EasySlot.SetActive(this.slots); }
	
	@Override
	public void onTabClose() { EasySlot.SetInactive(this.slots); }
	
	@Override
	public void addStorageMenuSlots(Function<Slot,Slot> addSlot) {
		for(int i = 0; i < this.menu.getBE().getUpgrades().getContainerSize(); ++i)
		{
			EasySlot upgradeSlot = new UpgradeInputSlot(this.menu.getBE().getUpgrades(), i, 176, 18 + 18 * i, this.menu.getBE());
			upgradeSlot.active = false;
			upgradeSlot.setListener(this::onUpgradeModified);
			addSlot.apply(upgradeSlot);
			this.slots.add(upgradeSlot);
		}
	}
	
	private void onUpgradeModified() {
		this.menu.getBE().setUpgradeSlotsDirty();
	}
	
	public void interactWithTank(int tank, boolean shiftHeld) {
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be)
		{
			
			if(this.menu.isClient())
			{
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("InteractWithTank", tank)
						.setBoolean("ShiftHeld", shiftHeld));
			}

			ItemStack heldStack = this.menu.getCarried();
			if(heldStack.isEmpty()) //If held stack is empty, do nothing
				return;
			
			//Try and fill the tank
			FluidActionResult result = FluidUtil.tryEmptyContainer(heldStack, be.getFluidBuffer(), Integer.MAX_VALUE, this.menu.player, true);
			if(result.isSuccess())
			{
				//LCTech.LOGGER.info("Successfuly filled the tank with some held fluid.");
				
				//If creative, and the item was a bucket, don't move the items around
				if(this.menu.player.isCreative() && result.getResult().getItem() == Items.BUCKET)
				{
					if(shiftHeld)
					{
						FluidStack fluid = FluidUtil.getFluidContained(heldStack).orElse(FluidStack.EMPTY);
						FluidEntry entry = be.getFluidBuffer().getTank(fluid);
						if(entry != null)
							entry.setAmount(be.getTankCapacity());
					}
					be.setFluidBufferDirty();
					return;
				}
				be.setFluidBufferDirty();
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
				if(tank < 0 || tank >= be.getFluidBuffer().getTanks())
					return;
				FluidEntry tankEntry = be.getFluidBuffer().getContents().get(tank);
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
							FluidEntry entry = be.getFluidBuffer().getTank(fluid);
							if(entry != null)
								entry.setAmount(0);
						}
						be.getFluidBuffer().clearInvalidTanks();
						be.setFluidBufferDirty();
						return;
					}
					
					be.getFluidBuffer().clearInvalidTanks();
					be.setFluidBufferDirty();
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

	public void toggleSide(Direction side, DirectionalSettingsState state) {
		if(this.menu.getBE().canAccess(this.menu.player) && this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be) {
			be.getFluidHandler().toggleSide(side,state);
			be.setHandlerDirty(be.getFluidHandler());
		}
	}

	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("InteractWithTank", LazyPacketData.TYPE_INT))
			this.interactWithTank(message.getInt("InteractWithTank"), message.getBoolean("ShiftHeld"));
	}

}
