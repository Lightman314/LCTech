package io.github.lightman314.lctech.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lctech.tileentities.UniversalFluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class FluidTraderServerBlock extends RotatableBlock implements ITraderBlock{
	
	final int tradeCount;
	
	public FluidTraderServerBlock(int tradeCount, Properties properties)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Override
	@Nullable
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new UniversalFluidTraderTileEntity(this.tradeCount);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!worldIn.isRemote)
		{
			UniversalTraderTileEntity tileEntity = (UniversalTraderTileEntity)worldIn.getTileEntity(pos);
			if(tileEntity != null && player instanceof PlayerEntity)
			{
				if(stack.hasDisplayName())
					tileEntity.init((PlayerEntity)player, stack.getDisplayName().getString());
				else
					tileEntity.init((PlayerEntity)player);
			}
		}
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		UniversalTraderTileEntity tileEntity = (UniversalTraderTileEntity)worldIn.getTileEntity(pos);
		if(tileEntity != null)
		{
			if(!tileEntity.canBreak(player))
				return;
			tileEntity.onDestroyed();
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote)
		{
			UniversalTraderTileEntity tileEntity = (UniversalTraderTileEntity)world.getTileEntity(pos);
			if(tileEntity != null)
			{
				if(tileEntity.hasPermission(playerEntity, Permissions.OPEN_STORAGE))
				{
					tileEntity.updateNames(playerEntity);
					tileEntity.openStorageMenu(playerEntity);
				}
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos)
	{
		return world.getTileEntity(pos);
	}
	

}
