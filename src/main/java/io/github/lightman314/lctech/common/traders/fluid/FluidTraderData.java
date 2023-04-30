package io.github.lightman314.lctech.common.traders.fluid;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidStorageTab;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidTradeEditTab;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TextNotification;
import io.github.lightman314.lightmanscurrency.common.traders.*;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

public class FluidTraderData extends InputTraderData implements ITraderFluidFilter, ITradeSource<FluidTradeData> {

	public final static ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"fluid_trader");
	
	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);
	
	public final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);
	
	TraderFluidStorage storage = new TraderFluidStorage(this);
	public TraderFluidStorage getStorage() { return this.storage; }
	public void markStorageDirty() { this.markDirty(this::saveStorage); }
	
	List<FluidTradeData> trades = FluidTradeData.listOfSize(1, true);
	
	public final boolean drainCapable() { return !this.showOnTerminal(); }
	
	public FluidTraderData() { super(TYPE); }
	public FluidTraderData(int tradeCount, Level level, BlockPos pos) {
		super(TYPE, level, pos);
		this.trades = FluidTradeData.listOfSize(tradeCount, true);
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		super.loadAdditional(compound);
		
		if(compound.contains(TradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(compound, !this.isPersistent());
		
		if(compound.contains("FluidStorage"))
			this.storage.load(compound, "FluidStorage");
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		this.saveTrades(compound);
		this.saveStorage(compound);
		
	}
	
	protected final void saveTrades(CompoundTag compound)
	{
		FluidTradeData.WriteNBTList(this.trades, compound);
	}
	
	protected final void saveStorage(CompoundTag compound)
	{
		this.storage.save(compound, "FluidStorage");
	}
	
	@Override
	public int getTradeCount() { return this.trades.size(); }
	
	@Override
	public void addTrade(Player requestor) {
		if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
			return;
		if(!CommandLCAdmin.isAdminPlayer(requestor))
		{
			Permissions.PermissionWarning(requestor, "add trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() + 1);
	}
	
	@Override
	public void removeTrade(Player requestor) {
		if(this.getTradeCount() <= 1)
			return;
		if(!CommandLCAdmin.isAdminPlayer(requestor))
		{
			Permissions.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() - 1);
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.getTradeCount() == newTradeCount)
			return;
		int tradeCount = MathUtil.clamp(newTradeCount, 1, TraderData.GLOBAL_TRADE_LIMIT);
		List<FluidTradeData> oldTrades = this.trades;
		this.trades = FluidTradeData.listOfSize(tradeCount, !this.isPersistent());
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		this.markTradesDirty();
	}
	
	public FluidTradeData getTrade(int tradeIndex) {
		if(tradeIndex >= 0 && tradeIndex < this.trades.size())
			return this.trades.get(tradeIndex);
		return new FluidTradeData(false);
	}
	
	@Override
	public int getTradeStock(int index) { return this.getTrade(index).getStock(this); }

	@Nonnull
	@Override
	public List<FluidTradeData> getTradeData() { return new ArrayList<>(this.trades); }
	
	public TradeFluidHandler getFluidHandler() { return this.fluidHandler; }
	
	@Override
	public List<FluidStack> getRelevantFluids() {
		List<FluidStack> result = new ArrayList<>();
		for(FluidTradeData trade : this.trades)
		{
			FluidStack product = trade.getProduct();
			if(!product.isEmpty() && !this.isInList(result, product))
				result.add(product);
		}
		return result;
	}
	
	private boolean isInList(List<FluidStack> list, FluidStack fluid)
	{
		if(fluid.isEmpty())
			return true;
		for(FluidStack query : list)
		{
			if(query.isFluidEqual(fluid))
				return true;
		}
		return false;
	}
	
	public static int getDefaultTankCapacity() { return TechConfig.SERVER.fluidTraderDefaultStorage.get() * FluidAttributes.BUCKET_VOLUME; }
	
	@Override
	public int getTankCapacity() {
		int defaultCapacity = getDefaultTankCapacity();
		int tankCapacity = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.getUpgrades().getContainerSize(); i++)
		{
			ItemStack stack = this.getUpgrades().getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						int addAmount = upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
						if(addAmount > defaultCapacity && !baseStorageCompensation)
						{
							addAmount -= defaultCapacity;
							baseStorageCompensation = true;
						}
						tankCapacity += addAmount;
					}
				}	
			}
		}
		return tankCapacity;
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction relativeSide) {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this.getFluidHandler().getExternalHandler(relativeSide)));
	}

	@Override
	public IconData inputSettingsTabIcon() { return IconData.of(Items.WATER_BUCKET); }
	@Override
	public MutableComponent inputSettingsTabTooltip() { return new TranslatableComponent("tooltip.lctech.settings.fluidinput"); }
	
	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		
		FluidTradeData trade = this.getTrade(tradeIndex);
		if(trade == null || !trade.isValid())
			return TradeResult.FAIL_INVALID_TRADE;
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();
		
		//Abort if not enough stock
		if(!trade.hasStock(this, context.getPlayerReference()) && !this.isCreative())
			return TradeResult.FAIL_OUT_OF_STOCK;
		
		if(trade.isSale())
		{
			
			FluidEntry tankEntry = this.getStorage().getTank(trade.getProduct());
			
			//Abort if the purchased fluid cannot be given
			if(!context.canFitFluid(trade.productOfQuantity()) && !(this.hasOutputSide() && tankEntry != null && tankEntry.drainable))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//Process the trades payment
			if(!context.getPayment(price))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Give the product
			boolean drainTank = true;
			if(context.canFitFluid(trade.productOfQuantity()))
				context.fillFluid(trade.productOfQuantity());
			else //If nowhere to put the product, add to the pending drain.
			{
				drainTank = false;
				tankEntry.addPendingDrain(trade.getQuantity());
				this.markStorageDirty();
			}
			
			//Post the notification
			this.pushNotification(() -> new FluidTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.isCreative())
			{
				//Remove the purchased fluid from storage
				if(drainTank)
				{
					this.getStorage().drain(trade.productOfQuantity());
					this.markStorageDirty();
				}
				//Give the paid price to storage
				this.addStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
		else if(trade.isPurchase())
		{
			
			//Abort if not enough fluid to buy
			if(!context.hasFluid(trade.productOfQuantity()))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//Abort if not enough space to put the purchased fluid
			if(!trade.hasSpace(this) && !this.isCreative())
				return TradeResult.FAIL_NO_INPUT_SPACE;
			
			//Give the payment to the player
			if(!context.givePayment(price))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Collect the product
			if(!context.drainFluid(trade.productOfQuantity()))
			{
				//Failed somehow. Take the money back
				context.getPayment(price);
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Post the notification
			this.pushNotification(() -> new FluidTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.isCreative())
			{
				//Put the purchased fluid in storage
				this.getStorage().forceFillTank(trade.productOfQuantity());
				this.markStorageDirty();
				//Remove the coins from storage
				this.removeStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}
	
	@Override
	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { interactionSlots.add(FluidInteractionSlot.INSTANCE); }
	
	@Override
	protected boolean allowAdditionalUpgradeType(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }
	
	@Override
	public boolean canMakePersistent() { return true; }
	
	@Override
	protected void getAdditionalContents(List<ItemStack> results) {
		
		for(FluidEntry entry : this.storage.getContents())
		{
			if(!entry.getTankContents().isEmpty())
				results.add(FluidShardItem.GetFluidShard(entry.getTankContents()));
		}
	}
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WATER_BUCKET); }
	
	@Override
	public boolean hasValidTrade() {
		for(FluidTradeData trade : this.trades)
		{
			if(trade.isValid())
				return true;
		}
		return false;
	}
	
	@Override
	public void initStorageTabs(TraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new FluidStorageTab(menu));
		//Fluid Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new FluidTradeEditTab(menu));
	}
	
	@Override
	protected void loadAdditionalFromJson(JsonObject json) throws Exception {
		
		if(!json.has("Trades"))
			throw new Exception("Fluid Trader must have a trade list.");
		
		JsonArray tradeList = json.get("Trades").getAsJsonArray();
		
		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
		{
			try {
				
				JsonObject tradeData = tradeList.get(i).getAsJsonObject();
				
				FluidTradeData newTrade = new FluidTradeData(false);
				
				//Product
				JsonObject product = tradeData.get("Product").getAsJsonObject();
				newTrade.setProduct(FluidItemUtil.parseFluidStack(product));
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeDirection(FluidTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Price
				newTrade.setCost(CoinValue.Parse(tradeData.get("Price")));
				//Quantity
				if(tradeData.has("Quantity"))
					newTrade.setBucketQuantity(tradeData.get("Quantity").getAsInt());
				//Trade Rules
				if(tradeData.has("TradeRules"))
				{
					newTrade.setRules(TradeRule.Parse(tradeData.get("TradeRules").getAsJsonArray(), newTrade));
				}
				
				this.trades.add(newTrade);
				
			} catch(Exception e) { LCTech.LOGGER.error("Error parsing fluid trade at index " + i, e); }
		}
		
		if(this.trades.size() == 0)
			throw new Exception("Trader has no valid trades!");
		
	}
	
	@Override
	protected void saveAdditionalToJson(JsonObject json) {
		JsonArray trades = new JsonArray();
		for(FluidTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				
				tradeData.addProperty("TradeType", trade.getTradeDirection().name());
				tradeData.add("Price", trade.getCost().toJson());
				tradeData.add("Product", FluidItemUtil.convertFluidStack(trade.getProduct()));
				tradeData.addProperty("Quantity", trade.getBucketQuantity());
				
				if(trade.getRules().size() > 0)
					tradeData.add("TradeRules", TradeRule.saveRulesToJson(trade.getRules()));
				
				trades.add(tradeData);
			}
		}
		json.add("Trades", trades);
	}
	
	@Override
	protected void saveAdditionalPersistentData(CompoundTag compound) {
		ListTag tradePersistentData = new ListTag();
		boolean tradesAreRelevant = false;
		for (FluidTradeData trade : this.trades) {
			CompoundTag ptTag = new CompoundTag();
			if (TradeRule.savePersistentData(ptTag, trade.getRules(), "RuleData"))
				tradesAreRelevant = true;
			tradePersistentData.add(ptTag);
		}
		if(tradesAreRelevant)
			compound.put("PersistentTradeData", tradePersistentData);
	}
	
	@Override
	protected void loadAdditionalPersistentData(CompoundTag compound) {
		if(compound.contains("PersistentTradeData"))
		{
			ListTag tradePersistentData = compound.getList("PersistentTradeData", Tag.TAG_COMPOUND);
			for(int i = 0; i < tradePersistentData.size() && i < this.trades.size(); ++i)
			{
				FluidTradeData trade = this.trades.get(i);
				CompoundTag ptTag = tradePersistentData.getCompound(i);
				TradeRule.loadPersistentData(ptTag, trade.getRules(), "RuleData");
			}
		}
	}
	
	@Override @Deprecated
	protected void loadExtraOldUniversalTraderData(CompoundTag compound) {
		if(compound.contains(TradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(compound, true);
		
		if(compound.contains("UpgradeInventory"))
			this.loadOldUpgradeData(InventoryUtil.loadAllItems("UpgradeInventory", compound, 5));
		
		if(compound.contains("FluidStorage"))
			this.storage.load(compound, "FluidStorage");
		else if(compound.contains(TradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.storage.loadFromTrades(compound.getList(TradeData.DEFAULT_KEY, Tag.TAG_COMPOUND));
		
		if(compound.contains("FluidSettings"))
		{
			CompoundTag fs = compound.getCompound("FluidSettings");
			if(fs.contains("InputSides"))
				this.loadOldInputSides(fs.getCompound("InputSides"));
			if(fs.contains("OutputSides"))
				this.loadOldOutputSides(fs.getCompound("OutputSides"));
		}
		
		if(compound.contains("TradeRules", Tag.TAG_LIST))
			this.loadOldTradeRuleData(TradeRule.loadRules(compound, "TradeRules"));
		
		if(compound.contains("FluidShopHistory"))
		{
			ListTag list = compound.getList("FluidShopHistory", Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				String jsonText = list.getCompound(i).getString("value");
				MutableComponent text = Component.Serializer.fromJson(jsonText);
				if(text != null)
					this.pushLocalNotification(new TextNotification(text));
			}
		}
		
	}
	
	@Override @Deprecated
	protected void loadExtraOldBlockEntityData(CompoundTag compound) { this.loadExtraOldUniversalTraderData(compound); }

}