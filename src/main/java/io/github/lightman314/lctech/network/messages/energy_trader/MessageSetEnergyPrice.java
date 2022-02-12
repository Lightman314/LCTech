package io.github.lightman314.lctech.network.messages.energy_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.EnergyTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetEnergyPrice implements IMessage<MessageSetEnergyPrice>{
	
	BlockPos pos;
	int tradeIndex;
	CoinValue price;
	TradeDirection tradeType;
	int amount;
	
	public MessageSetEnergyPrice() {};
	
	public MessageSetEnergyPrice(BlockPos pos, int tradeIndex, CoinValue price, TradeDirection tradeType, int amount)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.price = price;
		this.tradeType = tradeType;
		this.amount = amount;
	}
	
	@Override
	public MessageSetEnergyPrice decode(PacketBuffer buffer) {
		return new MessageSetEnergyPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), EnergyTradeData.loadTradeType(buffer.readString(100)), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyPrice message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.price.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeString(message.tradeType.name());
		buffer.writeInt(message.amount);
		
	}

	@Override
	public void handle(MessageSetEnergyPrice message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity blockEntity = player.world.getTileEntity(message.pos);
				if(blockEntity instanceof EnergyTraderTileEntity)
				{
					EnergyTraderTileEntity traderEntity = (EnergyTraderTileEntity)blockEntity;
					EnergyTradeData trade = traderEntity.getTrade(message.tradeIndex);
					
					trade.setCost(message.price);
					trade.setTradeDirection(message.tradeType);
					trade.setAmount(message.amount);
					
					traderEntity.markTradesDirty();
					
				}
			}
		});
		source.get().setPacketHandled(true);
	}
	
}
