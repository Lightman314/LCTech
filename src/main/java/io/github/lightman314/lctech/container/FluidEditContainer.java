package io.github.lightman314.lctech.container;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditClose;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditSet;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidEditContainer extends Container{

	private static final List<Fluid> BLACKLISTED_FLUIDS = Lists.newArrayList(Fluids.EMPTY);
	public static final void BlacklistFluid(Fluid fluid) { if(!BLACKLISTED_FLUIDS.contains(fluid)) BLACKLISTED_FLUIDS.add(fluid); }
	
	public static final int columnCount = 9;
	public static final int rowCount = 6;
	
	private static List<Fluid> allFluids = null;
	
	public final PlayerEntity player;
	private final Supplier<IFluidTrader> traderSource;
	public IFluidTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	public final int tradeIndex;
	public final FluidTradeData getTrade() { return this.getTrader().getTrade(this.tradeIndex); }
	
	List<Fluid> searchResultFluids;
	IInventory displayInventory;
	
	private String searchString;
	private int page;
	public int getPage() { return this.page; }
	
	final List<Slot> tradeSlots;
	
	protected boolean isClient() { return this.player.world.isRemote; }
	
	public FluidEditContainer(int windowId, PlayerInventory inventory, BlockPos traderPos, int tradeIndex)
	{
		this(ModContainers.FLUID_EDIT, windowId, inventory, tradeIndex, IFluidTrader.TileEntitySource(inventory.player.world, traderPos));
	}
	
	protected FluidEditContainer(ContainerType<?> type, int windowId, PlayerInventory inventory, int tradeIndex, Supplier<IFluidTrader> traderSource)
	{
		super(type, windowId);
		
		this.player = inventory.player;
		this.tradeIndex = tradeIndex;
		this.traderSource = traderSource;
		this.tradeSlots = Lists.newArrayList();
		
		this.displayInventory = new Inventory(columnCount * rowCount);
		
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
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player)
	{
		if(!this.isClient()) //Only function on client, as the server will be desynchronized
			return ItemStack.EMPTY;
		
		if(slotId >= 0 && slotId < inventorySlots.size())
		{
			Slot slot = inventorySlots.get(slotId);
			if(slot == null)
				return ItemStack.EMPTY;
			
			ItemStack stack = slot.getStack();
			//Define the item
			if(!stack.isEmpty())
				this.setFluid(stack);
			
		}
		return ItemStack.EMPTY;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected static void initFluidList() {
		if(allFluids != null)
			return;
		
		allFluids = Lists.newArrayList();
		
		ForgeRegistries.FLUIDS.forEach(fluid ->{
			if(!BLACKLISTED_FLUIDS.contains(fluid) && fluid.isSource(fluid.getDefaultState()))
				allFluids.add(fluid);
		});
		
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index) { return ItemStack.EMPTY; }
	
	@Override
	public boolean canInteractWith(PlayerEntity player) { return this.getTrader() != null && this.getTrader().hasPermission(player, Permissions.EDIT_TRADES); }
	
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
		return (this.searchResultFluids.size() - 1) / this.displayInventory.getSizeInventory();
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
		for(int i = 0; i < this.displayInventory.getSizeInventory(); i++)
		{
			int thisIndex = startIndex + i;
			if(thisIndex < this.searchResultFluids.size()) //Set to search result item
			{
				ItemStack stack = FluidItemUtil.getFluidDispayItem(this.searchResultFluids.get(thisIndex));
				this.displayInventory.setInventorySlotContents(i, stack);
			}
			else
				this.displayInventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}
	
	public void setFluid(ItemStack stack)
	{
		FluidUtil.getFluidContained(stack).ifPresent(fluidStack ->{
			if(isClient())
			{
				//Send message to server
				this.getTrade().setProduct(fluidStack);
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
	
	public static class UniversalFluidEditContainer extends FluidEditContainer
	{
		public UniversalFluidEditContainer(int windowID, PlayerInventory inventory, UUID traderID, int tradeIndex) {
			super(ModContainers.UNIVERSAL_FLUID_EDIT, windowID, inventory, tradeIndex, IFluidTrader.UniversalSource(inventory.player.world, traderID));
		}
	}
	
}
