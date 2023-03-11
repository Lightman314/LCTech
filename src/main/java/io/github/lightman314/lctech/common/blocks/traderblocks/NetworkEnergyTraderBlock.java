package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

public class NetworkEnergyTraderBlock extends TraderBlockRotatable {
	
	public NetworkEnergyTraderBlock(Properties properties) { super(properties); }
	
	@Override
	protected TileEntity makeTrader() { return new EnergyTraderBlockEntity(true); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, TechTooltips.ENERGY_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

}