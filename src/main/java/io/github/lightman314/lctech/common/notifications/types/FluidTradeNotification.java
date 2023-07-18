package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class FluidTradeNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_trade");
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	Component fluidName;
	int fluidCount;
	CoinValue cost = CoinValue.EMPTY;
	
	String customer;
	
	public FluidTradeNotification(FluidTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {
		
		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.fluidName = FluidFormatUtil.getFluidName(trade.getProduct()).withStyle(Style.EMPTY);
		this.fluidCount = trade.getQuantity();
		
		this.cost = cost;
		
		this.customer = customer.getName(false);
		
	}
	
	public FluidTradeNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }
	
	@Override
	public NotificationCategory getCategory() { return this.traderData; }
	
	@Override
	public MutableComponent getMessage() {
		
		Component boughtText = new TranslatableComponent("log.shoplog." + this.tradeType.name().toLowerCase());
		
		Component fluidText = new TranslatableComponent("log.shoplog.fluid.fluidformat", FluidFormatUtil.formatFluidAmount(this.fluidCount), this.fluidName);
		
		Component cost = this.cost.getComponent("0");
		
		return new TranslatableComponent("notifications.message.fluid_trade", this.customer, boughtText, fluidText, cost);
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		compound.putString("Fluid", Component.Serializer.toJson(this.fluidName));
		compound.putInt("FluidCount", this.fluidCount);
		compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);
		
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		
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
			return true;
		}
		return false;
	}
	
}