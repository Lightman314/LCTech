package io.github.lightman314.lctech.network.messages.universal_fluid_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
public class MessageSetFluidTradeProduct2 implements IMessage<MessageSetFluidTradeProduct2>{
	
	UUID traderID;
	int tradeIndex;
	FluidStack fluid;
	
	public MessageSetFluidTradeProduct2() {};
	
	public MessageSetFluidTradeProduct2(UUID traderID, int tradeIndex, FluidStack fluid)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.fluid = fluid;
	}
	
	@Override
	public MessageSetFluidTradeProduct2 decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidTradeProduct2(buffer.readUUID(), buffer.readInt(), FluidStack.readFromPacket(buffer));
	}

	@Override
	public void encode(MessageSetFluidTradeProduct2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.tradeIndex);
		message.fluid.writeToPacket(buffer);
	}

	@Override
	public void handle(MessageSetFluidTradeProduct2 message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data instanceof UniversalFluidTraderData)
			{
				UniversalFluidTraderData fluidData = (UniversalFluidTraderData)data;
				fluidData.getTrade(message.tradeIndex).setProduct(message.fluid);
				fluidData.markTradesDirty();
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
