package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class EnergyTradeNotification extends TaxableNotification {

	public static final NotificationType<EnergyTradeNotification> TYPE = new NotificationType<>(new ResourceLocation(LCTech.MODID, "energy_trade"),EnergyTradeNotification::new);
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	int quantity;
	MoneyValue cost = MoneyValue.empty();
	
	String customer;

	private EnergyTradeNotification() {}
	protected EnergyTradeNotification(EnergyTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) {
		super(taxesPaid);
		
		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.quantity = trade.getAmount();
		
		this.cost = cost;
		
		this.customer = customer.getName(false);
		
	}

	public static NonNullSupplier<Notification> create(EnergyTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new EnergyTradeNotification(trade, cost, customer, traderData, taxesPaid); }
	
	@Nonnull
	@Override
	public NotificationType<EnergyTradeNotification> getType() { return TYPE; }
	
	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.traderData; }
	
	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {
		
		Component boughtText = this.tradeType.getActionPhrase();
		
		return TechText.NOTIFICATION_TRADE_ENERGY.get(this.customer, boughtText, EnergyUtil.formatEnergyAmount(this.quantity), this.cost.getString());
		
	}
	
	@Override
	protected void saveNormal(CompoundTag compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		compound.putInt("Quantity", this.quantity);
		compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);
		
	}
	
	@Override
	protected void loadNormal(CompoundTag compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		this.quantity = compound.getInt("Quantity");
		this.cost = MoneyValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}
	
	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof EnergyTradeNotification etn)
		{
			if(!etn.traderData.matches(this.traderData))
				return false;
			if(etn.tradeType != this.tradeType)
				return false;
			if(etn.quantity != this.quantity)
				return false;
			if(!etn.cost.equals(this.cost))
				return false;
			if(!etn.customer.equals(this.customer))
				return false;
			//Passed all check. Allow merging.
			return this.TaxesMatch(etn);
		}
		return false;
	}
	
}
