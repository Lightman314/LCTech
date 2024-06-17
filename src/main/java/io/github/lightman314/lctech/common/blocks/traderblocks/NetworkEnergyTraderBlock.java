package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;

public class NetworkEnergyTraderBlock extends TraderBlockRotatable {
	
	public NetworkEnergyTraderBlock(Properties properties) { super(properties); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new EnergyTraderBlockEntity(pos, state, true); }
	
	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.ENERGY_TRADER.get(); }
	
	@Override @SuppressWarnings("deprecation")
	protected List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ENERGY_TRADER.get()); }

	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_NETWORK_ENERGY_TRADER.asTooltip(); }
}
