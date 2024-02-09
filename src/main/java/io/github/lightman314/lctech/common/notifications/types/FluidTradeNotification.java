package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class FluidTradeNotification extends TaxableNotification {

	public static final NotificationType<FluidTradeNotification> TYPE = new NotificationType<>(new ResourceLocation(LCTech.MODID, "fluid_trade"),FluidTradeNotification::new);

	TraderCategory traderData;

	TradeData.TradeDirection tradeType;
	Component fluidName;
	int fluidCount;
	MoneyValue cost = MoneyValue.empty();

	String customer;

	private FluidTradeNotification() {}
	protected FluidTradeNotification(FluidTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) {
		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();

		this.fluidName = FluidFormatUtil.getFluidName(trade.getProduct()).withStyle(Style.EMPTY);
		this.fluidCount = trade.getQuantity();

		this.cost = cost;

		this.customer = customer.getName(false);

	}

	public static NonNullSupplier<Notification> create(FluidTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new FluidTradeNotification(trade, cost, customer, traderData, taxesPaid); }

	public FluidTradeNotification(CompoundTag compound) { super(); this.load(compound); }

	@Nonnull
	@Override
	protected NotificationType<FluidTradeNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {

		Component boughtText = EasyText.translatable("log.shoplog." + this.tradeType.name().toLowerCase());

		Component fluidText = EasyText.translatable("log.shoplog.fluid.fluidformat", FluidFormatUtil.formatFluidAmount(this.fluidCount), this.fluidName);

		Component cost = this.cost.getText("0");

		return EasyText.translatable("notifications.message.fluid_trade", this.customer, boughtText, fluidText, cost);

	}

	@Override
	protected void saveNormal(CompoundTag compound) {

		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		compound.putString("Fluid", Component.Serializer.toJson(this.fluidName));
		compound.putInt("FluidCount", this.fluidCount);
		compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);

	}

	@Override
	protected void loadNormal(CompoundTag compound) {

		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = TradeData.TradeDirection.fromIndex(compound.getInt("TradeType"));
		this.fluidName = Component.Serializer.fromJson(compound.getString("Fluid"));
		this.fluidCount = compound.getInt("FluidCount");
		this.cost = MoneyValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");

	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof FluidTradeNotification ftn)
		{
			if(!ftn.traderData.matches(this.traderData))
				return false;
			if(ftn.tradeType != this.tradeType)
				return false;
			if(!ftn.fluidName.getString().equals(this.fluidName.getString()))
				return false;
			if(ftn.fluidCount != this.fluidCount)
				return false;
			if(ftn.cost.equals(this.cost))
				return false;
			if(!ftn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(ftn);
		}
		return false;
	}

}