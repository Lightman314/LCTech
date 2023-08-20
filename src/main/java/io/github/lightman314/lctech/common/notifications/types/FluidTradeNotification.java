package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
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
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class FluidTradeNotification extends TaxableNotification {

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_trade");
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	Component fluidName;
	int fluidCount;
	CoinValue cost = CoinValue.EMPTY;
	
	String customer;
	
	protected FluidTradeNotification(FluidTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) {
		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.fluidName = FluidFormatUtil.getFluidName(trade.getProduct()).withStyle(Style.EMPTY);
		this.fluidCount = trade.getQuantity();
		
		this.cost = cost;
		
		this.customer = customer.getName(false);
		
	}

	public static NonNullSupplier<Notification> create(FluidTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) { return () -> new FluidTradeNotification(trade, cost, customer, traderData, taxesPaid); }
	
	public FluidTradeNotification(CompoundTag compound) { super(); this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }
	
	@Override
	public NotificationCategory getCategory() { return this.traderData; }
	
	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {
		
		Component boughtText = EasyText.translatable("log.shoplog." + this.tradeType.name().toLowerCase());
		
		Component fluidText = EasyText.translatable("log.shoplog.fluid.fluidformat", FluidFormatUtil.formatFluidAmount(this.fluidCount), this.fluidName);
		
		Component cost = this.cost.getComponent("0");
		
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
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		this.fluidName = Component.Serializer.fromJson(compound.getString("Fluid"));
		this.fluidCount = compound.getInt("FluidCount");
		this.cost = CoinValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}
	
	@Override
	protected boolean canMerge(Notification other) {
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
			if(ftn.cost.getValueNumber() != this.cost.getValueNumber())
				return false;
			if(!ftn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(ftn);
		}
		return false;
	}
	
}
