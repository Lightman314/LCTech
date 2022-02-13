package io.github.lightman314.lctech.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class DirectionalUtil {
	
	/**
	 * Undoes the getRelativeSide calculations to get the true direction of the relative side
	 */
	public static Direction getTrueSide(Direction facing, Direction relativeSide)
	{
		if(relativeSide == null)
			return relativeSide;
		if(relativeSide.getAxis() == Axis.Y)
			return relativeSide;
		//Since my facings are backwards, invert it
		if(facing.getAxis() == Axis.Z)
			facing = facing.getOpposite();
		return Direction.from2DDataValue(facing.get2DDataValue() - relativeSide.get2DDataValue());
	}
}
