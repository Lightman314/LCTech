package io.github.lightman314.lctech.common.blocks;

import java.util.function.Supplier;

import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackState;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTankBlock extends Block implements IFluidTankBlock {

	public static final VoxelShape SHAPE = LazyShapes.BOX_T;
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 14f, 15.98f);
	public static final FluidRenderData RENDER_DATA_BOTTOM = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 15f, 15.98f);
	public static final FluidRenderData RENDER_DATA_TOP = FluidRenderData.CreateFluidRender(0.01f, 0f, 0.01f, 15.98f, 15f, 15.98f);
	public static final FluidRenderData RENDER_DATA_MIDDLE = FluidRenderData.CreateFluidRender(0.01f, 0f, 0.01f, 15.98f, 16f, 15.98f);

	private final VoxelShape shape;

	private final Supplier<Integer> tankCapacity;
	public int getTankCapacity() { return Math.max(this.tankCapacity.get(), FluidAttributes.BUCKET_VOLUME); }

	public FluidTankBlock(int tankCapacity, Properties properties) { this(() -> tankCapacity, properties, SHAPE); }

	public FluidTankBlock(Supplier<Integer> tankCapacity, Properties properties) { this(tankCapacity, properties, SHAPE); }

	public FluidTankBlock(int tankCapacity, Properties properties, VoxelShape shape) { this(() -> tankCapacity, properties, shape); }

	public FluidTankBlock(Supplier<Integer> tankCapacity, Properties properties, VoxelShape shape)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(TANK_STATE, TankStackState.SOLO));
		this.tankCapacity = tankCapacity;
		this.shape = shape;
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TANK_STATE);
	}

	@Override
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) { return this.shape; }

	@Override
	public void setPlacedBy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(!level.isClientSide)
		{
			if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity)
			{
				FluidTankBlockEntity tank = (FluidTankBlockEntity)level.getBlockEntity(pos);
				tank.loadFromItem(stack);
			}
		}
	}

	@Override
	public @Nonnull ActionResultType use(@Nonnull BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(result.getDirection().getAxis().isVertical() && player.getItemInHand(hand).getItem() instanceof BlockItem)
			return ActionResultType.PASS;
		if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity)
		{
			FluidTankBlockEntity tank = (FluidTankBlockEntity)level.getBlockEntity(pos);
			return tank.onInteraction(player, hand);
		}
		return ActionResultType.PASS;
	}

	//Drop tank item
	@Override
	public void playerWillDestroy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player)
	{
		if(!level.isClientSide && !player.isCreative())
		{
			TileEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof FluidTankBlockEntity)
			{
				popResource(level, pos, FluidTankItem.GetItemFromTank((FluidTankBlockEntity)tileEntity));
			}
		}
		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public @Nonnull ItemStack getCloneItemStack(IBlockReader level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof FluidTankBlockEntity)
			return FluidTankItem.GetItemFromTank((FluidTankBlockEntity)blockEntity);
		return new ItemStack(this);
	}

	@Override
	public FluidRenderData getItemRenderData() { return RENDER_DATA; }

	@Override
	public FluidRenderData getRenderData(BlockState state, boolean lighterThanAir, FluidTankBlockEntity tank, @Nullable FluidTankBlockEntity nextTank) {
		switch (this.getTankState(state)) {
			case BOTTOM:
				if(lighterThanAir)
					return RENDER_DATA_BOTTOM.withSides(FluidSides.NO_TOP);
				return RENDER_DATA_BOTTOM.withSides(tank.getTankFillPercent() >= 1d && nextTank != null && nextTank.getTankFillPercent() > 0d ? FluidSides.NO_TOP : FluidSides.ALL);
			case TOP:
				return RENDER_DATA_TOP.withSides(!lighterThanAir || this.hideNextFace(tank, nextTank), FluidSides.NO_BOTTOM);
			case MIDDLE:
				if(lighterThanAir)
					return RENDER_DATA_MIDDLE.withSides(this.hideNextFace(tank, nextTank), FluidSides.NO_TOP_OR_BOTTOM, FluidSides.NO_TOP);
				else
					return RENDER_DATA_MIDDLE.withSides(this.hideNextFace(tank, nextTank), FluidSides.NO_TOP_OR_BOTTOM, FluidSides.NO_BOTTOM);
			default: return RENDER_DATA;
		}
	}

	private boolean hideNextFace(FluidTankBlockEntity tank, @Nullable FluidTankBlockEntity nextTank) {
		return tank.getTankFillPercent() >= 1d && nextTank != null && nextTank.getTankFillPercent() > 0d;
	}

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, IBlockReader world) { return new FluidTankBlockEntity(); }

	@SuppressWarnings("deprecation")
	@Override
	public @Nonnull BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos)
	{
		if(facing.getAxis().isVertical())
		{
			if(worldIn.getBlockEntity(currentPos) instanceof FluidTankBlockEntity)
			{
				FluidTankBlockEntity tank = (FluidTankBlockEntity)worldIn.getBlockEntity(currentPos);
				tank.enqueTankStackRefactor();
			}
		}
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

}