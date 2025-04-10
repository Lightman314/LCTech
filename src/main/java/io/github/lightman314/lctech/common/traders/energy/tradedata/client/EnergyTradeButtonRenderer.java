package io.github.lightman314.lctech.common.traders.energy.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnergyTradeButtonRenderer extends TradeRenderManager<EnergyTradeData> {

    public EnergyTradeButtonRenderer(EnergyTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 128; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext tradeContext) {
        if(this.trade.isSale())
            return ScreenPosition.ofOptional(36, 1);
        else
            return ScreenPosition.ofOptional(70, 1);
    }


    @Override
    public void renderAdditional(EasyWidget button, EasyGuiGraphics gui, TradeContext context) {
        //Manually render the drainable icon
        if(this.allowsDrainage(context))
        {
            gui.resetColor();
            Optional<ScreenPosition> arrowPosOptional = this.arrowPosition(context);
            arrowPosOptional.ifPresent(arrowPos -> {
                gui.pushOffsetZero();
                gui.blit(EnergyStorageClientTab.GUI_TEXTURE, button.getX() + arrowPos.x, button.getY() + arrowPos.y + 9, 36, 18, 8, 7);
                gui.popOffset();
            });
        }
    }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) {
        if(this.trade.isSale())
            return new DisplayData(1, 1, 34, 16);
        else
            return new DisplayData(1, 1, 68, 16);
    }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        if(this.trade.isSale())
            return this.lazyPriceDisplayList(context);
        else
            return this.getProductEntry();
    }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) {
        if(this.trade.isSale())
            return new DisplayData(59, 1, 68, 16);
        else
            return new DisplayData(93, 1, 34, 16);
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        if(this.trade.isSale())
            return this.getProductEntry();
        else
            return this.lazyPriceDisplayList(context);
    }

    private List<DisplayEntry> getProductEntry() { return Lists.newArrayList(DisplayEntry.of(EasyText.literal(EnergyUtil.formatEnergyAmount(this.trade.getAmount())), TextRenderUtil.TextFormatting.create().centered().middle())); }

    @Override
    public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
        if(this.allowsDrainage(context))
        {
            Optional<ScreenPosition> arrowPosOptional = this.arrowPosition(context);
            AtomicBoolean mouseOver = new AtomicBoolean(false);
            arrowPosOptional.ifPresent(arrowPos ->
                mouseOver.set(arrowPos.offset(ScreenPosition.of(0, 9)).isMouseInArea(mouseX, mouseY, 8, 8))
            );
            if(mouseOver.get())
                return TechText.TOOLTIP_TRADE_DRAINABLE.getAsList();
        }
        return null;
    }

    private boolean allowsDrainage(TradeContext context) {
        if(context.isStorageMode || !this.trade.isSale())
            return false;
        if(context.getTrader() instanceof EnergyTraderData trader)
            return trader.canDrainExternally() && trader.isPurchaseDrainMode();
        return false;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof EnergyTraderData trader)
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
        if(this.trade.isSale() && !(context.canFitEnergy(this.trade.getAmount()) || this.allowsDrainage(context)))
            alerts.add(AlertData.warn(TechText.TOOLTIP_ALERT_NO_OUTPUT));
    }

}
