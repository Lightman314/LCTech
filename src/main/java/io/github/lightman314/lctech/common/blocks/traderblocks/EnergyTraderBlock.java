package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.NonNullSupplier;

public class EnergyTraderBlock extends TraderBlockRotatable {
	
	public EnergyTraderBlock(Properties properties) { super(properties); }
	
	public EnergyTraderBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new EnergyTraderBlockEntity(pos, state); }

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.ENERGY_TRADER.get(); }
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_ENERGY_TRADER.asTooltip(); }

}
