package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.container.FluidTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageFluidEditOpen implements IMessage<MessageFluidEditOpen>{
	
	int tradeIndex;
	
	public MessageFluidEditOpen() {};
	
	public MessageFluidEditOpen(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageFluidEditOpen decode(FriendlyByteBuf buffer) {
		return new MessageFluidEditOpen(buffer.readInt());
	}

	@Override
	public void encode(MessageFluidEditOpen message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageFluidEditOpen message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof FluidTraderStorageContainer)
				{
					FluidTraderStorageContainer container = (FluidTraderStorageContainer)player.containerMenu;
					container.openFluidEditScreenForTrade(message.tradeIndex);
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
