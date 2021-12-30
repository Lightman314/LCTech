package io.github.lightman314.lctech.client.models.items;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.blocks.FluidTankBlock;
import io.github.lightman314.lctech.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankModel implements BakedModel {
	
	BakedModel baseFluidTankModel;
	ItemOverrides fluidTankItemOverrideList = new FluidTankItemOverrideList();
	
	public FluidTankModel(BakedModel baseFluidTankModel)
	{
		this.baseFluidTankModel = baseFluidTankModel;
	}
	
	
	@Override
	@SuppressWarnings("deprecation")
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
	{
		return this.baseFluidTankModel.getQuads(state, side, rand);
	}
	
	@Override
	public ItemOverrides getOverrides()
	{
		return this.fluidTankItemOverrideList;
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return this.baseFluidTankModel.useAmbientOcclusion();
	}
	
	@Override
	public boolean isGui3d() {
		return this.baseFluidTankModel.isGui3d();
	}
	
	@Override
	public boolean usesBlockLight() {
		return this.baseFluidTankModel.usesBlockLight();
	}
	
	@Override
	public boolean isCustomRenderer() {
		return this.baseFluidTankModel.isCustomRenderer();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() {
		return this.baseFluidTankModel.getParticleIcon();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public ItemTransforms getTransforms() {
		return this.baseFluidTankModel.getTransforms();
	}
	
	@Override
	@Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
		throw new AssertionError("FluidTankModel::getQuads(IModelData) should never be called");
	}
	
	@Override
	@Nonnull
	public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		throw new AssertionError("FluidTankModel::getModelData should never be called");
	}
	
	public class FluidTankItemOverrideList extends ItemOverrides{

		public FluidTankItemOverrideList() { super(); }
		
		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int light)
		{
			FluidStack tank = FluidStack.EMPTY;
			int capacity = FluidTankTileEntity.DEFAULT_CAPACITY;
			FluidRenderData renderData = FluidTankBlock.RENDER_DATA;
			if(stack != null)
			{
				tank = FluidTankItem.GetFluid(stack);
				capacity = FluidTankItem.GetCapacity(stack);
				if(stack.getItem() instanceof BlockItem)
				{
					Block block = ((BlockItem)stack.getItem()).getBlock();
					if(block instanceof IFluidTankBlock)
					{
						renderData = ((IFluidTankBlock)block).getRenderData();
					}
				}
			}
			return new FluidTankFinalizedModel(model, tank, capacity, renderData);
		}
		
	}
	
}
