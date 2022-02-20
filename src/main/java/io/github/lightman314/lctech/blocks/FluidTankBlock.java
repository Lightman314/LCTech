package io.github.lightman314.lctech.blocks;

import java.util.function.Supplier;

import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

public class FluidTankBlock extends Block implements IFluidTankBlock{

	public static final VoxelShape SHAPE = makeCuboidShape(0d, 0d, 0d, 16d, 16d, 16d);
	public static final FluidRenderData RENDER_DATA = FluidRenderData.CreateFluidRender(0.01f, 1f, 0.01f, 15.98f, 14f, 15.98f);
	
	private final VoxelShape shape;
	
	private final Supplier<Integer> tankCapacity;
	public int getTankCapacity() { return Math.max(this.tankCapacity.get(), FluidAttributes.BUCKET_VOLUME); }
	
	public FluidTankBlock(int tankCapacity, Properties properties) { this(() -> tankCapacity, properties); }
	
	public FluidTankBlock(Supplier<Integer> tankCapacity, Properties properties) { this(tankCapacity, properties, SHAPE); }
	
	public FluidTankBlock(int tankCapacity, Properties properties, VoxelShape shape) { this(() -> tankCapacity, properties, shape); }
	
	public FluidTankBlock(Supplier<Integer> tankCapacity, Properties properties, VoxelShape shape)
	{
		super(properties);
		this.tankCapacity = tankCapacity;
		this.shape = shape;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new FluidTankTileEntity();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		return this.shape;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!world.isRemote())
		{
			FluidTankTileEntity tileEntity = (FluidTankTileEntity)world.getTileEntity(pos);
			if(tileEntity != null)
				tileEntity.loadFromItem(stack);
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof FluidTankTileEntity)
		{
			return ((FluidTankTileEntity)tileEntity).onInteraction(player, hand);
		}
		return ActionResultType.PASS;
	}
	
	//Drop tank item
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		if(!worldIn.isRemote && !player.abilities.isCreativeMode)
		{
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			if(tileEntity instanceof FluidTankTileEntity)
			{
				spawnAsEntity(worldIn, pos, FluidTankItem.GetItemFromTank((FluidTankTileEntity)tileEntity));
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof FluidTankTileEntity)
			return FluidTankItem.GetItemFromTank((FluidTankTileEntity)tileEntity);
		return new ItemStack(this);
	}
	
	public FluidRenderData getRenderData() { return RENDER_DATA; }
	
}
