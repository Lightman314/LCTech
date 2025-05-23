package io.github.lightman314.lctech.client.util;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FluidRenderUtil {

	public static void drawFluidTankInGUI(FluidStack tank, EasyGuiGraphics gui, int x, int y, int width, int height, float percent) { drawFluidTankInGUI(tank, gui.getOffset(), x, y, width, height, percent); }
	public static void drawFluidTankInGUI(FluidStack tank, ScreenPosition corner, int x, int y, int width, int height, float percent) { drawFluidTankInGUI(tank, corner.x + x, corner.y + y, width, height, percent); }
	public static void drawFluidTankInGUI(FluidStack tank, int x, int y, int width, int height, float percent)
	{
		if(tank == null || tank.isEmpty())
			return;
		
		IClientFluidTypeExtensions fluidRenderProperties = IClientFluidTypeExtensions.of(tank.getFluid());
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidRenderProperties.getStillTexture(tank));
		if(sprite != null)
		{	
			float minU = sprite.getU0();
			float maxU = sprite.getU1();
			float minV = sprite.getV0();
			float maxV = sprite.getV1();
			float deltaU = maxU - minU;
			float deltaV = maxV - minV;
			float tankLevel = Math.min(1f, percent) * height;
			int waterColor = fluidRenderProperties.getTintColor(tank);
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
				float subWidth = Math.min(16.0f, width - (16f * z));
				float offsetX = width - 16.0f * z - subWidth;
				for(int i = 0; i < count; i++)
				{
					float subHeight = Math.min(16.0f,  tankLevel - (16.0f * i));
					float offsetY = height - 16.0f * i - subHeight;
					drawQuad(x + offsetX, y + offsetY, subWidth, subHeight, (float) (maxU - deltaU * (subWidth / 16.0)), (float) (maxV - deltaV * (subHeight / 16.0)), maxU, maxV, red, green, blue);
				}
			}
			RenderSystem.disableBlend();
			
		}
		
	}

	private static void drawQuad(float x, float y, float width, float height, float minU, float minV, float maxU, float maxV, float red, float green, float blue)
	{
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		RenderSystem.setShaderColor(red, green, blue, 1f);
		buffer.addVertex(x, y + height, 0).setUv(minU, maxV);
		buffer.addVertex(x + width, y + height, 0).setUv(maxU, maxV);
		buffer.addVertex(x + width, y, 0).setUv(maxU, minV);
		buffer.addVertex(x, y, 0).setUv(minU, minV);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	public static void drawFluidInWorld(FluidStack tank, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, FluidRenderData renderData, int light)
	{
		drawFluidInWorld(tank, world, pos, matrixStack, renderTypeBuffer, renderData.x, renderData.y, renderData.z, renderData.width, renderData.height, renderData.depth, renderData.getHeight(), light, renderData.sides);
	}

	public static void drawFluidInWorld(FluidStack tank, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float x, float y, float z, float width, float top, float depth, float height, int light, FluidSides sides)
	{
		
		if(tank == null || tank.isEmpty())
			return;
		
		IClientFluidTypeExtensions fluidRenderProperties = IClientFluidTypeExtensions.of(tank.getFluid());
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidRenderProperties.getStillTexture(tank.getFluid().defaultFluidState(), world, pos));
		int waterColor = fluidRenderProperties.getTintColor(tank);
		float red = (float)(waterColor >> 16 & 255) / 255.0f;
		float green = (float)(waterColor >> 8 & 255) / 255.0f;
		float blue = (float)(waterColor & 255) / 255.0f;
		float minU = sprite.getU0();
		float minV = sprite.getV0();
		
		float x2 = x + width;
		float y2 = y + height;
		float z2 = z + depth;
		
		if(tank.getFluid().getFluidType().isLighterThanAir())
		{
			//Change y cords to match the inverted rendering
			y2 = top;
			y = y2 - height;
		}
		
		VertexConsumer buffer = renderTypeBuffer.getBuffer(RenderType.translucent());
		Matrix4f matrix = matrixStack.last().pose();
		UVCalculator uvCalculator = new UVCalculator(sprite,width,height,depth);
		
		//left side
		if(sides.test(Direction.WEST))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.WEST);
			buffer.addVertex(matrix, x2, y, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y2, z).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(),maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
		}
		
		//right side
		if(sides.test(Direction.EAST))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.EAST);
			buffer.addVertex(matrix, x, y, z2).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y, z2).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y2, z2).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
		}
		
		//south side
		if(sides.test(Direction.SOUTH))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.SOUTH);
			buffer.addVertex(matrix, x2, y, z2).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y, z).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
		}
		
		//north side
		if(sides.test(Direction.NORTH))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.NORTH);
			buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y, z2).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y2, z2).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y2, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
		}
		
		//top side
		if(sides.test(Direction.UP))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.UP);
			buffer.addVertex(matrix, x, y2, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x, y2, z2).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
			buffer.addVertex(matrix, x2, y2, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), maxUV.getSecond()).setLight(light).setNormal(0f, 1f, 0f);
		}
		
		//top side
		if(sides.test(Direction.DOWN))
		{
			Pair<Float,Float> maxUV = uvCalculator.getMaxUV(Direction.DOWN);
			buffer.addVertex(matrix, x2, y, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), minV).setLight(light).setNormal(0f, -1f, 0f);
			buffer.addVertex(matrix, x2, y, z2).setColor(red, green, blue, 1f).setUv(minU, minV).setLight(light).setNormal(0f, -1f, 0f);
			buffer.addVertex(matrix, x, y, z2).setColor(red, green, blue, 1f).setUv(minU, maxUV.getSecond()).setLight(light).setNormal(0f, -1f, 0f);
			buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, 1f).setUv(maxUV.getFirst(), maxUV.getSecond()).setLight(light).setNormal(0f, -1f, 0f);
		}
		
	}
	
	public static List<BakedQuad> getBakedFluidQuads(FluidStack tank, int capacity, FluidRenderData renderData)
	{
		
		if(tank.isEmpty())
			return Lists.newArrayList();
		
		IClientFluidTypeExtensions fluidRenderProperties = IClientFluidTypeExtensions.of(tank.getFluid());
		//Get sprite
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidRenderProperties.getStillTexture(tank));
		//Get color
		int fluidColor = fluidRenderProperties.getTintColor(tank);
		
		float fillPercent = MathUtil.clamp((float)tank.getAmount() / (float)capacity, 0f, 1f);
		renderData.setFillPercent(fillPercent);
		
		boolean inverted = tank.getFluid().getFluidType().isLighterThanAir();
		
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
		Vector3f normal;
		
		float bottom = data.y;
		float top = bottom + data.getHeight();
		if(inverted)
		{
			top = data.y + data.height;
			bottom = top - data.getHeight();
		}

		switch (face) {
			case UP -> {
				x1 = x2 = data.x + data.width;
				x3 = x4 = data.x;
				z1 = z4 = data.z + data.depth;
				z2 = z3 = data.z;
				y1 = y2 = y3 = y4 = top;
			}
			case DOWN -> {
				x1 = x2 = data.x + data.width;
				x3 = x4 = data.x;
				z1 = z4 = data.z;
				z2 = z3 = data.z + data.depth;
				y1 = y2 = y3 = y4 = bottom;
			}
			case WEST -> {
				z1 = z2 = data.z + data.depth;
				z3 = z4 = data.z;
				y1 = y4 = bottom;
				y2 = y3 = top;
				x1 = x2 = x3 = x4 = data.x;
			}
			case EAST -> {
				z1 = z2 = data.z;
				z3 = z4 = data.z + data.depth;
				y1 = y4 = bottom;
				y2 = y3 = top;
				x1 = x2 = x3 = x4 = data.x + data.width;
			}
			case NORTH -> {
				x1 = x2 = data.x;
				x3 = x4 = data.x + data.width;
				y1 = y4 = bottom;
				y2 = y3 = top;
				z1 = z2 = z3 = z4 = data.z;
			}
			case SOUTH -> {
				x1 = x2 = data.x + data.width;
				x3 = x4 = data.x;
				y1 = y4 = bottom;
				y2 = y3 = top;
				z1 = z2 = z3 = z4 = data.z + data.depth;
			}
			default -> throw new AssertionError("Unexpected Direction in createBakedQuadForFace:" + face);
		}
		
		// the order of the vertices on the face is (from the point of view of someone looking at the front face):
	    // 1 = bottom right, 2 = top right, 3 = top left, 4 = bottom left
		normal = calculatePackedNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
		
		final int BLOCK_LIGHT = 15;
		final int SKY_LIGHT = 15;
		int lightMapValue = LightTexture.pack(BLOCK_LIGHT, SKY_LIGHT);

		final float minU = texture.getU0();
		final float minV = texture.getV0();
		UVCalculator calculator = new UVCalculator(texture, data.width,data.getHeight(),data.depth);
		Pair<Float,Float> maxUV = calculator.getMaxUV(face);
		//If top/bottom, max V should be 1, otherwise limit by fill percent
		QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
		consumer.addVertex(x1,y1,z1).setColor(fluidColor).setUv(maxUV.getFirst(),maxUV.getSecond()).setLight(lightMapValue).setNormal(normal.x,normal.y,normal.z);
		consumer.addVertex(x2,y2,z2).setColor(fluidColor).setUv(maxUV.getFirst(),minV).setLight(lightMapValue).setNormal(normal.x,normal.y,normal.z);
		consumer.addVertex(x3,y3,z3).setColor(fluidColor).setUv(minU,minV).setLight(lightMapValue).setNormal(normal.x,normal.y,normal.z);
		consumer.addVertex(x4,y4,z4).setColor(fluidColor).setUv(minU,maxUV.getSecond()).setLight(lightMapValue).setNormal(normal.x,normal.y,normal.z);
		consumer.setTintIndex(itemRenderLayer);
		consumer.setDirection(face);
		consumer.setSprite(texture);
		consumer.setShade(true);
		return consumer.bakeQuad();
	}
	
	private static Vector3f calculatePackedNormal(float x1, float y1, float z1, float x2, float y2, float z2,
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

		return new Vector3f(xn,yn,zn);
	}

	private record UVCalculator(TextureAtlasSprite sprite, float width, float height, float depth)
	{
		private Pair<Float,Float> getMaxUV(Direction side)
		{
			return switch (side.getAxis()) {
				case Direction.Axis.X -> Pair.of(sprite.getU(depth),sprite.getV(height));
				case Direction.Axis.Y -> Pair.of(sprite.getU(width),sprite.getV(depth));
				case Direction.Axis.Z -> Pair.of(sprite.getU(width),sprite.getV(height));
			};
		}
	}
	
}
