package io.github.lightman314.lctech.network.messages.energy_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.blockentities.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public MessageSetEnergyPrice decode(FriendlyByteBuf buffer) {
		return new MessageSetEnergyPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readNbt()), EnergyTradeData.loadTradeType(buffer.readUtf()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyPrice message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.price.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeUtf(message.tradeType.name());
		buffer.writeInt(message.amount);
		
	}

	@Override
	public void handle(MessageSetEnergyPrice message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof EnergyTraderBlockEntity)
				{
					EnergyTraderBlockEntity traderEntity = (EnergyTraderBlockEntity)blockEntity;
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
