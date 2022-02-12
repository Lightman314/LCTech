package io.github.lightman314.lctech.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lctech.tileentities.UniversalEnergyTraderTileEntity;
import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.RotatableBlock;
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

public class EnergyTraderServerBlock extends RotatableBlock implements ITraderBlock{
	
	public EnergyTraderServerBlock(Properties properties)
	{
		super(properties);
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
		return new UniversalEnergyTraderTileEntity();
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!world.isRemote)
		{
			UniversalEnergyTraderTileEntity tileEntity = (UniversalEnergyTraderTileEntity)world.getTileEntity(pos);
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
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		UniversalEnergyTraderTileEntity blockEntity = (UniversalEnergyTraderTileEntity)world.getTileEntity(pos);
		if(blockEntity != null)
		{
			if(!blockEntity.canBreak(player))
				return;
			blockEntity.onDestroyed();
		}
		
		super.onBlockHarvested(world, pos, state, player);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote)
		{
			UniversalEnergyTraderTileEntity tileEntity = (UniversalEnergyTraderTileEntity)world.getTileEntity(pos);
			if(tileEntity != null)
			{
				if(tileEntity.hasPermission(player, Permissions.OPEN_STORAGE))
				{
					tileEntity.updateNames(player);
					tileEntity.openStorageMenu(player);
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
