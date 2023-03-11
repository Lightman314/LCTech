package io.github.lightman314.lctech.common.blocks.traderinterface;

import java.util.List;

import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates.TraderInterfaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public class FluidTraderInterfaceBlock extends TraderInterfaceBlock {

	public FluidTraderInterfaceBlock(Properties properties) { super(properties); }
	
	@Override
	protected TileEntity createBlockEntity(BlockState state) { return new FluidTraderInterfaceBlockEntity(); }
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return TechTooltips.FLUID_TRADER_INTERFACE; }
	
	@Override
	protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderInterfaceBlockEntity blockEntity) { }
	
}
