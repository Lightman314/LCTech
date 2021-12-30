package io.github.lightman314.lctech.network.messages.universal_fluid_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageToggleFluidIcon2 implements IMessage<MessageToggleFluidIcon2>{
	
	UUID traderID;
	int tradeIndex;
	int icon;
	
	public MessageToggleFluidIcon2() {};
	
	public MessageToggleFluidIcon2(UUID traderID, int tradeIndex, int icon)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.icon = icon;
	}
	
	@Override
	public MessageToggleFluidIcon2 decode(FriendlyByteBuf buffer) {
		return new MessageToggleFluidIcon2(buffer.readUUID(), buffer.readInt(), buffer.readInt());
	}

	@Override
	public void encode(MessageToggleFluidIcon2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeInt(message.icon);
	}

	@Override
	public void handle(MessageToggleFluidIcon2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalFluidTraderData)
			{
				UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)data;
				FluidTradeData trade = fluidTrader.getTrade(message.tradeIndex);
				switch(message.icon)
				{
				case 1:
					trade.setFillable(!trade.canFill());
					break;
				}
				fluidTrader.markTradesDirty();
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
