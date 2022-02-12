package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.menu.FluidEditMenu;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageFluidEditSet implements IMessage<MessageFluidEditSet>{
	
	private ItemStack item;
	
	public MessageFluidEditSet() {};
	
	public MessageFluidEditSet(ItemStack item)
	{
		this.item = item;
	}
	
	@Override
	public MessageFluidEditSet decode(FriendlyByteBuf buffer) {
		return new MessageFluidEditSet(buffer.readItem());
	}

	@Override
	public void encode(MessageFluidEditSet message, FriendlyByteBuf buffer) {
		buffer.writeItemStack(message.item, false);
	}

	@Override
	public void handle(MessageFluidEditSet message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof FluidEditMenu)
				{
					FluidEditMenu container = (FluidEditMenu)player.containerMenu;
					container.setFluid(message.item);
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
