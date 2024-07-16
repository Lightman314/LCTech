package io.github.lightman314.lctech.client.models.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankModel implements BakedModel {
	
	BakedModel baseFluidTankModel;
	ItemOverrides fluidTankItemOverrideList = new FluidTankItemOverrideList();
	
	public FluidTankModel(BakedModel baseFluidTankModel)
	{
		this.baseFluidTankModel = baseFluidTankModel;
	}
	
	
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand)
	{
		return this.baseFluidTankModel.getQuads(state, side, rand);
	}
	
	@Override
	@Nonnull
	public ItemOverrides getOverrides() { return this.fluidTankItemOverrideList; }
	
	@Override
	public boolean useAmbientOcclusion() { return this.baseFluidTankModel.useAmbientOcclusion(); }
	
	@Override
	public boolean isGui3d() { return this.baseFluidTankModel.isGui3d(); }
	
	@Override
	public boolean usesBlockLight() { return this.baseFluidTankModel.usesBlockLight(); }
	
	@Override
	public boolean isCustomRenderer() { return this.baseFluidTankModel.isCustomRenderer(); }
	
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() { return this.baseFluidTankModel.getParticleIcon(); }
	
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public ItemTransforms getTransforms() { return this.baseFluidTankModel.getTransforms(); }
	
	public static class FluidTankItemOverrideList extends ItemOverrides{

		public FluidTankItemOverrideList() { super(); }
		
		@Override
		public BakedModel resolve(@Nonnull BakedModel model, @Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int light)
		{
			FluidRenderData renderData = FluidTankBlock.RENDER_DATA;
			FluidStack tank = FluidTankItem.GetFluid(stack);
			if(stack.getItem() instanceof BlockItem blockItem)
			{
				Block block = blockItem.getBlock();
				if(block instanceof IFluidTankBlock tankBlock)
					renderData = tankBlock.getItemRenderData();
			}
			return new FluidTankFinalizedModel(model, tank, () -> FluidTankItem.GetCapacity(stack), renderData);
		}
		
	}
	
}
