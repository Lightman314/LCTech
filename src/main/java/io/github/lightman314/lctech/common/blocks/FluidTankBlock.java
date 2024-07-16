package io.github.lightman314.lctech.common.blocks;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackState;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTankBlock extends EasyBlock implements IEasyEntityBlock, IFluidTankBlock{

	public static final VoxelShape SHAPE = LazyShapes.BOX;
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 14f, 15.98f);
	public static final FluidRenderData RENDER_DATA_BOTTOM = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 15f, 15.98f);
	public static final FluidRenderData RENDER_DATA_TOP = FluidRenderData.CreateFluidRender(0.01f, 0f, 0.01f, 15.98f, 15f, 15.98f);
	public static final FluidRenderData RENDER_DATA_MIDDLE = FluidRenderData.CreateFluidRender(0.01f, 0f, 0.01f, 15.98f, 16f, 15.98f);

	private final VoxelShape shape;
	
	private final Supplier<Integer> tankCapacity;
	public int getTankCapacity() { return Math.max(this.tankCapacity.get(), FluidType.BUCKET_VOLUME); }
	
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
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TANK_STATE);
	}
	
	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return this.shape; }
	
	@Override
	public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(!level.isClientSide)
		{
			if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank)
				tank.loadFromItem(stack);
		}
	}

	@Nonnull
	@Override
	protected ItemInteractionResult useItemOn(@Nonnull ItemStack heldItem, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, BlockHitResult hit) {
		if(hit.getDirection().getAxis().isVertical() && player.getItemInHand(hand).getItem() instanceof BlockItem)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank)
			return tank.onInteraction(heldItem, player, hand);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	
	//Drop tank item
	@Nonnull
	@Override
	public BlockState playerWillDestroy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		if(!level.isClientSide && !player.isCreative())
		{
			if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity be)
			{
				popResource(level, pos, FluidTankItem.GetItemFromTank(be));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Nonnull
	@Override
	public ItemStack getCloneItemStack(@Nonnull BlockState state, @Nonnull HitResult target, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nonnull Player player) {
		if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank)
			return FluidTankItem.GetItemFromTank(tank);
		return new ItemStack(this);
	}

	@Override
	public FluidRenderData getItemRenderData() { return RENDER_DATA; }

	@Override
	public FluidRenderData getRenderData(BlockState state, boolean lighterThanAir, FluidTankBlockEntity tank, @Nullable FluidTankBlockEntity nextTank) {
		switch (this.getTankState(tank.getBlockState())) {
			case BOTTOM -> {
				if(lighterThanAir)
					return RENDER_DATA_BOTTOM.withSides(FluidSides.NO_TOP);
				return RENDER_DATA_BOTTOM.withSides(tank.getTankFillPercent() >= 1d && nextTank != null && nextTank.getTankFillPercent() > 0d ? FluidSides.NO_TOP : FluidSides.ALL);
			}
			case TOP -> {
				return RENDER_DATA_TOP.withSides(!lighterThanAir || this.hideNextFace(tank, nextTank), FluidSides.NO_BOTTOM);
			}
			case MIDDLE -> {
				if(lighterThanAir)
					return RENDER_DATA_MIDDLE.withSides(this.hideNextFace(tank, nextTank), FluidSides.NO_TOP_OR_BOTTOM, FluidSides.NO_TOP);
				else
					return RENDER_DATA_MIDDLE.withSides(this.hideNextFace(tank, nextTank), FluidSides.NO_TOP_OR_BOTTOM, FluidSides.NO_BOTTOM);
			}
			default -> { return RENDER_DATA; }
		}
	}

	private boolean hideNextFace(FluidTankBlockEntity tank, @Nullable FluidTankBlockEntity nextTank) {
		return tank.getTankFillPercent() >= 1d && nextTank != null && nextTank.getTankFillPercent() > 0d;
	}

	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new FluidTankBlockEntity(pos, state); }

	@Nonnull
	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(ModBlockEntities.FLUID_TANK.get()); }

	@Override
	@Nonnull
	public BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos)
	{
		if(facing.getAxis().isVertical())
		{
			if(worldIn.getBlockEntity(currentPos) instanceof FluidTankBlockEntity tank)
				tank.enqueTankStackRefactor();
		}
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

}
