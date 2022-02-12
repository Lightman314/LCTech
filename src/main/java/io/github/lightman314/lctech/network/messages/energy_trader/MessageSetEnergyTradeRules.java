package io.github.lightman314.lctech.network.messages.energy_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.EnergyTraderTileEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public MessageSetEnergyTradeRules decode(PacketBuffer buffer) {
		return new MessageSetEnergyTradeRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetEnergyTradeRules message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(TradeRule.writeRules(new CompoundNBT(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetEnergyTradeRules message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity blockEntity = player.world.getTileEntity(message.pos);
				if(blockEntity instanceof EnergyTraderTileEntity)
				{
					EnergyTraderTileEntity traderEntity = (EnergyTraderTileEntity)blockEntity;
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
