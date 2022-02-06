package io.github.lightman314.lctech.menu;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditClose;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditSet;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidEditMenu extends AbstractContainerMenu{

	private static final List<Fluid> BLACKLISTED_FLUIDS = Lists.newArrayList(Fluids.EMPTY);
	public static final void BlacklistFluid(Fluid fluid) { if(!BLACKLISTED_FLUIDS.contains(fluid)) BLACKLISTED_FLUIDS.add(fluid); }
	
	public static final int columnCount = 9;
	public static final int rowCount = 6;
	
	private static List<Fluid> allFluids = null;
	
	public final Player player;
	public final Supplier<IFluidTrader> traderSource;
	public final int tradeIndex;
	public final FluidTradeData tradeData;
	
	List<Fluid> searchResultFluids;
	Container displayInventory;
	
	private String searchString;
	private int page;
	public int getPage() { return this.page; }
	
	final List<Slot> tradeSlots;
	
	protected boolean isClient() { return this.player.level.isClientSide; }
	
	public FluidEditMenu(int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource, int tradeIndex)
	{
		this(ModMenus.FLUID_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
	protected FluidEditMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource, int tradeIndex, FluidTradeData tradeData)
	{
		super(type, windowId);
		
		this.player = inventory.player;
		this.tradeData = tradeData;
		this.tradeIndex = tradeIndex;
		this.traderSource = traderSource;
		this.tradeSlots = Lists.newArrayList();
		
		this.displayInventory = new SimpleContainer(columnCount * rowCount);
		
		if(!this.isClient())
			return;
		
		//Display Slots
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount; x++)
			{
				this.addSlot(new Slot(this.displayInventory, x + y * columnCount, 8 + x * 18, 18 + y * 18));
			}
		}
		
		//Load the fluid list from the registry
		initFluidList();
		
		//Set the search to the default value to initialize the inventory
		this.modifySearch("");
		
	}
	
	@Override
	public void clicked(int slotId, int dragType, ClickType clickType, Player player)
	{
		if(!this.isClient()) //Only function on client, as the server will be desynchronized
			return;
		
		if(slotId >= 0 && slotId < this.slots.size())
		{
			Slot slot = this.slots.get(slotId);
			if(slot == null)
				return;
			
			ItemStack stack = slot.getItem();
			//Define the item
			if(!stack.isEmpty())
				this.setFluid(stack);
			
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	protected static void initFluidList() {
		if(allFluids != null)
			return;
		
		allFluids = Lists.newArrayList();
		
		ForgeRegistries.FLUIDS.forEach(fluid ->{
			if(!BLACKLISTED_FLUIDS.contains(fluid) && fluid.isSource(fluid.defaultFluidState()))
				allFluids.add(fluid);
		});
		
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index) { return ItemStack.EMPTY; }
	
	@Override
	public boolean stillValid(Player player) { return this.traderSource.get().hasPermission(player, Permissions.EDIT_TRADES); }
	
	public void modifySearch(String newSearch) {
		this.searchString = newSearch.toLowerCase();
		
		//Repopulate the searchResultItems list
		if(this.searchString.length() > 0)
		{
			//Search the display name
			this.searchResultFluids = Lists.newArrayList();
			for(Fluid fluid : allFluids)
			{
				//Search the fluid name
				if(fluid.getAttributes().getDisplayName(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME)).getString().toLowerCase().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
				//Search the registry name
				else if(fluid.getRegistryName().toString().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
			}
		}
		else //No search string, so the result is just the allFluids list
			this.searchResultFluids = allFluids;
		
		this.refreshPage();
	}
	
	public int maxPage() {
		return (this.searchResultFluids.size() - 1) / this.displayInventory.getContainerSize();
	}
	
	public void modifyPage(int deltaPage) {
		this.page += deltaPage;
		refreshPage();
	}
	
	public void refreshPage() {
		if(this.page < 0)
			this.page = 0;
		if(this.page > maxPage())
			this.page = maxPage();
		
		int startIndex = this.page * columnCount * rowCount;
		//Define the display inventories contents
		for(int i = 0; i < this.displayInventory.getContainerSize(); i++)
		{
			int thisIndex = startIndex + i;
			if(thisIndex < this.searchResultFluids.size()) //Set to search result item
			{
				ItemStack stack = FluidItemUtil.getFluidDispayItem(this.searchResultFluids.get(thisIndex));
				this.displayInventory.setItem(i, stack);
			}
			else
				this.displayInventory.setItem(i, ItemStack.EMPTY);
		}
	}
	
	public void setFluid(ItemStack stack)
	{
		FluidUtil.getFluidContained(stack).ifPresent(fluidStack ->{
			if(isClient())
			{
				//Send message to server
				this.tradeData.setProduct(fluidStack);
				LCTechPacketHandler.instance.sendToServer(new MessageFluidEditSet(stack));
			}
			else
			{
				//Set the trade
				this.traderSource.get().getTrade(this.tradeIndex).setProduct(fluidStack);
				this.traderSource.get().markTradesDirty();
			}
		});
	}
	
	public void openTraderStorage() {
		if(isClient()) {
			LCTechPacketHandler.instance.sendToServer(new MessageFluidEditClose());
		}
		else {
			this.traderSource.get().openStorageMenu(this.player);
		}
	}
	
}
