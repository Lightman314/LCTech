package io.github.lightman314.lctech.network.messages.energy_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.blockentities.EnergyTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetEnergyTradeRules implements IMessage<MessageSetEnergyTradeRules>{
	
	BlockPos pos;
	int tradeIndex;
	List<TradeRule> rules;
	
	public MessageSetEnergyTradeRules() {};
	
	public MessageSetEnergyTradeRules(BlockPos pos, List<TradeRule> rules)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = -1;
	}
	
	public MessageSetEnergyTradeRules(BlockPos pos, List<TradeRule> rules, int tradeIndex)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageSetEnergyTradeRules decode(FriendlyByteBuf buffer) {
		return new MessageSetEnergyTradeRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyTradeRules message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetEnergyTradeRules message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof EnergyTraderBlockEntity)
				{
					EnergyTraderBlockEntity traderEntity = (EnergyTraderBlockEntity)blockEntity;
					if(message.tradeIndex < 0)
					{
						traderEntity.setRules(message.rules);
						traderEntity.markRulesDirty();
					}
					else
					{
						ITradeRuleHandler trade = traderEntity.getTrade(message.tradeIndex);
						if(trade != null)
							trade.setRules(message.rules);
						traderEntity.markTradesDirty();
					}
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
