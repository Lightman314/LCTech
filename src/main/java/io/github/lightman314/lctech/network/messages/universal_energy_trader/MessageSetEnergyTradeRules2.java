package io.github.lightman314.lctech.network.messages.universal_energy_trader;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetEnergyTradeRules2 implements IMessage<MessageSetEnergyTradeRules2>{
	
	UUID traderID;
	int tradeIndex;
	List<TradeRule> rules;
	
	public MessageSetEnergyTradeRules2() {};
	
	public MessageSetEnergyTradeRules2(UUID traderID, List<TradeRule> rules)
	{
		this(traderID, rules, -1);
	}
	
	public MessageSetEnergyTradeRules2(UUID traderID, List<TradeRule> rules, int tradeIndex)
	{
		this.traderID = traderID;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageSetEnergyTradeRules2 decode(FriendlyByteBuf buffer) {
		return new MessageSetEnergyTradeRules2(buffer.readUUID(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyTradeRules2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetEnergyTradeRules2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalEnergyTraderData)
			{
				UniversalEnergyTraderData energyTrader = (UniversalEnergyTraderData)data;
				if(message.tradeIndex < 0)
				{
					energyTrader.setRules(message.rules);
					energyTrader.markRulesDirty();
				}
				else
				{
					ITradeRuleHandler trade = energyTrader.getTrade(message.tradeIndex);
					if(trade != null)
						trade.setRules(message.rules);
					energyTrader.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
