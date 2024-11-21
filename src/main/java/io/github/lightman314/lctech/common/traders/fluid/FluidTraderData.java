package io.github.lightman314.lctech.common.traders.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidStorageTab;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidTradeEditTab;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lctech.common.util.icons.FluidIcon;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.*;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveTradeNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.*;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.TradeOfferUpgrade;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTraderData extends InputTraderData implements ITraderFluidFilter, IFlexibleOfferTrader {

	public final static TraderType<FluidTraderData> TYPE = new TraderType<>(new ResourceLocation(LCTech.MODID,"fluid_trader"),FluidTraderData::new);

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);

	public final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);

	TraderFluidStorage storage = new TraderFluidStorage(this);
	public TraderFluidStorage getStorage() { return this.storage; }
	public void markStorageDirty() { this.markDirty(this::saveStorage); }

	private int baseTradeCount = 0;
	List<FluidTradeData> trades = FluidTradeData.listOfSize(1, true);

	public final boolean drainCapable() { return !this.showOnTerminal(); }

	@Override
	protected boolean allowVoidUpgrade() { return true; }

	private FluidTraderData() { super(TYPE); }
	public FluidTraderData(int tradeCount, Level level, BlockPos pos) {
		super(TYPE, level, pos);
		this.trades = FluidTradeData.listOfSize(tradeCount, true);
		this.baseTradeCount = tradeCount;
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		super.loadAdditional(compound);

		if(compound.contains(TradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(compound, !this.isPersistent());

		if(compound.contains("FluidStorage"))
			this.storage.load(compound, "FluidStorage");

		if(compound.contains("BaseTradeCount"))
			this.baseTradeCount = compound.getInt("BaseTradeCount");
		if(this.baseTradeCount <= 0) //Reset base trade count to current trade count if the current value is invalid
			this.baseTradeCount = this.trades.size();

	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

		this.saveTrades(compound);
		this.saveStorage(compound);

	}

	protected final void saveTrades(CompoundTag compound)
	{
		compound.putInt("BaseTradeCount", this.baseTradeCount);
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
		if(this.isClient())
			return;
		if(LCAdminMode.isAdminPlayer(requestor) && this.baseTradeCount < TraderData.GLOBAL_TRADE_LIMIT)
		{

			this.baseTradeCount++;
			this.refactorTrades();

			this.pushLocalNotification(new AddRemoveTradeNotification(PlayerReference.of(requestor), true, this.getTradeCount()));

		}
		else
			Permissions.PermissionWarning(requestor, "add a trade slot", Permissions.ADMIN_MODE);
	}

	public void removeTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(LCAdminMode.isAdminPlayer(requestor) && this.baseTradeCount > 1)
		{

			this.baseTradeCount--;
			this.refactorTrades();

			this.pushLocalNotification(new AddRemoveTradeNotification(PlayerReference.of(requestor), false, this.getTradeCount()));

		}
		else
			Permissions.PermissionWarning(requestor, "remove a trade slot", Permissions.ADMIN_MODE);
	}

	@Override
	public void refactorTrades() {
		int newCount = MathUtil.clamp(this.baseTradeCount + TradeOfferUpgrade.getBonusTrades(this.getUpgrades()), 1, TraderData.GLOBAL_TRADE_LIMIT);
		if(newCount != this.trades.size())
			this.overrideTradeCount(newCount);
	}

	public void overrideTradeCount(int newTradeCount)
	{
		int tradeCount = MathUtil.clamp(newTradeCount, 1, TraderData.GLOBAL_TRADE_LIMIT);
		if(this.trades.size() == tradeCount)
			return;
		List<FluidTradeData> oldTrades = this.trades;
		this.trades = FluidTradeData.listOfSize(tradeCount, !this.isPersistent());
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		if(this.isServer())
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
		for(FluidTradeData trade : this.getTradeData())
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

	public static int getDefaultTankCapacity() { return TechConfig.SERVER.fluidTraderDefaultStorage.get() * FluidType.BUCKET_VOLUME; }

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
						int addAmount = UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
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
		return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> this.getFluidHandler().getExternalHandler(relativeSide)));
	}

	@Override
	public IconData inputSettingsTabIcon() { return IconData.of(Items.WATER_BUCKET); }
	@Override
	public MutableComponent inputSettingsTabTooltip() { return TechText.TOOLTIP_SETTINGS_INPUT_FLUID.get(); }

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

		FluidTradeData trade = this.getTrade(tradeIndex);
		if(trade == null || !trade.isValid())
			return TradeResult.FAIL_INVALID_TRADE;

		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;

		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(trade, context).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;

		//Get the cost of the trade
		//Update get the price from TradeData#getCost as it will avoid doing any unecessary calculations.
		MoneyValue price = trade.getCost(context);

		//Abort if not enough stock
		if(!trade.hasStock(context) && !this.isCreative())
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

			MoneyValue taxesPaid = MoneyValue.empty();

			//Give the paid price to storage
			if(this.canStoreMoney())
				taxesPaid = this.addStoredMoney(price,true);

			//Ignore internal editing if this is creative
			if(!this.isCreative())
			{
				//Remove the purchased fluid from storage
				if(drainTank)
				{
					this.getStorage().drain(trade.productOfQuantity());
					this.markStorageDirty();
				}
			}

			//Update stats
			this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID, taxesPaid);

			//Post the notification
			this.pushNotification(FluidTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

			//Push the post-trade event
			this.runPostTradeEvent(trade, context, price, taxesPaid);

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

			MoneyValue taxesPaid = MoneyValue.empty();

			//Put the purchased fluid in storage
			if(this.shouldStoreGoods())
			{
				//Put the purchased fluid in storage
				this.getStorage().forceFillTank(trade.productOfQuantity());
				this.markStorageDirty();
			}

			//Remove the coins from storage
			if(!this.isCreative())
				taxesPaid = this.removeStoredMoney(price, true);

			this.incrementStat(StatKeys.Traders.MONEY_PAID, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID, taxesPaid);

			//Post the notification
			this.pushNotification(FluidTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

			//Push the post-trade event
			this.runPostTradeEvent(trade, context, price, taxesPaid);

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
	public void initStorageTabs(ITraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new FluidStorageTab(menu));
		//Fluid Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new FluidTradeEditTab(menu));
	}

	@Override
	protected void loadAdditionalFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException {

		if(!json.has("Trades"))
			throw new JsonSyntaxException("Fluid Trader must have a trade list.");

		JsonArray tradeList = GsonHelper.getAsJsonArray(json,"Trades");

		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
		{
			try {

				JsonObject tradeData = tradeList.get(i).getAsJsonObject();

				FluidTradeData newTrade = new FluidTradeData(false);

				//Product
				JsonObject product = GsonHelper.getAsJsonObject(tradeData, "Product");
				newTrade.setProduct(FluidItemUtil.parseFluidStack(product));
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeDirection(FluidTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Price
				newTrade.setCost(MoneyValue.loadFromJson(tradeData.get("Price")));
				//Quantity
				if(tradeData.has("Quantity"))
					newTrade.setBucketQuantity(GsonHelper.getAsInt(tradeData, "Quantity"));
				//Trade Rules
				if(tradeData.has("TradeRules"))
					newTrade.setRules(TradeRule.Parse(GsonHelper.getAsJsonArray(tradeData, "TradeRules"), newTrade));

				this.trades.add(newTrade);

			} catch(Exception e) { LCTech.LOGGER.error("Error parsing fluid trade at index {}", i, e); }
		}

		if(this.trades.isEmpty())
			throw new JsonSyntaxException("Trader has no valid trades!");

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

				if(!trade.getRules().isEmpty())
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

	@Override
	protected void appendTerminalInfo(@Nonnull List<Component> list, @Nullable Player player) {
		int tradeCount = 0;
		int outOfStock = 0;
		for(FluidTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				++tradeCount;
				if(!this.isCreative() && !trade.hasStock(this))
					++outOfStock;
			}
		}
		list.add(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT.get(tradeCount));
		if(outOfStock > 0)
			list.add(LCText.TOOLTIP_NETWORK_TERMINAL_OUT_OF_STOCK_COUNT.get(outOfStock));
	}

	@Nonnull
	@Override
	public IconData getIconForItem(@Nonnull ItemStack stack) {

		FluidStack fluid;
		fluid = findFluid(stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM));
		if(fluid == null)
			fluid = findFluid(stack.getCapability(ForgeCapabilities.FLUID_HANDLER));
		if(fluid != null && !fluid.isEmpty())
		{
			fluid.setAmount(FluidType.BUCKET_VOLUME);
			FluidIcon newIcon = FluidIcon.of(fluid);
			//If already a fluid icon, and we attempt to set the icon as the same fluid, use the default icon instead
			if(this.getCustomIcon() instanceof FluidIcon fi && newIcon.matches(fi))
				return super.getIconForItem(stack);
			return newIcon;
		}

		return super.getIconForItem(stack);
	}

	@Nullable
	private static FluidStack findFluid(@Nonnull LazyOptional<? extends IFluidHandler> optional)
	{
		AtomicReference<FluidStack> result = new AtomicReference<>(null);
		optional.ifPresent(handler -> {
			for(int i = 0; i < handler.getTanks() && result.get() == null; ++i)
			{
				FluidStack contents = handler.getFluidInTank(i);
				if(!contents.isEmpty())
					result.set(contents.copy());
			}
		});
		return result.get();
	}

}
