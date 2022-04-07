package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.menu.FluidTraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
public class MessageFluidTradeTankInteraction implements IMessage<MessageFluidTradeTankInteraction>{
	
	int tradeIndex;
	
	public MessageFluidTradeTankInteraction() {};
	
	public MessageFluidTradeTankInteraction(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageFluidTradeTankInteraction decode(FriendlyByteBuf buffer) {
		return new MessageFluidTradeTankInteraction(buffer.readInt());
	}

	@Override
	public void encode(MessageFluidTradeTankInteraction message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageFluidTradeTankInteraction message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof FluidTraderStorageMenu)
				{
					FluidTraderStorageMenu container = (FluidTraderStorageMenu)player.containerMenu;
					container.PlayerTankInteraction(message.tradeIndex);
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
