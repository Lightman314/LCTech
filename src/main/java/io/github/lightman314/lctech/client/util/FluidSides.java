package io.github.lightman314.lctech.client.util;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Direction;

public class FluidSides
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
