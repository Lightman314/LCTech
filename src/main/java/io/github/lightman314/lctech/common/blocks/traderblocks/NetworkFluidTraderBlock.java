package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

public class NetworkFluidTraderBlock extends TraderBlockRotatable {
	
	public static final int SMALL_SERVER_COUNT = 2;
	public static final int MEDIUM_SERVER_COUNT = 4;
	public static final int LARGE_SERVER_COUNT = 6;
	public static final int EXTRA_LARGE_SERVER_COUNT = 8;
	
	final int tradeCount;
	
	public NetworkFluidTraderBlock(int tradeCount, Properties properties)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	protected TileEntity makeTrader() { return new FluidTraderBlockEntity(this.tradeCount, true); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, TechTooltips.FLUID_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}