package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.blockentities.FluidTraderBlockEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageToggleFluidIcon implements IMessage<MessageToggleFluidIcon>{
	
	BlockPos pos;
	int tradeIndex;
	int icon;
	
	public MessageToggleFluidIcon() {};
	
	public MessageToggleFluidIcon(BlockPos pos, int tradeIndex, int icon)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.icon = icon;
	}
	
	@Override
	public MessageToggleFluidIcon decode(FriendlyByteBuf buffer) {
		return new MessageToggleFluidIcon(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	public void encode(MessageToggleFluidIcon message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeInt(message.icon);
	}

	@Override
	public void handle(MessageToggleFluidIcon message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof FluidTraderBlockEntity)
				{
					FluidTraderBlockEntity traderEntity = (FluidTraderBlockEntity)blockEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					switch(message.icon)
					{
					case 0:
						trade.setDrainableExternally(!trade.canDrainExternally());
						break;
					case 1:
						trade.setFillableExternally(!trade.canFillExternally());
						break;
					}
					traderEntity.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
