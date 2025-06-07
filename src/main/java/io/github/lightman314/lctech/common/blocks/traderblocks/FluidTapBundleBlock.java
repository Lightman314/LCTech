package io.github.lightman314.lctech.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderDataManager;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTapBundleBlock extends TraderBlockRotatable implements IFluidTraderBlock, IVariantBlock {

	public static final ResourceLocation DATA_NW = VersionUtil.modResource(LCTech.MODID,"fluid_tap_bundle/nw");
	public static final ResourceLocation DATA_NE = VersionUtil.modResource(LCTech.MODID,"fluid_tap_bundle/ne");
	public static final ResourceLocation DATA_SW = VersionUtil.modResource(LCTech.MODID,"fluid_tap_bundle/sw");
	public static final ResourceLocation DATA_SE = VersionUtil.modResource(LCTech.MODID,"fluid_tap_bundle/se");
	public static final List<ResourceLocation> FLUID_RENDER = Lists.newArrayList(DATA_NW, DATA_NE, DATA_SW, DATA_SE);
	
	public FluidTapBundleBlock(Properties properties) { super(properties); }
	public FluidTapBundleBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new FluidTraderBlockEntity(pos, state, 4); }

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.FLUID_TRADER.get(); }
	
	@Override
	public int getTradeRenderLimit() { return 4; }
	
	private static final List<Direction> IGNORELIST = Lists.newArrayList(Direction.UP, Direction.DOWN);
	private static final Map<Direction,List<ResourceLocation>> RENDERMAP = Maps.newHashMap();

	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index){
		
		Direction facing = state.getValue(FACING);
		if(!RENDERMAP.containsKey(facing))
			initRenderMap(facing);
		if(RENDERMAP.containsKey(facing))
		{
			List<ResourceLocation> renderList = RENDERMAP.get(facing);
			if(index < 0 || index >= renderList.size())
				return null;
			return FluidRenderDataManager.getDataOrEmpty(renderList.get(index));
		}
		return null;
	}

	private static void initRenderMap(Direction direction)
	{
		if(IGNORELIST.contains(direction))
			return;
		List<ResourceLocation> list;
		list = createList(getRenderOrder(direction));
		if(!list.isEmpty())
			RENDERMAP.put(direction, list);
		else //No results, so return nothing
			IGNORELIST.add(direction);
	}

	public static List<ResourceLocation> getRenderID(Direction facing)
	{
		if(!RENDERMAP.containsKey(facing))
			initRenderMap(facing);
		return RENDERMAP.getOrDefault(facing,new ArrayList<>());
	}

	private static List<Integer> getRenderOrder(Direction facing)
	{
		return switch(facing) {
			case NORTH -> ImmutableList.of(0,1,2,3);
			case EAST-> ImmutableList.of(1,3,0,2);
			case SOUTH-> ImmutableList.of(3,2,1,0);
			case WEST -> ImmutableList.of(2,0,3,1);
			default -> ImmutableList.of();
		};
	}
	
	private static List<ResourceLocation> createList(List<Integer> indexes)
	{
		List<ResourceLocation> list = Lists.newArrayList();
		for(int index : indexes)
		{
			if(index >= 0 && index < FLUID_RENDER.size())
				list.add(FLUID_RENDER.get(index));
		}
		return list;
	}
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return TechText.TOOLTIP_FLUID_TRADER.asTooltip(4); }
	
}
