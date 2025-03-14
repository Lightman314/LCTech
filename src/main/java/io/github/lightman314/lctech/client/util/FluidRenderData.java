package io.github.lightman314.lctech.client.util;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import org.jetbrains.annotations.Range;

public class FluidRenderData
{
	
	public final float x;
	public final float y;
	public final float z;
	public final float width;
	public final float height;
	public final float getHeight()
	{
		return height * fillPercent;
	}
	public final float depth;
	public final FluidSides sides;
	
	private float fillPercent = 1f;
	public float getFillPercent() { return this.fillPercent; }
	public void setFillPercent(float fillPercent) { this.fillPercent = MathUtil.clamp(fillPercent, 0f, 1f); }
	
	private FluidRenderData(float x, float y, float z, float width, float height, float depth, FluidSides sides)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.sides = sides;
	}

	public FluidRenderData withSides(FluidSides sidesOverride) { return new FluidRenderData(this.x, this.y, this.z, this.width, this.height, this.depth, sidesOverride); }
	public FluidRenderData withSides(boolean shouldOverride, FluidSides sidesOverride) { return shouldOverride ? this.withSides(sidesOverride) : this; }
	public FluidRenderData withSides(boolean overrideFlag, FluidSides sidesOverride1, FluidSides sidesOverride2) { return overrideFlag ? this.withSides(sidesOverride1) : this.withSides(sidesOverride2); }

	public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth)
	{
		return CreateFluidRender(x, y, z, width, height, depth, FluidSides.ALL);
	}
	
	public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth, FluidSides sides)
	{
		return new FluidRenderData(x/16f, y/16f, z/16f, width/16f, height/16f, depth/16f, sides);
	}
	
}
