package io.github.lightman314.lctech.common.traders.fluid.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid.FluidStorageClientTab;
import io.github.lightman314.lctech.client.gui.widget.button.trade.SpriteDisplayEntry;
import io.github.lightman314.lctech.common.menu.slots.FluidInputSlot;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidTradeButtonRenderer extends TradeRenderManager<FluidTradeData> {

    public FluidTradeButtonRenderer(FluidTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return this.allowsDrainage(context) ? 87 : 76; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext tradeContext) {
        if (this.trade.isSale())
            return ScreenPosition.ofOptional(36, 1);
        else
            return ScreenPosition.ofOptional(18, 1);
    }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) {
        return new DisplayData(1, 1, this.trade.isSale() ? 34 : 16, 16);
    }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        if(this.trade.isSale())
            return this.lazyPriceDisplayList(context);
        if(this.trade.isPurchase())
            return this.getFluidEntry(context);
        return null;
    }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) {
        return new DisplayData(this.trade.isSale() ? 58 : 40, 1, this.trade.isSale() ? (this.allowsDrainage(context) ? 32 : 16) : 34, 16);
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        if(this.trade.isSale())
            return this.getFluidEntry(context);
        if(this.trade.isPurchase())
            return this.lazyPriceDisplayList(context);
        return null;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof FluidTraderData trader)
        {
            if(!trader.isCreative())
            {
                if(this.trade.getStock(context) <= 0)
                    alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_STOCK));
                if(!this.trade.hasSpace(trader))
                    alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_SPACE));
            }
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));
        }
        if(this.trade.isSale() && !(context.canFitFluid(this.trade.productOfQuantity()) || this.allowsDrainage(context)))
            alerts.add(AlertData.warn(TechText.TOOLTIP_ALERT_NO_OUTPUT));
    }

    private List<DisplayEntry> getFluidEntry(TradeContext context) {
        List<DisplayEntry> entries = new ArrayList<>();
        if(!this.trade.getProduct().isEmpty())
            entries.add(DisplayEntry.of(this.trade.getFilledBucket(), this.trade.getBucketQuantity(), getFluidTooltip(context)));
        else if(context.isStorageMode)
            entries.add(DisplayEntry.of(FluidInputSlot.BACKGROUND, TechText.TOOLTIP_TRADER_FLUID_EDIT.getAsList()));
        //Add drainage entry if draining is allowed
        if(this.allowsDrainage(context))
            entries.add(SpriteDisplayEntry.of(FluidStorageClientTab.GUI_TEXTURE, 0, 0, 8, 8, TechText.TOOLTIP_TRADE_DRAINABLE.getAsList()));
        return entries;
    }

    private List<Component> getFluidTooltip(TradeContext context) {
        if(this.trade.getProduct().isEmpty())
            return null;

        List<Component> tooltips = Lists.newArrayList();

        //Fluid Name
        tooltips.add((trade.isSale() ? TechText.TOOLTIP_TRADE_INFO_SELLING : TechText.TOOLTIP_TRADE_INFO_PURCHASING).get(FluidFormatUtil.getFluidName(this.trade.getProduct(), ChatFormatting.GOLD)).withStyle(ChatFormatting.GOLD));
        //Quantity
        tooltips.add(TechText.TOOLTIP_TRADE_INFO_FLUID_QUANTITY.get(this.trade.getBucketQuantity(), FluidFormatUtil.formatFluidAmount(this.trade.getQuantity())).withStyle(ChatFormatting.GOLD));
        //Stock
        if(context.hasTrader())
        {
            tooltips.add(this.getStockTooltip(context.getTrader().isCreative(), this.trade.getStock(context)));
        }


        return tooltips;
    }

    private boolean allowsDrainage(TradeContext context) {
        if(context.isStorageMode || !this.trade.isSale())
            return false;
        if(context.getTrader() instanceof FluidTraderData trader)
            return trader.drainCapable() && trader.hasOutputSide() && trader.getStorage().isDrainable(this.trade.getProduct());
        return false;
    }


}
