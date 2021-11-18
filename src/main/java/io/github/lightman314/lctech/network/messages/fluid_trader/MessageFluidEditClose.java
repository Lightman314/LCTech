package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.container.FluidEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageFluidEditClose implements IMessage<MessageFluidEditClose>{
	
	public MessageFluidEditClose() {};
	
	@Override
	public MessageFluidEditClose decode(PacketBuffer buffer) {
		return new MessageFluidEditClose();
	}

	@Override
	public void encode(MessageFluidEditClose message, PacketBuffer buffer) {
		
	}

	@Override
	public void handle(MessageFluidEditClose message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof FluidEditContainer)
				{
					FluidEditContainer container = (FluidEditContainer)player.openContainer;
					container.openTraderStorage();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
