package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new FluidTraderBlockEntity(pos, state, this.tradeCount, true); }
	
	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.FLUID_TRADER.get(); }
	
	@Override @SuppressWarnings("deprecation")
	protected List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.FLUID_TRADER.get(), ModBlockEntities.UNIVERSAL_FLUID_TRADER.get()); }

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, TechTooltips.FLUID_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}