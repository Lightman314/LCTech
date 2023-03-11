package io.github.lightman314.lctech.client.util;

import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import net.minecraft.util.Direction;

public class FluidSides
{

	public static final FluidSides ALL = Create(Direction.values());
	public static final FluidSides NO_TOP = Create(side -> side != Direction.UP);
	public static final FluidSides NO_BOTTOM = Create(side -> side != Direction.DOWN);
	public static final FluidSides NO_TOP_OR_BOTTOM = Create(side -> !side.getAxis().isVertical());
	public static FluidSides Create(Direction... sides) { return new FluidSides(sides); }
	public static FluidSides Create(Predicate<Direction> allowSide) { return new FluidSides(Stream.of(Direction.values()).filter(allowSide).collect(Collectors.toList())); }

	private final EnumMap<Direction,Boolean> map = new EnumMap<>(Direction.class);

	private FluidSides(Iterable<Direction> sides) {
		Stream.of(Direction.values()).forEach(direction -> this.map.put(direction, false));
		sides.forEach(direction -> this.map.put(direction, true));
	}

	private FluidSides(Direction... sides) { this(Lists.newArrayList(sides)); }

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