package io.github.lightman314.lctech.blocks.traderinterface;

import java.util.List;

import io.github.lightman314.lctech.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.tradeinterface.templates.TraderInterfaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;

public class EnergyTraderInterfaceBlock extends TraderInterfaceBlock {

	public EnergyTraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EnergyTraderInterfaceBlockEntity(pos, state);
	}
	
	@Override
	protected BlockEntityType<?> interfaceType() { 
		return ModBlockEntities.TRADER_INTERFACE_ENERGY.get();
	}
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return TechTooltips.ENERGY_TRADER_INTERFACE; }

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity blockEntity) { }
	
}
