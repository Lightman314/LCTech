package io.github.lightman314.lctech.common.notifications.types;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class FluidTradeNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_trade");
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	Component fluidName;
	int fluidCount;
	CoinValue cost = new CoinValue();
	
	String customer;
	
	public FluidTradeNotification(FluidTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {
		
		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.fluidName = FluidFormatUtil.getFluidName(trade.getProduct()).withStyle(Style.EMPTY);
		this.fluidCount = trade.getQuantity();
		
		this.cost = cost;
		
		this.customer = customer.lastKnownName();
		
	}
	
	public FluidTradeNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }
	
	@Override
	public Category getCategory() { return this.traderData; }
	
	@Override
	public Component getMessage() {
		
		Component boughtText = new TranslatableComponent("log.shoplog." + this.tradeType.name().toLowerCase());
		
		Component fluidText = new TranslatableComponent("log.shoplog.fluid.fluidformat", FluidFormatUtil.formatFluidAmount(this.fluidCount), this.fluidName);
		
		Component cost = new TextComponent(this.cost.getString("0"));
		
		return new TranslatableComponent("notifications.message.fluid_trade", this.customer, boughtText, fluidText, cost);
		
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		compound.putString("Fluid", Component.Serializer.toJson(this.fluidName));
		compound.putInt("FluidCount", this.fluidCount);
		this.cost.writeToNBT(compound, "Price");
		compound.putString("Customer", this.customer);
		
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		this.fluidName = Component.Serializer.fromJson(compound.getString("Fluid"));
		this.fluidCount = compound.getInt("FluidCount");
		this.cost.readFromNBT(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}
	
	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof FluidTradeNotification)
		{
			FluidTradeNotification ftn = (FluidTradeNotification)other;
			if(!ftn.traderData.matches(this.traderData))
				return false;
			if(ftn.tradeType != this.tradeType)
				return false;
			if(!ftn.fluidName.getString().equals(this.fluidName.getString()))
				return false;
			if(ftn.fluidCount != this.fluidCount)
				return false;
			if(ftn.cost.getRawValue() != this.cost.getRawValue())
				return false;
			if(!ftn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return true;
		}
		return false;
	}
	
}
