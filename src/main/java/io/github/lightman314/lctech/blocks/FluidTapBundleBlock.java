package io.github.lightman314.lctech.blocks;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidSides;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class FluidTapBundleBlock extends RotatableBlock implements IFluidTraderBlock{
	
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
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!world.isRemote())
		{
			TraderTileEntity tileEntity = (TraderTileEntity)world.getTileEntity(pos);
			if(tileEntity != null)
			{
				tileEntity.setOwner(player);
				if(stack.hasDisplayName())
					tileEntity.setCustomName(stack.getDisplayName().getString());
			}
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = this.getTileEntity(state, world, pos);
			if(tileEntity instanceof TraderTileEntity)
			{
				TraderTileEntity trader = (FluidTraderTileEntity)tileEntity;
				if(trader.isOwner(player) && !trader.isCreative())
				{
					trader.setOwner(player);
				}
				TileEntityUtil.sendUpdatePacket(tileEntity);
				trader.openTradeMenu(player);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TileEntity tileEntity = getTileEntity(state, worldIn, pos);
		if(tileEntity instanceof TraderTileEntity)
		{
			TraderTileEntity traderEntity = (TraderTileEntity)tileEntity;
			if(!traderEntity.canBreak(player))
				return;
			else
				traderEntity.dumpContents(worldIn, pos);
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new FluidTraderTileEntity(4);
	}
	
	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos) {
		return world.getTileEntity(pos);
	}

	@Override
	public int getTradeRenderLimit() {
		return 4;
	}
	
	private static List<Direction> IGNORELIST = Lists.newArrayList(Direction.UP, Direction.DOWN);
	private static Map<Direction,List<FluidRenderData>> RENDERMAP = Maps.newHashMap();
	
	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index){
		
		Direction facing = state.get(FACING);
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
