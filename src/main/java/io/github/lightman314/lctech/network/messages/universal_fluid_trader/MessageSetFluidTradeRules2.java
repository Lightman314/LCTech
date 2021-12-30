package io.github.lightman314.lctech.network.messages.universal_fluid_trader;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetFluidTradeRules2 implements IMessage<MessageSetFluidTradeRules2>{
	
	UUID traderID;
	int tradeIndex;
	List<TradeRule> rules;
	
	public MessageSetFluidTradeRules2() {};
	
	public MessageSetFluidTradeRules2(UUID traderID, List<TradeRule> rules)
	{
		this(traderID, rules, -1);
	}
	
	public MessageSetFluidTradeRules2(UUID traderID, List<TradeRule> rules, int tradeIndex)
	{
		this.traderID = traderID;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageSetFluidTradeRules2 decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidTradeRules2(buffer.readUUID(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetFluidTradeRules2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetFluidTradeRules2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalFluidTraderData)
			{
				UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)data;
				if(message.tradeIndex < 0)
				{
					fluidTrader.setRules(message.rules);
					fluidTrader.markRulesDirty();
				}
				else
				{
					ITradeRuleHandler trade = fluidTrader.getTrade(message.tradeIndex);
					if(trade != null)
						trade.setRules(message.rules);
					fluidTrader.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
