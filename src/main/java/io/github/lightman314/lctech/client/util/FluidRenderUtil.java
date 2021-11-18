package io.github.lightman314.lctech.client.util;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class FluidRenderUtil {

	public static void drawFluidTankInGUI(FluidStack tank, int x, int y, int width, int height, double percent)
	{
		if(tank == null || tank.isEmpty())
			return;
		
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(tank.getFluid().getAttributes().getStillTexture());
		if(sprite != null)
		{	
			float minU = sprite.getMinU();
			float maxU = sprite.getMaxU();
			float minV = sprite.getMinV();
			float maxV = sprite.getMaxV();
			float deltaU = maxU - minU;
			float deltaV = maxV - minV;
			double tankLevel = percent * height;
			int waterColor = tank.getFluid().getAttributes().getColor(tank);
			float red = (float)(waterColor >> 16 & 255) / 255.0f;
			float green = (float)(waterColor >> 8 & 255) / 255.0f;
			float blue = (float)(waterColor & 255) / 255.0f;
			
			Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
			
			RenderSystem.enableBlend();
			int xCount = 1 + (width / 16);
			int count = 1 + ((int)Math.ceil(tankLevel)) / 16;
			for(int z = 0; z < xCount; z++)
			{
				double subWidth = Math.min(16.0, width - (16 * z));
				double offsetX = width - 16.0 * z - subWidth;
				for(int i = 0; i < count; i++)
				{
					double subHeight = Math.min(16.0,  tankLevel - (16.0 * i));
					double offsetY = height - 16.0 * i - subHeight;
					drawQuad(x + offsetX, y + offsetY, subWidth, subHeight, (float) (maxU - deltaU * (subWidth / 16.0)), (float) (maxV - deltaV * (subHeight / 16.0)), maxU, maxV, red, green, blue);
				}
			}
			RenderSystem.disableBlend();
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private static void drawQuad(double x, double y, double width, double height, float minU, float minV, float maxU, float maxV, float red, float green, float blue)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		RenderSystem.color4f(red, green, blue, 1f);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + height, 0).tex(minU, maxV).endVertex();
		buffer.pos(x + width, y + height, 0).tex(maxU, maxV).endVertex();
		buffer.pos(x + width, y, 0).tex(maxU, minV).endVertex();
		buffer.pos(x, y, 0).tex(minU, minV).endVertex();
		tessellator.draw();
	}
	
	public static void drawFluidInWorld(FluidStack tank, World world, BlockPos pos, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, FluidRenderData renderData, int light)
	{
		drawFluidInWorld(tank, world, pos, matrixStack, renderTypeBuffer, renderData.x, renderData.y, renderData.z, renderData.width, renderData.getHeight(), renderData.depth, light, renderData.sides);
	}
	
	public static void drawFluidInWorld(FluidStack tank, World world, BlockPos pos, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float x, float y, float z, float width, float height, float depth, int light, FluidSides sides)
	{
		
		if(tank.isEmpty())
			return;
		
		TextureAtlasSprite sprite = ForgeHooksClient.getFluidSprites(world,  pos,  tank.getFluid().getDefaultState())[0];
		int waterColor = tank.getFluid().getAttributes().getColor(world, pos);
		float red = (float)(waterColor >> 16 & 255) / 255.0f;
		float green = (float)(waterColor >> 8 & 255) / 255.0f;
		float blue = (float)(waterColor & 255) / 255.0f;
		float minU = sprite.getMinU();
		float maxU = Math.min(minU + (sprite.getMaxU() - minU) * depth, sprite.getMaxU());
		float minV = sprite.getMinV();
		float maxV = Math.min(minV + (sprite.getMaxV() - minV) * height, sprite.getMaxV());
		
		float x2 = x + width;
		float y2 = y + height;
		float z2 = z + depth;
		
		IVertexBuilder buffer = renderTypeBuffer.getBuffer(RenderType.getTranslucent());
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		
		//left side
		if(sides.test(Direction.WEST))
		{
			buffer.pos(matrix, x2, y, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
		}
		
		//right side
		if(sides.test(Direction.EAST))
		{
			buffer.pos(matrix, x, y, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
		}
		
		maxU = Math.min(minU + (sprite.getMaxU() - minU), sprite.getMaxU());
		
		//south side
		if(sides.test(Direction.SOUTH))
		{
			buffer.pos(matrix, x2, y, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
		}
		
		//north side
		if(sides.test(Direction.NORTH))
		{
			buffer.pos(matrix, x, y, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
		}
		
		maxV = Math.min(minV + (sprite.getMaxV() - minV), sprite.getMaxV());
		
		//top side
		if(sides.test(Direction.UP))
		{
			buffer.pos(matrix, x, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, minV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z2).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(minU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
			buffer.pos(matrix, x2, y2, z).color(red - 0.25f, green - 0.25f, blue - 0.25f, 1f).tex(maxU, maxV).lightmap(light).normal(0f, 1f, 0f).endVertex();
		}
		
	}
	
	public static List<BakedQuad> getBakedFluidQuads(FluidStack tank, int capacity, FluidRenderData renderData)
	{
		
		if(tank.isEmpty())
			return Lists.newArrayList();
		
		//Get sprite
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(tank.getFluid().getAttributes().getStillTexture());
		//Get color
		int fluidColor = tank.getFluid().getAttributes().getColor();
		if(fluidColor != 0xFFFFFFFF)
		{
			int red = fluidColor >> 16 & 255;
			int green = fluidColor >> 8 & 255;
			int blue = fluidColor & 255;
			fluidColor = 0xFF000000 | red | green << 8 | blue << 16;
		}
		
		double fillPercent = MathUtil.clamp((double)tank.getAmount() / (double)capacity, 0d, 1d);
		renderData.setFillPercent((float)fillPercent);
		
		final int ITEM_RENDER_LAYER0 = 0;
		
		List<BakedQuad> returnList = Lists.newArrayList();
		
		final int color = fluidColor;
		renderData.sides.forEach(face ->{
			BakedQuad faceQuad = createBakedQuadForFace(renderData, ITEM_RENDER_LAYER0, color, sprite, face);
			returnList.add(faceQuad);
		});
		
		return returnList;
	}
	
	private static BakedQuad createBakedQuadForFace(FluidRenderData data, int itemRenderLayer, int fluidColor, TextureAtlasSprite texture, Direction face)
	{
		float x1, x2, x3, x4;
		float y1, y2, y3, y4;
		float z1, z2, z3, z4;
		int packednormal;
		
		switch(face) {
		case UP:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			z1 = z4 = data.z + data.depth;
			z2 = z3 = data.z;
			y1 = y2 = y3 = y4 = data.y + data.getHeight();
			break;
		case DOWN:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			z1 = z4 = data.z;
			z2 = z3 = data.z + data.depth;
			y1 = y2 = y3 = y4 = data.y;
			break;
		case WEST:
			z1 = z2 = data.z + data.depth;
			z3 = z4 = data.z;
			y1 = y4 = data.y;
			y2 = y3 = data.y + data.getHeight();
			x1 = x2 = x3 = x4 = data.x;
			break;
		case EAST:
			z1 = z2 = data.z;
			z3 = z4 = data.z + data.depth;
			y1 = y4 = data.y;
			y2 = y3 = data.y + data.getHeight();
			x1 = x2 = x3 = x4 = data.x + data.width;
			break;
		case NORTH:
			x1 = x2 = data.x;
			x3 = x4 = data.x + data.width;
			y1 = y4 = data.y;
			y2 = y3 = data.y + data.getHeight();
			z1 = z2 = z3 = z4 = data.z;
			break;
		case SOUTH:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			y1 = y4 = data.y;
			y2 = y3 = data.y + data.getHeight();
			z1 = z2 = z3 = z4 = data.z + data.depth;
			break;
			default:
				throw new AssertionError("Unexpected Direction in createBakedQuadForFace:" + face);
		}
		
		// the order of the vertices on the face is (from the point of view of someone looking at the front face):
	    // 1 = bottom right, 2 = top right, 3 = top left, 4 = bottom left
		packednormal = calculatePackedNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
		
		final int BLOCK_LIGHT = 15;
		final int SKY_LIGHT = 15;
		int lightMapValue = LightTexture.packLight(BLOCK_LIGHT, SKY_LIGHT);
		
		final int minU = 0;
		final int maxU = 16;
		final int minV = 0;
		final int maxV = 16;
		int [] vertexData1 = vertexToInts(x1, y1, z1, fluidColor, texture, maxU, maxV, lightMapValue, packednormal);
		int [] vertexData2 = vertexToInts(x2, y2, z2, fluidColor, texture, maxU, minV, lightMapValue, packednormal);
	    int [] vertexData3 = vertexToInts(x3, y3, z3, fluidColor, texture, minU, minV, lightMapValue, packednormal);
	    int [] vertexData4 = vertexToInts(x4, y4, z4, fluidColor, texture, minU, maxV, lightMapValue, packednormal);
		int [] vertexDataAll = Ints.concat(vertexData1, vertexData2, vertexData3, vertexData4);
		final boolean APPLY_DIFFUSE_LIGHTING = true;
		return new BakedQuad(vertexDataAll, itemRenderLayer, face, texture, APPLY_DIFFUSE_LIGHTING);
	}
	
	private static int calculatePackedNormal(float x1, float y1, float z1, float x2, float y2, float z2,
	          float x3, float y3, float z3, float x4, float y4, float z4) {
		float xp = x4 - x2;
		float yp = y4 - y2;
		float zp = z4 - z2;
		
		float xq = x3 - x1;
		float yq = y3 - y1;
		float zq = z3 - z1;
		
		//Cross Product
		float xn = yq*zp - zq*yp;
		float yn = zq*xp - xq*zp;
		float zn = xq*yp - yq*xp;
		
		//Normalize
		float norm = (float)Math.sqrt(xn*xn + yn*yn + zn*zn);
		final float SMALL_LENGTH = 1.0E-4f;
		if(norm < SMALL_LENGTH) norm = 1f; //Protect against degenrate quad
		
		norm = 1f / norm;
		xn *= norm;
		yn *= norm;
		zn *= norm;
		
		int x = ((byte)(xn * 127)) & 0xFF;
		int y = ((byte)(yn * 127)) & 0xFF;
		int z = ((byte)(zn * 127)) & 0xFF;
		return x | (y << 0x08) | (z << 0x10);
	}
	
	private static int[] vertexToInts(float x, float y, float z, int color, TextureAtlasSprite texture, float u, float v, int lightmapvalue, int normal)
	{
		
		return new int[] {
				Float.floatToRawIntBits(x),
				Float.floatToRawIntBits(y),
				Float.floatToRawIntBits(z),
				color,
				Float.floatToRawIntBits(texture.getInterpolatedU(u)),
				Float.floatToRawIntBits(texture.getInterpolatedV(v)),
				lightmapvalue,
				normal
		};
		
	}
	
	public static class FluidSides
	{
		
		private static final List<Direction> BLACKLISTED_SIDES = ImmutableList.of(Direction.DOWN);
		
		public static final FluidSides ALL = new FluidSides(Direction.values());
		public static FluidSides Create(Direction... sides) { return new FluidSides(sides); }
		
		private final EnumMap<Direction,Boolean> map = new EnumMap<>(Direction.class);
		
		private FluidSides(Direction... sides)
		{
			Stream.of(Direction.values()).forEach(direction -> this.map.put(direction, false));
			Stream.of(sides).forEach(direction -> {
				if(!BLACKLISTED_SIDES.contains(direction))
					this.map.put(direction, true);
			});
		}
		
		public boolean test(Direction direction)
		{
			return this.map.get(direction);
		}
		
		public void forEach(Consumer<Direction> consumer)
		{
			for(Direction side : Direction.values())
			{
				if(test(side))
					consumer.accept(side);
			}
		}
		
	}
	
	public static class FluidRenderData
	{
		
		public final float x;
		public final float y;
		public final float z;
		public final float width;
		private final float height;
		public final float getHeight()
		{
			return height * fillPercent;
		}
		public final float depth;
		public final FluidSides sides;
		
		private float fillPercent = 1f;
		public void setFillPercent(float fillPercent) { this.fillPercent = MathUtil.clamp(fillPercent, 0f, 1f); }
		
		public FluidRenderData(float x, float y, float z, float width, float height, float depth, FluidSides sides)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.width = width;
			this.height = height;
			this.depth = depth;
			this.sides = sides;
		}
		
		public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth, FluidSides sides)
		{
			return new FluidRenderData(x/16f, y/16f, z/16f, width/16f, height/16f, depth/16f, sides);
		}
		
	}
	
	
	
}
