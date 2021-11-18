package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.container.FluidTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageFluidEditOpen implements IMessage<MessageFluidEditOpen>{
	
	int tradeIndex;
	
	public MessageFluidEditOpen() {};
	
	public MessageFluidEditOpen(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageFluidEditOpen decode(PacketBuffer buffer) {
		return new MessageFluidEditOpen(buffer.readInt());
	}

	@Override
	public void encode(MessageFluidEditOpen message, PacketBuffer buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageFluidEditOpen message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof FluidTraderStorageContainer)
				{
					FluidTraderStorageContainer container = (FluidTraderStorageContainer)player.openContainer;
					container.openFluidEditScreenForTrade(message.tradeIndex);
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
