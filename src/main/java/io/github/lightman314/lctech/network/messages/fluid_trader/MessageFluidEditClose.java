package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.menu.FluidEditMenu;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageFluidEditClose implements IMessage<MessageFluidEditClose>{
	
	public MessageFluidEditClose() {};
	
	@Override
	public MessageFluidEditClose decode(FriendlyByteBuf buffer) {
		return new MessageFluidEditClose();
	}

	@Override
	public void encode(MessageFluidEditClose message, FriendlyByteBuf buffer) {
		
	}

	@Override
	public void handle(MessageFluidEditClose message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof FluidEditMenu)
				{
					FluidEditMenu container = (FluidEditMenu)player.containerMenu;
					container.openTraderStorage();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
