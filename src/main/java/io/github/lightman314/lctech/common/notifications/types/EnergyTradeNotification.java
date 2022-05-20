package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class EnergyTradeNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_trade");
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	int quantity;
	CoinValue cost = new CoinValue();
	
	String customer;
	
	public EnergyTradeNotification(EnergyTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {
		
		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.quantity = trade.getAmount();
		
		this.cost = cost;
		
		this.customer = customer.lastKnownName();
		
	}
	
	public EnergyTradeNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public Category getCategory() { return this.traderData; }
	
	@Override
	public Component getMessage() {
		
		Component boughtText = new TranslatableComponent("log.shoplog." + this.tradeType.name().toLowerCase());
		
		return new TranslatableComponent("notifications.message.energy_trade", this.customer, boughtText, EnergyUtil.formatEnergyAmount(this.quantity), this.cost.getString());
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		compound.putInt("Quantity", this.quantity);
		this.cost.writeToNBT(compound, "Price");
		compound.putString("Customer", this.customer);
		
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		this.quantity = compound.getInt("Quantity");
		this.cost.readFromNBT(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}
	
	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof EnergyTradeNotification)
		{
			EnergyTradeNotification etn = (EnergyTradeNotification)other;
			if(!etn.traderData.matches(this.traderData))
				return false;
			if(etn.tradeType != this.tradeType)
				return false;
			if(etn.quantity != this.quantity)
				return false;
			if(etn.cost.getRawValue() != this.cost.getRawValue())
				return false;
			if(!etn.customer.equals(this.customer))
				return false;
			//Passed all check. Allow merging.
			return true;
		}
		return false;
	}
	
}
