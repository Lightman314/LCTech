package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid.FluidStorageClientTab;
import io.github.lightman314.lctech.client.gui.widget.button.trade.SpriteDisplayEntry;
import io.github.lightman314.lctech.menu.slots.FluidInputSlot;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidTradeData extends TradeData {
	
	//Tank is stored locally, as each fluid-type being sold is stored independently
	/*FluidStack tank = FluidStack.EMPTY;
	public FluidStack getTankContents() { return tank.copy(); }
	public int getDrainableAmount() { return tank.getAmount() - this.pendingDrain; }
	public boolean validTankContents() { return this.tank.isEmpty() || this.tank.isFluidEqual(this.product); }
	public void setTankContents(FluidStack tank) { this.tank = tank.copy(); }
	public double getTankFillPercent() { return MathUtil.clamp((double)tank.getAmount() / (double)this.getTankCapacity(), 0d, 1d); }*/
	
	FluidStack product = FluidStack.EMPTY;
	public FluidStack getProduct() { return product.copy(); }
	public void setProduct(FluidStack newProduct) {
		this.product = newProduct.copy();
		if(this.product.getFluid() != Fluids.EMPTY)
			this.product.setAmount(FluidAttributes.BUCKET_VOLUME);
	}
	public FluidStack productOfQuantity()
	{
		FluidStack stack = this.product.copy();
		if(!stack.isEmpty())
			stack.setAmount(this.getQuantity());
		return stack;
	}
	
	int bucketQuantity = 1;
	public int getQuantity() { return this.bucketQuantity * FluidAttributes.BUCKET_VOLUME; }
	public int getBucketQuantity() { return this.bucketQuantity; }
	public void setBucketQuantity(int value) { this.bucketQuantity = MathUtil.clamp(value, 1, this.getMaxBucketQuantity()); }
	public int getMaxBucketQuantity() { return Math.max(1, TechConfig.SERVER.fluidTradeMaxQuantity.get()); }
	
	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection type) { this.tradeDirection = type; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }
	
	public ItemStack getFilledBucket() { return FluidUtil.getFilledBucket(this.product);}
	
	public FluidTradeData() { }
	
	public boolean hasStock(IFluidTrader trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(IFluidTrader trader, Player player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(IFluidTrader trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(IFluidTrader trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(IFluidTrader trader, Player player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(IFluidTrader trader, PlayerReference player)
	{
		if(this.product.isEmpty())
			return 0;
		
		if(this.isSale())
		{
			return trader.getStorage().getAvailableFluidCount(this.product) / this.getQuantity();
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
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
		if(this.product.isEmpty())
			return 0;
		
		if(!context.hasTrader() || !(context.getTrader() instanceof IFluidTrader))
			return 0;
		
		IFluidTrader trader = (IFluidTrader)context.getTrader();
		if(trader.getCoreSettings().isCreative())
			return 1;
		
		if(this.isSale())
		{
			return trader.getStorage().getAvailableFluidCount(this.product) / this.getQuantity();
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
			return context.hasFluid(this.productOfQuantity());
		return false;
	}
	
	public boolean hasSpace(IFluidTrader trader)
	{
		if(this.isPurchase())
			return trader.getStorage().getFillableAmount(this.product) >= this.getQuantity();
		return true;
	}
	
	//Flag as not valid should the tank & product not match
	public boolean isValid() { return super.isValid() && !this.product.isEmpty(); }
	
	@Override
	public CompoundTag getAsNBT()
	{
		CompoundTag compound = super.getAsNBT();
		
		compound.put("Trade", this.product.writeToNBT(new CompoundTag()));
		compound.putInt("Quantity", this.bucketQuantity);
		//compound.putInt("PendingDrain", this.pendingDrain);
		compound.putString("TradeType", this.tradeDirection.name());
		
		return compound;
	}
	
	@Override
	public void loadFromNBT(CompoundTag compound)
	{
		super.loadFromNBT(compound);
		//Load the product
		this.product = FluidStack.loadFluidStackFromNBT(compound.getCompound("Trade"));
		//Load the quantity
		if(compound.contains("Quantity", Tag.TAG_INT))
			this.bucketQuantity = compound.getInt("Quantity");
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
	
	public static List<FluidTradeData> listOfSize(int tradeCount)
	{
		List<FluidTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new FluidTradeData());
		}
		return list;
	}
	
	public static CompoundTag WriteNBTList(List<FluidTradeData> tradeList, CompoundTag compound)
	{
		return WriteNBTList(tradeList, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static CompoundTag WriteNBTList(List<FluidTradeData> tradeList, CompoundTag compound, String tag)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < tradeList.size(); i++)
		{
			list.add(tradeList.get(i).getAsNBT());
			//LCTech.LOGGER.info("Wrote to NBT List: \n" + tradeList.get(i).getAsNBT().toString());
		}
		compound.put(tag, list);
		return compound;
	}
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, CompoundTag compound)
	{
		return LoadNBTList(tradeCount, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, CompoundTag compound, String tag)
	{
		List<FluidTradeData> tradeData = listOfSize(tradeCount);
		
		if(!compound.contains(tag))
			return tradeData;
		
		ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size() && i < tradeCount; i++)
		{
			tradeData.get(i).loadFromNBT(list.getCompound(i));
			//LCTech.LOGGER.info("Loaded from NBT List:\n" + list.getCompound(i).toString());
			//LCTech.LOGGER.info("Loaded value writes as:\n" + tradeData.get(i).getAsNBT());
		}
		
		return tradeData;
	}
	
	public static FluidTradeData loadData(CompoundTag compound) {
		FluidTradeData trade = new FluidTradeData();
		trade.loadFromNBT(compound);
		return trade;
	}
	
	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof FluidTradeData)
		{
			FluidTradeData otherFluidTrade = (FluidTradeData)otherTrade;
			//Flag as compatible
			result.setCompatible();
			//Compare product
			result.addProductResult(ProductComparisonResult.CompareFluid(this.productOfQuantity(), otherFluidTrade.productOfQuantity()));
			//Compare prices
			result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
			//Compare types
			result.setTypeResult(this.tradeDirection == otherFluidTrade.tradeDirection);
		}
		//Return the comparison results
		return result;
	}
	
	@Override
	public boolean AcceptableDifferences(TradeComparisonResult result) {
		//Confirm the types match
		if(!result.TypeMatches() || !result.isCompatible())
			return false;
		//Comfirm the product is acceptable
		if(result.getProductResultCount() <= 0)
			return false;
		//Product result
		ProductComparisonResult productResult = result.getProductResult(0);
		if(productResult.SameProductType() && productResult.SameProductNBT())
		{
			if(this.isSale())
			{
				//Product should be greater than or equal to pass
				if(productResult.ProductQuantityDifference() > 0)
					return false;
			}
			else if(this.isPurchase())
			{
				//Purchase product should be less than or equal to pass
				if(productResult.ProductQuantityDifference() < 0)
					return false;
			}
		}
		else //Fluid & tag don't match. Failure.
			return false;
		//Product is acceptable, now check the price
		if(this.isSale() && result.isPriceExpensive())
			return false;
		if(this.isPurchase() && result.isPriceCheaper())
			return false;
		
		//Product, price, and types are all acceptable
		return true;
	}
	
	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) {
		List<Component> list = new ArrayList<>();
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).withStyle(ChatFormatting.RED));
			else //Cheaper
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).withStyle(ChatFormatting.RED));
		}
		if(differences.getProductResultCount() > 0)
		{
			Component directionName = this.isSale() ? new TranslatableComponent("gui.lctech.interface.difference.product.sale") : new TranslatableComponent("gui.lctech.interface.difference.product.purchase");
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(new TranslatableComponent("gui.lctech.interface.fluid.difference.fluidtype", directionName).withStyle(ChatFormatting.RED));
			else
			{
				if(!productCheck.SameProductNBT())
					list.add(new TranslatableComponent("gui.lctech.interface.fluid.difference.fluidnbt"));
				else if(!productCheck.SameProductQuantity())
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					if(quantityDifference < 0) //More items
						list.add(new TranslatableComponent("gui.lctech.interface.fluid.difference.quantity.more", directionName, FluidFormatUtil.formatFluidAmount(-quantityDifference)).withStyle(ChatFormatting.RED));
					else //Less items
						list.add(new TranslatableComponent("gui.lctech.interface.fluid.difference.quantity.less", directionName, FluidFormatUtil.formatFluidAmount(quantityDifference)).withStyle(ChatFormatting.RED));
				}
			}
		}
		
		return list;
	}
	
	@Override
	public List<DisplayEntry> getInputDisplays(TradeContext context) {
		if(this.isSale())
			return Lists.newArrayList(DisplayEntry.of(this.getCost(context), context.isStorageMode ? Lists.newArrayList(new TranslatableComponent("tooltip.lightmanscurrency.trader.price_edit")) : null));
		if(this.isPurchase())
			return this.getFluidEntry(context);
		return null;
	}
	
	
	
	@Override
	public List<DisplayEntry> getOutputDisplays(TradeContext context) {
		if(this.isSale())
			return this.getFluidEntry(context);
		if(this.isPurchase())
			return Lists.newArrayList(DisplayEntry.of(this.getCost(context), context.isStorageMode ? Lists.newArrayList(new TranslatableComponent("tooltip.lightmanscurrency.trader.price_edit")) : null));
		return null;
	}
	
	private List<DisplayEntry> getFluidEntry(TradeContext context) {
		List<DisplayEntry> entries = new ArrayList<>();
		if(!this.product.isEmpty())
			entries.add(DisplayEntry.of(this.getFilledBucket(), this.bucketQuantity, getFluidTooltip(context)));
		else if(context.isStorageMode)
			entries.add(DisplayEntry.of(FluidInputSlot.BACKGROUND, Lists.newArrayList(new TranslatableComponent("tooltip.lctech.trader.fluid_edit"))));
		//Add drainage entry if draining is allowed
		if(this.allowsDrainage(context))
			entries.add(SpriteDisplayEntry.of(FluidStorageClientTab.GUI_TEXTURE, 0, 0, 8, 8, Lists.newArrayList(new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable"))));
		return entries;
	}
	
	private List<Component> getFluidTooltip(TradeContext context) {
		if(product.isEmpty())
			return null;
		
		List<Component> tooltips = Lists.newArrayList();
		
		//Fluid Name
		tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.tooltip." + getTradeDirection().name().toLowerCase(), FluidFormatUtil.getFluidName(product, ChatFormatting.GOLD)));
		//Quantity
		tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.tooltip.quantity", getBucketQuantity(), FluidFormatUtil.formatFluidAmount(this.getQuantity())).withStyle(ChatFormatting.GOLD));
		//Stock
		if(context.hasTrader())
		{
			tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.stock", context.getTrader().getCoreSettings().isCreative() ? new TranslatableComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new TextComponent("§6" + this.getStock(context))));
		}
		
		
		return tooltips;
	}
	
	@Override
	public int tradeButtonWidth(TradeContext context) { return this.allowsDrainage(context) ? 87 : 76; }
	
	@Override
	public int tradeButtonHeight(TradeContext context) { return 18; }
	
	@Override
	public DisplayData inputDisplayArea(TradeContext context) {
		return new DisplayData(1, 1, this.isSale() ? 34 : 16, 16);
	}
	
	@Override
	public DisplayData outputDisplayArea(TradeContext context) {
		return new DisplayData(this.isSale() ? 58 : 40, 1, this.isSale() ? (this.allowsDrainage(context) ? 32 : 16) : 34, 16);
	}
	
	private boolean allowsDrainage(TradeContext context) {
		if(context.isStorageMode || !this.isSale())
			return false;
		if(context.getTrader() instanceof IFluidTrader)
		{
			IFluidTrader trader = (IFluidTrader)context.getTrader();
			return trader.drainCapable() && trader.getFluidSettings().hasOutputSide() && trader.getStorage().isDrainable(this.product);
		}
		return false;
	}
	
	@Override
	public Pair<Integer, Integer> arrowPosition(TradeContext context) {
		return Pair.of(this.isSale() ? 36 : 18, 1);
	}
	
	@Override
	public Pair<Integer, Integer> alertPosition(TradeContext context) {
		return this.arrowPosition(context);
	}
	
	@Override
	public List<Component> getAlerts(TradeContext context) {
		if(context.isStorageMode)
			return null;
		List<Component> alerts = new ArrayList<>();
		if(context.hasTrader() && context.getTrader() instanceof IFluidTrader)
		{
			IFluidTrader trader = (IFluidTrader)context.getTrader();
			if(this.getStock(context) <= 0)
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.outofstock"));
			if(!this.hasSpace(trader))
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.outofspace"));
			if(!this.canAfford(context))
				alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.cannotafford"));
			
		}
		if(this.isSale() && !(context.canFitFluid(this.productOfQuantity()) || this.allowsDrainage(context)))
			alerts.add(new TranslatableComponent("tooltip.lightmanscurrency.nooutputcontainer"));
		
		this.addTradeRuleAlerts(alerts, context);
		return alerts;
	}
	
	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IFluidTrader)
		{
			IFluidTrader trader = (IFluidTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
			if(this.isPurchase())
			{
				//Set the fluid to the held fluid
				if(heldItem.isEmpty() && this.product.isEmpty())
				{
					//Open fluid edit
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", 0);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				else
				{
					FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(null);
					if(heldFluid != null)
					{
						this.setProduct(heldFluid);
						trader.markTradesDirty();
						trader.getStorage().refactorTanks();
						trader.markStorageDirty();
					}
					tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
				}
			}
		}
	}
	
	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IFluidTrader)
		{
			IFluidTrader trader = (IFluidTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				//Set the fluid to the held fluid
				if(heldItem.isEmpty() && this.product.isEmpty())
				{
					//Open fluid edit
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", 0);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				else
				{
					FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(null);
					if(heldFluid != null)
					{
						this.setProduct(heldFluid);
						trader.markTradesDirty();
						trader.getStorage().refactorTanks();
						trader.markStorageDirty();
					}
					tab.sendOutputInteractionMessage(tradeIndex, index, button, heldItem);
				}
			}
			else if(this.isPurchase())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
		}
	}
	
	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {
		//No need to open the trade edit tab when clicking on a non-interaction slot, as there will always be a price button you can click.
		//Also no toggling of drain/fillable state, as that is done from the storage screen directly.
	}
	
}
