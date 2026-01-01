package io.github.lightman314.lctech.common.traders.fluid.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.gui.TechSprites;
import io.github.lightman314.lctech.client.gui.widget.button.trade.SpriteDisplayEntry;
import io.github.lightman314.lctech.common.menu.slots.FluidInputSlot;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidTradeEditTab;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
        return new DisplayData(this.trade.isSale() ? 59 : 40, 1, this.trade.isSale() ? (this.allowsDrainage(context) ? 32 : 16) : 34, 16);
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
            entries.add(SpriteDisplayEntry.of(TechSprites.DRAINABLE_ACTIVE, TechText.TOOLTIP_TRADE_DRAINABLE.getAsList()));
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

    @Override
    @Nullable
    protected List<GhostSlot<?>> collectGhostSlots(TradeContext context, @Nullable ITraderStorageMenu menu, ScreenPosition buttonPos) {
        if(menu != null && this.isValidTab(menu.currentTab()))
        {
            List<GhostSlot<?>> slots = new ArrayList<>();
            int tradeIndex = context.getTrader().indexOfTrade(this.trade);
            if(tradeIndex < 0)
                return null;
            if(this.trade.isSale())
                slots.add(GhostSlot.simpleFluid(buttonPos.offset(59,1),ConsumerForTab(menu.currentTab(),tradeIndex)));
            else
                slots.add(GhostSlot.simpleFluid(buttonPos.offset(1,1),ConsumerForTab(menu.currentTab(),tradeIndex)));
            return slots;
        }
        return null;
    }

    private boolean isValidTab(TraderStorageTab tab)
    {
        return tab instanceof BasicTradeEditTab || tab instanceof FluidTradeEditTab;
    }

    private Consumer<FluidStack> ConsumerForTab(TraderStorageTab tab,int tradeIndex)
    {
        if(tab instanceof BasicTradeEditTab basicTab)
            return fluid -> sendUpdateMessage(basicTab,tradeIndex,fluid);
        else if(tab instanceof FluidTradeEditTab editTab)
            return editTab::setFluid;
        return null;
    }

    private void sendUpdateMessage(BasicTradeEditTab tab, int tradeIndex, FluidStack fluid)
    {
        //Pretend the player clicked on the fluid display slot with an item that contains this fluid
        ItemStack item = FluidItemUtil.getFluidDisplayItem(fluid);
        if(this.trade.isSale())
            tab.SendOutputInteractionMessage(tradeIndex,0,TradeInteractionData.DUMMY,item);
        else
            tab.SendInputInteractionMessage(tradeIndex,0,TradeInteractionData.DUMMY,item);
    }

}
