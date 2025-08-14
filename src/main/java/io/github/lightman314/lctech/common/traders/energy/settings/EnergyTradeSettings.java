package io.github.lightman314.lctech.common.traders.energy.settings;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.trades.TradeSubNode;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyTradeSettings extends TradeSettings<EnergyTraderData> {

    public EnergyTradeSettings(EnergyTraderData trader) { super("fluid_trades", trader); }

    @Override
    @Nullable
    protected EnergyTradeData getRuleHost(int tradeIndex) {
        return tradeIndex >= 0 && tradeIndex < this.trader.getTradeCount() ? this.trader.getTrade(tradeIndex) : null;
    }

    @Override
    protected SettingsSubNode<?> createTradeNode(int tradeIndex) { return new TradeNode(this,tradeIndex); }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        data.setIntValue("trade_count",this.trader.getTradeCount());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasIntValue("trade_count"))
            this.trader.overrideTradeCount(data.getIntValue("trade_count"));
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(), data.getIntValue("trade_count")));
    }

    private static class TradeNode extends TradeSubNode<EnergyTradeData,EnergyTradeSettings>
    {

        public TradeNode(EnergyTradeSettings parent, int index) {
            super(parent, index);
        }

        @Override
        @Nullable
        protected EnergyTradeData getTrade() {
            return this.parent.getRuleHost(this.index);
        }

        @Override
        protected void saveTrade(SavedSettingData.MutableNodeAccess node, EnergyTradeData trade) {
            node.setStringValue("type",trade.getTradeDirection().toString());
            node.setCompoundValue("price",trade.getCost().save());
            node.setIntValue("energy",trade.getAmount());
        }

        @Override
        protected void loadTrade(SavedSettingData.NodeAccess node, EnergyTradeData trade, LoadContext context) {
            trade.setTradeDirection(EnumUtil.enumFromString(node.getStringValue("type"),TradeDirection.values(),TradeDirection.SALE));
            trade.setCost(MoneyValue.load(node.getCompoundValue("price")));
            trade.setAmount(node.getIntValue("energy"));
        }

        @Override
        protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
            TradeDirection type = EnumUtil.enumFromString(data.getStringValue("type"), TradeDirection.values(), TradeDirection.SALE);
            lineWriter.accept(SettingsNode.formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_TYPE.get(), LCText.GUI_TRADE_DIRECTION.get(type).get()));
            lineWriter.accept(SettingsNode.formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_PRICE.get(), MoneyValue.load(data.getCompoundValue("price")).getText()));
            lineWriter.accept(SettingsNode.formatEntry(TechText.DATA_ENTRY_TRADER_TRADE_ENERGY.get(), EnergyUtil.formatEnergyAmount(data.getIntValue("energy"))));
        }

    }

}