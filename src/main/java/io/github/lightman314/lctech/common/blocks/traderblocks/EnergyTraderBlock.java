package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.NonNullSupplier;

public class EnergyTraderBlock extends TraderBlockRotatable {
	
	public EnergyTraderBlock(Properties properties)
	{
		super(properties);
	}
	
	public EnergyTraderBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}

	@Override
	protected TileEntity makeTrader() {
		return new EnergyTraderBlockEntity();
	}
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return TechTooltips.ENERGY_TRADER; }

}
