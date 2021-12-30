package io.github.lightman314.lctech.blocks.traderblocks;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lctech.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidSides;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTapBundleBlock extends TraderBlockRotatable implements IFluidTraderBlock{

	public static final FluidRenderData FLUID_RENDER_NW = FluidRenderData.CreateFluidRender(0.01f, 0.01f, 0.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	public static final FluidRenderData FLUID_RENDER_NE = FluidRenderData.CreateFluidRender(8.01f, 0.01f, 0.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	public static final FluidRenderData FLUID_RENDER_SW = FluidRenderData.CreateFluidRender(0.01f, 0.01f, 8.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	public static final FluidRenderData FLUID_RENDER_SE = FluidRenderData.CreateFluidRender(8.01f, 0.01f, 8.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	public static final List<FluidRenderData> FLUID_RENDER = Lists.newArrayList(FLUID_RENDER_NW, FLUID_RENDER_NE, FLUID_RENDER_SW, FLUID_RENDER_SE);
	
	public FluidTapBundleBlock(Properties properties)
	{
		super(properties);
	}
	
	public FluidTapBundleBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) {
		return new FluidTraderTileEntity(pos, state, 4);
	}

	@Override
	protected BlockEntityType<?> traderType() {
		return ModTileEntities.FLUID_TRADER;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if(this.shouldMakeTrader(state))
			return this.makeTrader(pos, state);
		return new DummyBlockEntity(pos, state);
	}
	
	@Override
	public int getTradeRenderLimit() {
		return 4;
	}
	
	private static List<Direction> IGNORELIST = Lists.newArrayList(Direction.UP, Direction.DOWN);
	private static Map<Direction,List<FluidRenderData>> RENDERMAP = Maps.newHashMap();
	
	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index){
		
		Direction facing = state.getValue(FACING);
		if(!RENDERMAP.containsKey(facing))
			initRenderMap(facing);
		if(RENDERMAP.containsKey(facing))
		{
			List<FluidRenderData> renderList = RENDERMAP.get(facing);
			if(index < 0 || index >= renderList.size())
				return null;
			return renderList.get(index);
		}
		return null;
	}
	
	private static void initRenderMap(Direction direction)
	{
		if(IGNORELIST.contains(direction))
			return;
		List<FluidRenderData> list = Lists.newArrayList();
		switch(direction) {
		case NORTH:
			list = createList(0,1,2,3);
			break;
		case EAST:
			list = createList(1,3,0,2);
			break;
		case SOUTH:
			list = createList(3,2,1,0);
			break;
		case WEST:
			list = createList(2,0,3,1);
			break;
		default:
		}
		if(list.size() > 0)
			RENDERMAP.put(direction, list);
		else //No results, so return nothing
			IGNORELIST.add(direction);
	}
	
	private static List<FluidRenderData> createList(int... indexes)
	{
		List<FluidRenderData> list = Lists.newArrayList();
		for(int index : indexes)
		{
			if(index >= 0 && index < FLUID_RENDER.size())
				list.add(FLUID_RENDER.get(index));
		}
		return list;
	}
	
}
