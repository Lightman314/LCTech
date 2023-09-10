package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class EnergyTradeNotification extends TaxableNotification {

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_trade");

	TraderCategory traderData;

	TradeDirection tradeType;
	int quantity;
	CoinValue cost = CoinValue.EMPTY;

	String customer;

	protected EnergyTradeNotification(EnergyTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) {
		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();

		this.quantity = trade.getAmount();

		this.cost = cost;

		this.customer = customer.getName(false);

	}

	public static NonNullSupplier<Notification> create(EnergyTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) { return () -> new EnergyTradeNotification(trade, cost, customer, traderData, taxesPaid); }

	public EnergyTradeNotification(CompoundTag compound) { super(); this.load(compound); }

	@Override
	public ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {

		Component boughtText = EasyText.translatable("log.shoplog." + this.tradeType.name().toLowerCase());

		return EasyText.translatable("notifications.message.energy_trade", this.customer, boughtText, EnergyUtil.formatEnergyAmount(this.quantity), this.cost.getString());

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
		this.cost = CoinValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");

	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof EnergyTradeNotification etn)
		{
			if(!etn.traderData.matches(this.traderData))
				return false;
			if(etn.tradeType != this.tradeType)
				return false;
			if(etn.quantity != this.quantity)
				return false;
			if(etn.cost.getValueNumber() != this.cost.getValueNumber())
				return false;
			if(!etn.customer.equals(this.customer))
				return false;
			//Passed all check. Allow merging.
			return this.TaxesMatch(etn);
		}
		return false;
	}

}