package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTankBlock extends Block implements EntityBlock{

	public static final VoxelShape SHAPE = LazyShapes.BOX_T;
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 14f, 15.98f);
	
	private final VoxelShape shape;
	
	public final int tankCapacity;
	
	public FluidTankBlock(int tankCapacity, Properties properties)
	{
		super(properties);
		this.tankCapacity = tankCapacity;
		this.shape = SHAPE;
	}
	
	public FluidTankBlock(int tankCapacity, Properties properties, VoxelShape shape)
	{
		super(properties);
		this.tankCapacity = tankCapacity;
		this.shape = shape;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return this.shape;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			FluidTankTileEntity tileEntity = (FluidTankTileEntity)level.getBlockEntity(pos);
			if(tileEntity != null)
				tileEntity.loadFromItem(stack);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof FluidTankTileEntity)
		{
			return ((FluidTankTileEntity)blockEntity).onInteraction(player, hand);
		}
		return InteractionResult.PASS;
	}
	
	//Drop tank item
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		if(!level.isClientSide && !player.isCreative())
		{
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof FluidTankTileEntity)
			{
				popResource(level, pos, FluidTankItem.GetItemFromTank((FluidTankTileEntity)tileEntity));
			}
		}
		super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof FluidTankTileEntity)
			return FluidTankItem.GetItemFromTank((FluidTankTileEntity)blockEntity);
		return new ItemStack(this);
	}
	
	public FluidRenderData getRenderData() { return RENDER_DATA; }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTankTileEntity(pos, state);
	}
	
}
