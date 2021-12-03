package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public MessageToggleFluidIcon decode(PacketBuffer buffer) {
		return new MessageToggleFluidIcon(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	public void encode(MessageToggleFluidIcon message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeInt(message.icon);
	}

	@Override
	public void handle(MessageToggleFluidIcon message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity tileEntity = player.world.getTileEntity(message.pos);
				if(tileEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)tileEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					switch(message.icon)
					{
					case 0:
						trade.setDrainable(!trade.canDrain());
						break;
					case 1:
						trade.setFillable(!trade.canFill());
						break;
					}
					traderEntity.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
