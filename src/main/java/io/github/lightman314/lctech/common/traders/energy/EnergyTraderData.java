package io.github.lightman314.lctech.common.traders.energy;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.client.gui.settings.energy.EnergyInputAddon;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyStorageTab;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.*;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class EnergyTraderData extends InputTraderData {

	public static final int DEFAULT_TRADE_LIMIT = 8;

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.ENERGY_CAPACITY);

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"energy_trader");

	protected final TradeEnergyHandler energyHandler = new TradeEnergyHandler(this);

	public final boolean drainCapable() { return !this.showOnTerminal(); }

	public enum DrainMode { ALWAYS(0), PURCHASES_ONLY(1);

		public final int index;
		DrainMode(int index) { this.index = index; }

		public static DrainMode of(int index) {
			for(DrainMode mode : DrainMode.values())
				if(mode.index == index)
					return mode;
			return DrainMode.ALWAYS;
		}

	}

	private DrainMode drainMode = DrainMode.PURCHASES_ONLY;
	public DrainMode getDrainMode() { return this.drainMode; }
	public boolean isAlwaysDrainMode() { return !this.drainCapable() || this.drainMode == DrainMode.ALWAYS; }
	public boolean isPurchaseDrainMode() {
		if(!this.drainCapable())
			return false;
		if(this.drainMode == DrainMode.PURCHASES_ONLY)
		{
			for(Direction side : Direction.values())
			{
				if(this.allowOutputSide(side))
					return true;
			}
		}
		return false;
	}

	List<EnergyTradeData> trades = EnergyTradeData.listOfSize(1, true);

	//Energy Storage
	int energyStorage = 0;
	int pendingDrain = 0;

	public EnergyTraderData() { super(TYPE);}
	public EnergyTraderData(Level level, BlockPos pos) {
		super(TYPE, level, pos);
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

		this.saveTrades(compound);
		this.saveEnergyStorage(compound);
		this.saveDrainMode(compound);

	}

	protected final void saveTrades(CompoundTag compound) {
		EnergyTradeData.WriteNBTList(this.trades, compound);
	}

	protected final void saveDrainMode(CompoundTag compound) {
		compound.putInt("DrainMode", this.drainMode.index);
	}

	protected final void saveEnergyStorage(CompoundTag compound) {
		compound.putInt("Battery", this.energyStorage);
		compound.putInt("PendingDrain", this.pendingDrain);
	}

	public void loadAdditional(CompoundTag compound) {
		super.loadAdditional(compound);

		if(compound.contains(TradeData.DEFAULT_KEY))
			this.trades = EnergyTradeData.LoadNBTList(compound, !this.isPersistent());

		if(compound.contains("Battery"))
			this.energyStorage = Math.max(0,compound.getInt("Battery"));
		if(compound.contains("PendingDrain"))
			this.pendingDrain = Math.max(0,compound.getInt("PendingDrain"));

		if(compound.contains("DrainMode"))
			this.drainMode = DrainMode.of(compound.getInt("DrainMode"));
	}

	public int getTradeCount() { return this.trades.size(); }

	public EnergyTradeData getTrade(int tradeIndex) {
		if(tradeIndex >= 0 && tradeIndex < this.trades.size())
			return this.trades.get(tradeIndex);
		return new EnergyTradeData(false);
	}

	@Nonnull
	@Override
	public List<EnergyTradeData> getTradeData() { return new ArrayList<>(this.trades); }

	public TradeEnergyHandler getEnergyHandler() { return this.energyHandler; }

	@Override
	public boolean canEditTradeCount() { return true; }

	@Override
	public int getMaxTradeCount() { return DEFAULT_TRADE_LIMIT; }

	public void addTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
			return;

		if(this.getTradeCount() >= DEFAULT_TRADE_LIMIT && !CommandLCAdmin.isAdminPlayer(requestor))
		{
			Permissions.PermissionWarning(requestor, "add creative trade slot", Permissions.ADMIN_MODE);
			return;
		}

		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Permissions.PermissionWarning(requestor, "add trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() + 1);
	}

	public void removeTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.getTradeCount() <= 1)
			return;

		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Permissions.PermissionWarning(requestor, "remove trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() - 1);
	}

	public void overrideTradeCount(int newTradeCount)
	{
		if(this.getTradeCount() == newTradeCount)
			return;
		int tradeCount = MathUtil.clamp(newTradeCount, 1, TraderData.GLOBAL_TRADE_LIMIT);
		List<EnergyTradeData> oldTrades = this.trades;
		this.trades = EnergyTradeData.listOfSize(tradeCount, !this.isPersistent());
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); ++i)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		//Send an update to the client
		if(this.isServer())
		{
			this.markDirty(this::saveTrades);
		}
	}

	@Override
	public int getTradeStock(int tradeIndex) {
		return this.getTrade(tradeIndex).getStock(this);
	}

	public int getPendingDrain() { return Math.max(this.pendingDrain,0); }
	private void setPendingDrain(int amount) { this.pendingDrain = Math.max(amount, 0); }
	public void addPendingDrain(int amount) { this.pendingDrain += amount; }
	public void shrinkPendingDrain(int amount) { this.setPendingDrain(this.pendingDrain - amount); }

	public int getAvailableEnergy() { return this.energyStorage - this.getPendingDrain(); }
	public int getDrainableEnergy() {
		if(this.isAlwaysDrainMode())
		{
			//Cannot drain from creative traders if they're in "ALWAYS" mode
			return this.isCreative() ? 0 : this.getAvailableEnergy();
		}
		else if(this.isPurchaseDrainMode())
		{
			//Allow draining up to the purchasable amount when in purchase mode (confirm that we have the energy available if not in creative)
			return this.isCreative() ? this.pendingDrain : Math.min(this.pendingDrain, this.energyStorage);
		}
		return 0;
	}
	public int getTotalEnergy() { return this.energyStorage; }
	public static int getDefaultMaxEnergy() { return TechConfig.SERVER.energyTraderDefaultStorage.get(); }
	public int getMaxEnergy() {
		//Calculate based on the current upgrades
		int defaultCapacity = getDefaultMaxEnergy();
		int maxEnergy = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.getUpgrades().getContainerSize(); ++i)
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
						maxEnergy += addAmount;
					}
				}
			}
		}
		return maxEnergy;
	}

	public void shrinkEnergy(int amount) { this.energyStorage -= amount; }
	public void addEnergy(int amount) { this.energyStorage += amount; }

	public void markEnergyStorageDirty() { this.markDirty(this::saveEnergyStorage); }

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction relativeSide) {
		return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> this.getEnergyHandler().getExternalHandler(relativeSide)));
	}

	@Override
	public IconData inputSettingsTabIcon() { return IconData.of(IBatteryItem.HideEnergyBar(ModItems.BATTERY)); }
	@Override
	public MutableComponent inputSettingsTabTooltip() { return EasyText.translatable("tooltip.lctech.settings.energyinput"); }
	@Override @OnlyIn(Dist.CLIENT)
	public List<InputTabAddon> inputSettingsAddons() { return ImmutableList.of(EnergyInputAddon.INSTANCE); }

	@Override
	public void receiveNetworkMessage(@NotNull Player player, @NotNull CompoundTag message)
	{
		super.receiveNetworkMessage(player, message);
		if(message.contains("NewEnergyDrainMode"))
		{
			DrainMode newMode = DrainMode.of(message.getInt("NewEnergyDrainMode"));
			if(this.drainMode != newMode)
			{
				this.drainMode = newMode;
				this.markDirty(this::saveDrainMode);
			}
		}
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

		EnergyTradeData trade = this.getTrade(tradeIndex);

		if(trade == null || !trade.isValid())
			return TradeResult.FAIL_INVALID_TRADE;

		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;

		if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;

		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();

		//Abort if not enough stock
		if(!trade.hasStock(context) && !this.isCreative())
			return TradeResult.FAIL_OUT_OF_STOCK;

		if(trade.isSale())
		{

			//Confirm that the energy can be output
			if(!context.canFitEnergy(trade.getAmount()) && !(this.drainCapable() && this.hasOutputSide() && this.isPurchaseDrainMode()))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;

			//Process the trades payment
			if(!context.getPayment(price))
				return TradeResult.FAIL_CANNOT_AFFORD;

			//We have enough money, and the trade is valid. Execute the trade
			//Give the energy
			boolean drainStorage = true;
			if(context.canFitEnergy(trade.getAmount()))
				context.fillEnergy(trade.getAmount());
			else //If nowhere to give the energy, add to the pending drain
			{
				this.addPendingDrain(trade.getAmount());
				drainStorage = false;
			}

			CoinValue taxesPaid = CoinValue.EMPTY;

			//Ignore internal editing if this is creative
			if(!this.isCreative())
			{
				//Remove the purchased energy from storage
				if(drainStorage)
				{
					this.shrinkEnergy(trade.getAmount());
					this.markEnergyStorageDirty();
				}

				//Give the paid price to storage
				taxesPaid = this.addStoredMoney(price, true);
			}

			//Push the notification
			this.pushNotification(EnergyTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price, taxesPaid);

			return TradeResult.SUCCESS;

		}
		else if(trade.isPurchase())
		{
			//Abort if not enough energy to buy
			if(!context.hasEnergy(trade.getAmount()))
				return TradeResult.FAIL_CANNOT_AFFORD;

			//Abort if not enough space to put the purchased energy
			if(!trade.hasSpace(this) && !this.isCreative())
				return TradeResult.FAIL_NO_INPUT_SPACE;

			//Give the payment to the player
			if(!context.givePayment(price))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;

			//We have enough money, and the trade is valid. Execute the trade
			//Collect the product
			if(!context.drainEnergy(trade.getAmount()))
			{
				//Failed somehow. Take the money back
				context.getPayment(price);
				return TradeResult.FAIL_CANNOT_AFFORD;
			}

			CoinValue taxesPaid = CoinValue.EMPTY;

			//Ignore internal editing if this is creative
			if(!this.isCreative())
			{
				//Put the purchased fluid in storage
				this.addEnergy(trade.getAmount());
				this.markEnergyStorageDirty();
				//Remove the coins from storage
				taxesPaid = this.removeStoredMoney(price, true);
			}

			//Push the notification
			this.pushNotification(EnergyTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price, taxesPaid);

			return TradeResult.SUCCESS;

		}

		return TradeResult.FAIL_INVALID_TRADE;
	}

	@Override
	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { interactionSlots.add(EnergyInteractionSlot.INSTANCE); }

	@Override
	protected boolean allowAdditionalUpgradeType(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }

	@Override
	public boolean canMakePersistent() { return true; }

	@Override
	protected void getAdditionalContents(List<ItemStack> contents) { }

	@Override
	public IconData getIcon() { return IconData.of(IBatteryItem.HideEnergyBar(ModItems.BATTERY.get())); }

	@Override
	public boolean hasValidTrade() {
		for(EnergyTradeData trade : this.trades)
		{
			if(trade.isValid())
				return true;
		}
		return false;
	}

	@Override
	public void initStorageTabs(TraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new EnergyStorageTab(menu));
		//Energy Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new EnergyTradeEditTab(menu));
	}

	@Override
	protected void loadAdditionalFromJson(JsonObject json) throws Exception {
		if(!json.has("Trades"))
			throw new Exception("Energy Trader must have a trade list.");

		JsonArray tradeList = json.get("Trades").getAsJsonArray();
		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
		{
			try {

				JsonObject tradeData = tradeList.get(i).getAsJsonObject();
				EnergyTradeData newTrade = new EnergyTradeData(false);
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeDirection(EnergyTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Quantity
				newTrade.setAmount(tradeData.get("Quantity").getAsInt());
				//Price
				newTrade.setCost(CoinValue.Parse(tradeData.get("Price")));
				//Trade Rules
				if(tradeData.has("TradeRules"))
				{
					newTrade.setRules(TradeRule.Parse(tradeData.get("TradeRules").getAsJsonArray(), newTrade));
				}

				this.trades.add(newTrade);

			} catch(Exception e) { LCTech.LOGGER.error("Error parsing energy trade at index " + i, e); }
		}

		if(this.trades.size() == 0)
			throw new Exception("Trader has no valid trades!");

		this.energyStorage = this.getMaxEnergy();

	}

	@Override
	protected void saveAdditionalToJson(JsonObject json) {

		JsonArray trades = new JsonArray();
		for(EnergyTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				tradeData.addProperty("TradeType", trade.getTradeDirection().name());
				tradeData.add("Price", trade.getCost().toJson());
				tradeData.addProperty("Quantity", trade.getAmount());

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
		for (EnergyTradeData trade : this.trades) {
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
				EnergyTradeData trade = this.trades.get(i);
				CompoundTag ptTag = tradePersistentData.getCompound(i);
				TradeRule.loadPersistentData(ptTag, trade.getRules(), "RuleData");
			}
		}
	}

	public static List<Component> getEnergyHoverTooltip(EnergyTraderData trader)
	{
		List<Component> tooltip = Lists.newArrayList();
		tooltip.add(EasyText.literal(EnergyUtil.formatEnergyAmount(trader.getTotalEnergy()) + "/" + EnergyUtil.formatEnergyAmount(trader.getMaxEnergy())).withStyle(ChatFormatting.AQUA));
		if(trader.getPendingDrain() > 0)
		{
			tooltip.add(EasyText.translatable("gui.lctech.energytrade.pending_drain", EnergyUtil.formatEnergyAmount(trader.getPendingDrain())).withStyle(ChatFormatting.AQUA));
		}
		return tooltip;
	}

	public final boolean canDrainExternally() { return this.drainCapable() && this.hasOutputSide(); }

}