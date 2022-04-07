package io.github.lightman314.lctech.client.util;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
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
		
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tank.getFluid().getAttributes().getStillTexture());
		if(sprite != null)
		{	
			float minU = sprite.getU0();
			float maxU = sprite.getU1();
			float minV = sprite.getV0();
			float maxV = sprite.getV1();
			float deltaU = maxU - minU;
			float deltaV = maxV - minV;
			double tankLevel = Math.min(1d, percent) * height;
			int waterColor = tank.getFluid().getAttributes().getColor(tank);
			float red = (float)(waterColor >> 16 & 255) / 255.0f;
			float green = (float)(waterColor >> 8 & 255) / 255.0f;
			float blue = (float)(waterColor & 255) / 255.0f;
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			
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
	
	private static void drawQuad(double x, double y, double width, double height, float minU, float minV, float maxU, float maxV, float red, float green, float blue)
	{
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		RenderSystem.setShaderColor(red, green, blue, 1f);
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(x, y + height, 0).uv(minU, maxV).endVertex();
		buffer.vertex(x + width, y + height, 0).uv(maxU, maxV).endVertex();
		buffer.vertex(x + width, y, 0).uv(maxU, minV).endVertex();
		buffer.vertex(x, y, 0).uv(minU, minV).endVertex();
		tessellator.end();
	}
	
	public static void drawFluidInWorld(FluidStack tank, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, FluidRenderData renderData, int light)
	{
		drawFluidInWorld(tank, world, pos, matrixStack, renderTypeBuffer, renderData.x, renderData.y, renderData.z, renderData.width, renderData.height, renderData.depth, renderData.getHeight(), light, renderData.sides);
	}
	
	public static void drawFluidInWorld(FluidStack tank, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float x, float y, float z, float width, float top, float depth, float height, int light, FluidSides sides)
	{
		
		if(tank.isEmpty())
			return;
		
		TextureAtlasSprite sprite = ForgeHooksClient.getFluidSprites(world,  pos,  tank.getFluid().defaultFluidState())[0];
		int waterColor = tank.getFluid().getAttributes().getColor(tank);
		float red = (float)(waterColor >> 16 & 255) / 255.0f;
		float green = (float)(waterColor >> 8 & 255) / 255.0f;
		float blue = (float)(waterColor & 255) / 255.0f;
		float minU = sprite.getU0();
		float maxU = Math.min(minU + (sprite.getU1() - minU) * depth, sprite.getU1());
		float minV = sprite.getV0();
		float maxV = Math.min(minV + (sprite.getV1() - minV) * height, sprite.getV1());
		
		float x2 = x + width;
		float y2 = y + height;
		float z2 = z + depth;
		
		if(tank.getFluid().getAttributes().isLighterThanAir())
		{
			//Change y cords to match the inverted rendering
			y2 = top;
			y = y2 - height;
		}
		
		VertexConsumer buffer = renderTypeBuffer.getBuffer(RenderType.translucent());
		Matrix4f matrix = matrixStack.last().pose();
		
		//left side
		if(sides.test(Direction.WEST))
		{
			buffer.vertex(matrix, x2, y, z).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y, z).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y2, z).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
		}
		
		//right side
		if(sides.test(Direction.EAST))
		{
			buffer.vertex(matrix, x, y, z2).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y, z2).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y2, z2).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
		}
		
		maxU = Math.min(minU + (sprite.getU1() - minU), sprite.getU1());
		
		//south side
		if(sides.test(Direction.SOUTH))
		{
			buffer.vertex(matrix, x2, y, z2).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y, z).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
		}
		
		//north side
		if(sides.test(Direction.NORTH))
		{
			buffer.vertex(matrix, x, y, z).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y, z2).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y2, z2).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y2, z).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
		}
		
		maxV = Math.min(minV + (sprite.getV1() - minV), sprite.getV1());
		
		//top side
		if(sides.test(Direction.UP))
		{
			buffer.vertex(matrix, x, y2, z).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x, y2, z2).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y2, z).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, 1f, 0f).endVertex();
		}
		
		//top side
		if(sides.test(Direction.DOWN))
		{
			buffer.vertex(matrix, x2, y, z).color(red, green, blue, 1f).uv(maxU, minV).uv2(light).normal(0f, -1f, 0f).endVertex();
			buffer.vertex(matrix, x2, y, z2).color(red, green, blue, 1f).uv(minU, minV).uv2(light).normal(0f, -1f, 0f).endVertex();
			buffer.vertex(matrix, x, y, z2).color(red, green, blue, 1f).uv(minU, maxV).uv2(light).normal(0f, -1f, 0f).endVertex();
			buffer.vertex(matrix, x, y, z).color(red, green, blue, 1f).uv(maxU, maxV).uv2(light).normal(0f, -1f, 0f).endVertex();
		}
		
	}
	
	public static List<BakedQuad> getBakedFluidQuads(FluidStack tank, int capacity, FluidRenderData renderData)
	{
		
		if(tank.isEmpty())
			return Lists.newArrayList();
		
		//Get sprite
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tank.getFluid().getAttributes().getStillTexture());
		//Get color
		int fluidColor = tank.getFluid().getAttributes().getColor(tank);
		if(fluidColor != 0xFFFFFFFF)
		{
			int red = fluidColor >> 16 & 255;
			int green = fluidColor >> 8 & 255;
			int blue = fluidColor & 255;
			fluidColor = 0xFF000000 | red | green << 8 | blue << 16;
		}
		
		double fillPercent = MathUtil.clamp((double)tank.getAmount() / (double)capacity, 0d, 1d);
		renderData.setFillPercent((float)fillPercent);
		
		boolean inverted = tank.getFluid().getAttributes().isLighterThanAir();
		
		final int ITEM_RENDER_LAYER0 = 0;
		
		List<BakedQuad> returnList = Lists.newArrayList();
		
		final int color = fluidColor;
		renderData.sides.forEach(face ->{
			BakedQuad faceQuad = createBakedQuadForFace(renderData, ITEM_RENDER_LAYER0, color, sprite, face, inverted);
			returnList.add(faceQuad);
		});
		
		return returnList;
	}
	
	private static BakedQuad createBakedQuadForFace(FluidRenderData data, int itemRenderLayer, int fluidColor, TextureAtlasSprite texture, Direction face, boolean inverted)
	{
		float x1, x2, x3, x4;
		float y1, y2, y3, y4;
		float z1, z2, z3, z4;
		int packednormal;
		
		float bottom = data.y;
		float top = bottom + data.getHeight();
		if(inverted)
		{
			top = data.y + data.height;
			bottom = top - data.getHeight();
		}
		
		switch(face) {
		case UP:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			z1 = z4 = data.z + data.depth;
			z2 = z3 = data.z;
			y1 = y2 = y3 = y4 = top;
			break;
		case DOWN:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			z1 = z4 = data.z;
			z2 = z3 = data.z + data.depth;
			y1 = y2 = y3 = y4 = bottom;
			break;
		case WEST:
			z1 = z2 = data.z + data.depth;
			z3 = z4 = data.z;
			y1 = y4 = bottom;
			y2 = y3 = top;
			x1 = x2 = x3 = x4 = data.x;
			break;
		case EAST:
			z1 = z2 = data.z;
			z3 = z4 = data.z + data.depth;
			y1 = y4 = bottom;
			y2 = y3 = top;
			x1 = x2 = x3 = x4 = data.x + data.width;
			break;
		case NORTH:
			x1 = x2 = data.x;
			x3 = x4 = data.x + data.width;
			y1 = y4 = bottom;
			y2 = y3 = top;
			z1 = z2 = z3 = z4 = data.z;
			break;
		case SOUTH:
			x1 = x2 = data.x + data.width;
			x3 = x4 = data.x;
			y1 = y4 = bottom;
			y2 = y3 = top;
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
		int lightMapValue = LightTexture.pack(BLOCK_LIGHT, SKY_LIGHT);
		
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
				Float.floatToRawIntBits(texture.getU(u)),
				Float.floatToRawIntBits(texture.getV(v)),
				lightmapvalue,
				normal
		};
		
	}
	
	public static class FluidSides
	{
		
		private static final List<Direction> BLACKLISTED_SIDES = ImmutableList.of();
		
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
		
		public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth)
		{
			return CreateFluidRender(x, y, z, width, height, depth, FluidSides.ALL);
		}
		
		public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth, FluidSides sides)
		{
			return new FluidRenderData(x/16f, y/16f, z/16f, width/16f, height/16f, depth/16f, sides);
		}
		
	}
	
	
	
}
