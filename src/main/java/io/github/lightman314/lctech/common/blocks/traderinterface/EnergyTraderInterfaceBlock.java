package io.github.lightman314.lctech.common.blocks.traderinterface;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blocks.TraderInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyTraderInterfaceBlock extends TraderInterfaceBlock {

	public EnergyTraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EnergyTraderInterfaceBlockEntity(pos, state);
	}
	
	@Override
	protected BlockEntityType<?> interfaceType() { return ModBlockEntities.TRADER_INTERFACE_ENERGY.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_ENERGY_TRADER_INTERFACE.asTooltip(); }

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity blockEntity) { }
	
}
