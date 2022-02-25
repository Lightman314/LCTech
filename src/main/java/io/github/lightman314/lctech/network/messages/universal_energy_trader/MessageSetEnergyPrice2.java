package io.github.lightman314.lctech.network.messages.universal_energy_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetEnergyPrice2 implements IMessage<MessageSetEnergyPrice2>{
	
	UUID traderID;
	int tradeIndex;
	CoinValue price;
	TradeDirection tradeType;
	int amount;
	
	public MessageSetEnergyPrice2() {};
	
	public MessageSetEnergyPrice2(UUID traderID, int tradeIndex, CoinValue price, TradeDirection tradeType, int amount)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.price = price;
		this.tradeType = tradeType;
		this.amount = amount;
	}
	
	@Override
	public MessageSetEnergyPrice2 decode(PacketBuffer buffer) {
		return new MessageSetEnergyPrice2(buffer.readUniqueId(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), EnergyTradeData.loadTradeType(buffer.readString(100)), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyPrice2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.price.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeString(message.tradeType.name());
		buffer.writeInt(message.amount);
		
	}

	@Override
	public void handle(MessageSetEnergyPrice2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalEnergyTraderData)
			{
				UniversalEnergyTraderData energyTrader = (UniversalEnergyTraderData)data;
				EnergyTradeData trade = energyTrader.getTrade(message.tradeIndex);
				
				trade.setCost(message.price);
				trade.setTradeDirection(message.tradeType);
				trade.setAmount(message.amount);
				
				energyTrader.markTradesDirty();
				
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
