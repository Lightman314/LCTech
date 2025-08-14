package io.github.lightman314.lctech.common.traders.fluid.settings;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
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
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidTradeSettings extends TradeSettings<FluidTraderData> {

    public FluidTradeSettings(FluidTraderData trader) { super("fluid_trades", trader); }

    @Override
    @Nullable
    protected FluidTradeData getRuleHost(int tradeIndex) {
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
        if(context.isServerAdmin() && data.hasIntValue("trade_count"))
        {
            int newCount = data.getIntValue("trade_count");
            if(newCount < this.trader.getTradeCount())
                this.trader.overrideTradeCount(newCount);
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        lineWriter.accept(formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(), data.getIntValue("trade_count")));
    }

    private static class TradeNode extends TradeSubNode<FluidTradeData,FluidTradeSettings>
    {

        public TradeNode(FluidTradeSettings parent, int index) {
            super(parent, index);
        }

        @Override
        @Nullable
        protected FluidTradeData getTrade() {
            return this.parent.getRuleHost(this.index);
        }

        @Override
        protected void saveTrade(SavedSettingData.MutableNodeAccess node, FluidTradeData trade) {
            node.setStringValue("type",trade.getTradeDirection().toString());
            node.setCompoundValue("price",trade.getCost().save());
            node.setCompoundValue("fluid",(CompoundTag)trade.getProduct().saveOptional(this.registryAccess()));
            node.setIntValue("buckets",trade.getBucketQuantity());
        }

        @Override
        protected void loadTrade(SavedSettingData.NodeAccess node, FluidTradeData trade, LoadContext context) {
            trade.setTradeDirection(EnumUtil.enumFromString(node.getStringValue("type"),TradeDirection.values(),TradeDirection.SALE));
            trade.setCost(MoneyValue.load(node.getCompoundValue("price")));
            trade.setProduct(FluidStack.parseOptional(this.registryAccess(),node.getCompoundValue("fluid")));
            trade.setBucketQuantity(node.getIntValue("buckets"));
        }

        @Override
        protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
            TradeDirection type = EnumUtil.enumFromString(data.getStringValue("type"), TradeDirection.values(), TradeDirection.SALE);
            lineWriter.accept(SettingsNode.formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_TYPE.get(), LCText.GUI_TRADE_DIRECTION.get(type).get()));
            lineWriter.accept(SettingsNode.formatEntry(LCText.DATA_ENTRY_TRADER_TRADE_PRICE.get(), MoneyValue.load(data.getCompoundValue("price")).getText()));
            FluidStack fluid = FluidStack.parseOptional(this.registryAccess(),data.getCompoundValue("fluid"));
            int buckets = data.getIntValue("buckets");
            lineWriter.accept(TechText.DATA_ENTRY_TRADER_TRADE_FLUID.get(FluidFormatUtil.getFluidName(fluid,ChatFormatting.RESET),buckets));
        }

    }

}
