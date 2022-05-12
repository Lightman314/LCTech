package io.github.lightman314.lctech.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lctech.blockentities.UniversalFluidTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FluidTraderServerBlock extends RotatableBlock implements ITraderBlock, EntityBlock{
	
	public static final int SMALL_SERVER_COUNT = 2;
	public static final int MEDIUM_SERVER_COUNT = 2;
	public static final int LARGE_SERVER_COUNT = 2;
	public static final int EXTRA_LARGE_SERVER_COUNT = 2;
	
	final int tradeCount;
	
	public FluidTraderServerBlock(int tradeCount, Properties properties)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalFluidTraderBlockEntity(pos, state, this.tradeCount);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			UniversalTraderBlockEntity tileEntity = (UniversalTraderBlockEntity)level.getBlockEntity(pos);
			if(tileEntity != null && player instanceof Player)
			{
				if(stack.hasCustomHoverName())
					tileEntity.init((Player)player, stack.getDisplayName().getString());
				else
					tileEntity.init((Player)player);
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		UniversalTraderBlockEntity blockEntity = (UniversalTraderBlockEntity)level.getBlockEntity(pos);
		if(blockEntity != null)
		{
			if(!blockEntity.canBreak(player))
				return;
			blockEntity.onDestroyed();
		}
		
		super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			UniversalTraderBlockEntity tileEntity = (UniversalTraderBlockEntity)level.getBlockEntity(pos);
			if(tileEntity != null)
			{
				if(tileEntity.hasPermission(player, Permissions.OPEN_STORAGE))
				{
					tileEntity.updateNames(player);
					tileEntity.openStorageMenu(player);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		return level.getBlockEntity(pos);
	}
	

}
