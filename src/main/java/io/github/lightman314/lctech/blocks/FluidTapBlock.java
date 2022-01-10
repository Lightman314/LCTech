package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidSides;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class FluidTapBlock extends RotatableBlock implements IFluidTraderBlock{

	public static final FluidRenderData FLUID_RENDER = FluidRenderData.CreateFluidRender(4.01f, 0.01f, 4.01f, 7.98f, 15.98f, 7.98f, FluidSides.ALL);
	
	public FluidTapBlock(Properties properties)
	{
		super(properties);
	}
	
	public FluidTapBlock(Properties properties, VoxelShape shape)
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
				tileEntity.initOwner(PlayerReference.of(player));
				if(stack.hasDisplayName())
					tileEntity.getCoreSettings().setCustomName(null, stack.getDisplayName().getString());
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
				trader.getCoreSettings().updateNames(player);
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
		return new FluidTraderTileEntity(1);
	}
	
	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos) {
		return world.getTileEntity(pos);
	}

	@Override
	public int getTradeRenderLimit() {
		return 1;
	}
	
	@Override
	public FluidRenderData getRenderPosition(BlockState state, int index){
		return FLUID_RENDER;
	}
	
}
