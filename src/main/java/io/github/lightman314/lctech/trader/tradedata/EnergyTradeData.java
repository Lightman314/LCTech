package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry.TextFormatting;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyTradeData extends TradeData {
	
	
	int amount = 0;
	public int getAmount() { return this.amount; }
	public void setAmount(int newAmount) { this.amount = Math.max(0, newAmount); }
	
	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection direction) { this.tradeDirection = direction; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }
	
	public EnergyTradeData() { }
	
	public boolean hasStock(IEnergyTrader trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(IEnergyTrader trader, Player player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(IEnergyTrader trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(IEnergyTrader trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(IEnergyTrader trader, Player player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(IEnergyTrader trader, PlayerReference player) {
		if(this.amount <= 0)
			return 0;
		
		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
		}
		else if(this.isPurchase())
		{
			if(this.cost.isFree())
				return 1;
			if(cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			CoinValue price = player == null ? this.cost : trader.runTradeCostEvent(player, trader.getAllTrades().indexOf(this)).getCostResult();
			return (int)(coinValue/price.getRawValue());
		}
		return 0;
	}
	public int getStock(TradeContext context) {
		if(this.amount <= 0)
			return 0;
		
		if(!context.hasTrader() || !(context.getTrader() instanceof IEnergyTrader))
			return 0;
		
		IEnergyTrader trader = (IEnergyTrader)context.getTrader();
		if(trader.getCoreSettings().isCreative())
			return 1;
		
		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
			if(this.cost.isFree())
				return 1;
			if(cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			CoinValue price = this.getCost(context);
			return (int)(coinValue/price.getRawValue());
		}
		return 0;
	}
	
	public boolean canAfford(TradeContext context) {
		if(this.isSale())
			return context.hasFunds(this.getCost(context));
		if(this.isPurchase())
			return context.hasEnergy(this.amount);
		return false;
	}
	
	public boolean hasSpace(IEnergyTrader trader)
	{
		if(this.isPurchase())
			return trader.getMaxEnergy() - trader.getTotalEnergy() >= this.amount;
		return true;
	}
	
	@Override
	public boolean isValid() {
		return super.isValid() && this.amount > 0;
	}
	
	/**
	 * Confirms that the battery stack can receive or extract the energy required to carry out this trade.
	 * Does NOT confirm that the trader has enough energy in stock, or enough space to hold the relevant energy.
	 * Returns true for sales if external purchase draining is allowed.
	 */
	/*public boolean canTransferEnergy(IEnergyTrader trader, ItemStack batteryStack)
	{
		if(!this.isValid())
			return false;
		if(this.isSale())
		{
			//if(!this.hasStock(trader) && !trader.getCoreSettings().isCreative())
			//	return false;
			if(trader.canDrainExternally() && trader.getEnergySettings().isPurchaseDrainMode())
				return true;
			//Check the battery stack for an energy handler that can hold the output amount
			if(!batteryStack.isEmpty())
			{
				AtomicBoolean passes = new AtomicBoolean(false);
				EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyHandler ->{
					passes.set(energyHandler.receiveEnergy(this.amount, true) == this.amount);
				});
				return passes.get();
			}
		}
		else if(this.isPurchase())
		{
			if(batteryStack.isEmpty())
				return false;
			if(this.amount > 0)
			{
				AtomicBoolean passes = new AtomicBoolean(false);
				EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyHandler ->{
					passes.set(energyHandler.extractEnergy(this.amount, true) == this.amount);
				});
				return passes.get();
			}
		}
		return false;
	}
	
	@Deprecated
	public ItemStack transferEnergy(IEnergyTrader trader, ItemStack batteryStack)
	{
		if(!this.canTransferEnergy(trader, batteryStack))
		{
			LCTech.LOGGER.error("Attmpted to transfer energy trade energy without confirming that you can.");
			return batteryStack;
		}
		if(this.isSale())
		{
			AtomicBoolean canFillNormally = new AtomicBoolean(false);
			EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyHandler ->{
				canFillNormally.set(energyHandler.receiveEnergy(this.amount, true) == this.amount);
			});
			if(canFillNormally.get())
			{
				EnergyActionResult result = EnergyUtil.tryFillContainer(batteryStack, trader.getEnergyHandler().getTradeExecutor(), this.amount, true);
				return result.getResult();
			}
			else if(trader.canDrainExternally() && trader.getEnergySettings().isPurchaseDrainMode())
			{
				trader.addPendingDrain(this.amount);
				return batteryStack;
			}
			else
			{
				LCTech.LOGGER.error("Flagged as being able to transfer energy for the sale, but the battery stack cannot accept the fluid, and this trader does not allow external drains.");
				return batteryStack;
			}
		}
		else if(this.isPurchase())
		{
			EnergyActionResult result = EnergyUtil.tryEmptyContainer(batteryStack, trader.getEnergyHandler().getTradeExecutor(), this.amount, true);
			return result.getResult();
		}
		else
		{
			LCTech.LOGGER.error("Energy Trade type " + this.tradeDirection.name() + " is not a valid TradeDirection for energy transfer.");
			return batteryStack;
		}
	}*/
	
	
	@Override
	public CompoundTag getAsNBT()
	{
		CompoundTag compound = super.getAsNBT();
		
		compound.putInt("Amount", this.amount);
		compound.putString("TradeType", this.tradeDirection.name());
		
		return compound;
	}
	
	@Override
	public void loadFromNBT(CompoundTag compound)
	{
		super.loadFromNBT(compound);
		
		//Load the amount
		this.amount = compound.getInt("Amount");
		//Load the trade type
		this.tradeDirection = loadTradeType(compound.getString("TradeType"));
		
	}
	
	public static TradeDirection loadTradeType(String name)
	{
		try {
			return TradeDirection.valueOf(name);
		} catch (IllegalArgumentException e) {
			LCTech.LOGGER.error("Could not load '" + name + "' as a TradeType.");
			return TradeDirection.SALE;
		}
	}
	
	public static List<EnergyTradeData> listOfSize(int tradeCount)
	{
		List<EnergyTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new EnergyTradeData());
		}
		return list;
	}
	
	public static CompoundTag WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound)
	{
		return WriteNBTList(tradeList, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static CompoundTag WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound, String tag)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < tradeList.size(); ++i)
		{
			list.add(tradeList.get(i).getAsNBT());
		}
		compound.put(tag, list);
		return compound;
	}
	
	public static List<EnergyTradeData> LoadNBTList(int tradeCount, CompoundTag compound)
	{
		return LoadNBTList(tradeCount, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static List<EnergyTradeData> LoadNBTList(int tradeCount, CompoundTag compound, String tag)
	{
		List<EnergyTradeData> tradeData = listOfSize(tradeCount);
		
		if(!compound.contains(tag))
			return tradeData;
		
		ListTag list = compound.getList(tag,  Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size() && i < tradeCount; ++i)
		{
			tradeData.get(i).loadFromNBT(list.getCompound(i));
		}
		
		return tradeData;
	}
	
	@Override
	public boolean AcceptableDifferences(TradeComparisonResult differences) {
		return false;
	}
	
	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		return new TradeComparisonResult();
	}
	
	@Override
	public List<DisplayEntry> getInputDisplays(TradeContext context) {
		if(this.isSale())
			return this.getCostEntry(context);
		else
			return this.getProductEntry();
	}
	
	@Override
	public List<DisplayEntry> getOutputDisplays(TradeContext context) {
		if(this.isSale())
			return this.getProductEntry();
		else
			return this.getCostEntry(context);
	}
	
	private List<DisplayEntry> getCostEntry(TradeContext context) {
		return Lists.newArrayList(DisplayEntry.of(this.getCost(context)));
	}
	
	private List<DisplayEntry> getProductEntry() { return Lists.newArrayList(DisplayEntry.of(new TextComponent(EnergyUtil.formatEnergyAmount(this.amount)), TextFormatting.create().centered().middle())); }
	
	@Override
	public DisplayData inputDisplayArea(TradeContext context) {
		if(this.isSale())
			return new DisplayData(1, 1, this.tradeButtonWidth(context) - 2, 16);
		else
			return new DisplayData(1, 1, this.tradeButtonWidth(context) - 2, 10);
	}
	
	@Override
	public DisplayData outputDisplayArea(TradeContext context) {
		if(this.isSale())
			return new DisplayData(1, 24, this.tradeButtonWidth(context) - 2, 10);
		else
			return new DisplayData(1, 18, this.tradeButtonWidth(context) - 2, 16);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) {
		//Manually render the arrow
		RenderSystem.setShaderTexture(0, EnergyStorageClientTab.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		Pair<Integer,Integer> position = this.alertPosition(context);
		button.blit(pose, button.x + position.getFirst(), button.y + position.getSecond(), 54, 0, 22, 18);
		
		//Manually render the drainable icon
		if(this.allowsDrainage(context))
		{
			button.blit(pose, button.x + this.tradeButtonWidth(context) - 10, button.y + position.getSecond() + 5, 36, 18, 8, 8);
		}
	}
	
	@Override
	public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
		if(this.allowsDrainage(context))
		{
			Pair<Integer,Integer> arrowPos = this.alertPosition(context);
			int width = this.tradeButtonWidth(context);
			if(mouseX >= width - 10 && mouseX < width - 2 && mouseY >= arrowPos.getSecond() + 5 && mouseY < arrowPos.getSecond() + 13)
				return Lists.newArrayList(new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable"));
		}
		return null;
	}
	
	private boolean allowsDrainage(TradeContext context) {
		if(context.isStorageMode || !this.isSale())
			return false;
		if(context.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)context.getTrader();
			return trader.canDrainExternally() && trader.getEnergySettings().isPurchaseDrainMode();
		}
		return false;
	}
	
	@Override
	public boolean hasArrow(TradeContext context) { return false; }
	
	@Override
	public Pair<Integer, Integer> arrowPosition(TradeContext context) { return alertPosition(context); }
	
	@Override
	public Pair<Integer, Integer> alertPosition(TradeContext context) {
		return Pair.of(26, this.isSale() ? 13 : 7);
	}
	
	@Override
	public List<Component> getAlerts(TradeContext context) {
		if(context.isStorageMode)
			return null;
		List<Component> alerts = new ArrayList<>();
		if(context.hasTrader() && context.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)context.getTrader();
			if(this.getStock(context) <= 0)
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.outofstock"));
			if(!this.hasSpace(trader))
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.outofspace"));
			if(!this.canAfford(context))
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.cannotafford"));
		}
		if(this.isSale() && !(context.canFitEnergy(this.amount) || this.allowsDrainage(context)))
			alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.nooutputcontainer"));
		
		this.addTradeRuleAlerts(alerts, context);
		return alerts;
	}
	
	
	
	@Override
	public int tradeButtonHeight(TradeContext context) {
		return 36;
	}
	
	@Override
	public int tradeButtonWidth(TradeContext context) {
		return 70;
	}
	
	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? -1 : 0;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}
	
	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? 0 : -1;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}
	
	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int mouseX, int mouseY, int button, ItemStack heldItem) {
		
	}
	

}
