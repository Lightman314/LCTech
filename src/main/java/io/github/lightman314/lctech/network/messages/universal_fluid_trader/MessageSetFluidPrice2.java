package io.github.lightman314.lctech.network.messages.universal_fluid_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetFluidPrice2 implements IMessage<MessageSetFluidPrice2>{
	
	UUID traderID;
	int tradeIndex;
	CoinValue price;
	TradeDirection tradeType;
	int quantity;
	
	public MessageSetFluidPrice2() {};
	
	public MessageSetFluidPrice2(UUID traderID, int tradeIndex, CoinValue price, TradeDirection tradeType, int quantity)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.price = price;
		this.tradeType = tradeType;
		this.quantity = quantity;
	}
	
	@Override
	public MessageSetFluidPrice2 decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidPrice2(buffer.readUUID(), buffer.readInt(), new CoinValue(buffer.readNbt()), FluidTradeData.loadTradeType(buffer.readUtf()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetFluidPrice2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.price.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeUtf(message.tradeType.name());
		buffer.writeInt(message.quantity);
	}

	@Override
	public void handle(MessageSetFluidPrice2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalFluidTraderData)
			{
				UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)data;
				FluidTradeData trade = fluidTrader.getTrade(message.tradeIndex);
				
				trade.setCost(message.price);
				trade.setTradeDirection(message.tradeType);
				trade.setBucketQuantity(message.quantity);
				trade.setDrainableExternally(false);
				trade.setFillableExternally(false);
				
				fluidTrader.markTradesDirty();
				
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
