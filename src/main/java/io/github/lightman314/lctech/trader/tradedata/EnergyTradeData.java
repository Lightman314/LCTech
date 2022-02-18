package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.EnergyUtil.EnergyActionResult;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
	public boolean canTransferEnergy(IEnergyTrader trader, ItemStack batteryStack)
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
	}
	
	
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
	

}
