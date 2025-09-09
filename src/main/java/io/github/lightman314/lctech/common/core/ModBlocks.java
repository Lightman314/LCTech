package io.github.lightman314.lctech.common.core;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.blocks.*;
import io.github.lightman314.lctech.common.blocks.traderblocks.*;
import io.github.lightman314.lctech.common.blocks.traderinterface.*;
import io.github.lightman314.lctech.common.items.*;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidType;

public class ModBlocks {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	private static Function<Block,Item> getDefaultGenerator() {
		return (block) -> new BlockItem(block, new Item.Properties());
	}
	private static Function<Block,Item> getFluidTankGenerator() {
		return (block) -> new FluidTankItem(block, new Item.Properties());
	}
	private static Function<Block,Item> getNetheriteTankGenerator() {
		return (block) -> new FluidTankItem(block, new Item.Properties().fireResistant());
	}
	private static Function<Block,Item> getVoidTankGenerator() {
		return (block) -> new VoidTankItem(block, new Item.Properties());
	}

    private static Block getVoidTankDrop() { return VOID_TANK.get(); }
	
	static {
		
		IRON_TANK = register("iron_tank", getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.ironTankCapacity.get() * FluidType.BUCKET_VOLUME,
				Block.Properties.of()
						.mapColor(MapColor.METAL)
						.strength(3.0f, 5.0f)
						.sound(SoundType.GLASS)
				));
		GOLD_TANK = register("gold_tank", getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.goldTankCapacity.get() * FluidType.BUCKET_VOLUME,
				Block.Properties.of()
						.mapColor(MapColor.GOLD)
						.strength(3.0f, 5.0f)
						.sound(SoundType.GLASS)
				));
		DIAMOND_TANK = register("diamond_tank", getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.diamondTankCapacity.get() * FluidType.BUCKET_VOLUME,
				Block.Properties.of()
						.mapColor(MapColor.DIAMOND)
						.strength(3.0f, 5.0f)
						.sound(SoundType.GLASS)
				));
		NETHERITE_TANK = register("netherite_tank", getNetheriteTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.netheriteTankCapacity.get() * FluidType.BUCKET_VOLUME,
				Block.Properties.of()
						.mapColor(MapColor.DIAMOND)
						.strength(3.0f, 5.0f)
						.sound(SoundType.GLASS)
		));

		VOID_TANK = register("void_tank", getVoidTankGenerator(), () -> new VoidTankBlock(
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.COLOR_BLACK)
						.strength(25f, 600f)
						.sound(SoundType.GLASS)
		));
		
		FLUID_TAP = register("fluid_tap", () -> new FluidTapBlock(
				Block.Properties.of()
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.GLASS),
						Block.box(4d, 0d, 4d, 12d, 16d, 12d)
				));
		FLUID_TAP_BUNDLE = register("fluid_tap_bundle", () -> new FluidTapBundleBlock(
				Block.Properties.of()
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.GLASS)
				));
		
		FLUID_NETWORK_TRADER_1 = register("fluid_trader_server_sml", () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.SMALL_SERVER_COUNT,
				Block.Properties.of()
						.mapColor(MapColor.COLOR_BLUE)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_2 = register("fluid_trader_server_med", () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.MEDIUM_SERVER_COUNT,
				Block.Properties.of()
						.mapColor(MapColor.COLOR_BLUE)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_3 = register("fluid_trader_server_lrg", () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.LARGE_SERVER_COUNT,
				Block.Properties.of()
						.mapColor(MapColor.COLOR_BLUE)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_4 = register("fluid_trader_server_xlrg", () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.EXTRA_LARGE_SERVER_COUNT,
				Block.Properties.of()
						.mapColor(MapColor.COLOR_BLUE)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		
		FLUID_TRADER_INTERFACE = register("fluid_trader_interface", () -> new FluidTraderInterfaceBlock(
				Block.Properties.of()
						.mapColor(MapColor.COLOR_BLUE)
						.strength(5.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		
		BATTERY_SHOP = register("battery_shop", () -> new EnergyTraderBlock(
				Block.Properties.of()
						.mapColor(MapColor.COLOR_GRAY)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL),
				LazyShapes.BOX_T
				)
		);
		
		ENERGY_NETWORK_TRADER = register("energy_trader_server", () -> new NetworkEnergyTraderBlock(
				Block.Properties.of()
						.mapColor(MapColor.COLOR_GRAY)
						.strength(3.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		
		ENERGY_TRADER_INTERFACE = register("energy_trader_interface", () -> new EnergyTraderInterfaceBlock(
				Block.Properties.of()
						.mapColor(MapColor.COLOR_GRAY)
						.strength(5.0f, Float.POSITIVE_INFINITY)
						.sound(SoundType.METAL)
				));
		
	}
	
	//Fluid Tanks
	public static final Supplier<Block> IRON_TANK;
	public static final Supplier<Block> GOLD_TANK;
	public static final Supplier<Block> DIAMOND_TANK;
	public static final Supplier<Block> NETHERITE_TANK;
	public static final Supplier<Block> VOID_TANK;
	
	//Fluid traders
	public static final Supplier<Block> FLUID_TAP;
	
	public static final Supplier<Block> FLUID_TAP_BUNDLE;
	
	//Universal Fluid Traders
	public static final Supplier<Block> FLUID_NETWORK_TRADER_1;
	public static final Supplier<Block> FLUID_NETWORK_TRADER_2;
	public static final Supplier<Block> FLUID_NETWORK_TRADER_3;
	public static final Supplier<Block> FLUID_NETWORK_TRADER_4;
	
	//Fluid Trader Interface
	public static final Supplier<Block> FLUID_TRADER_INTERFACE;
	
	//Energy Trader
	public static final Supplier<Block> BATTERY_SHOP;
	
	//Universal Energy Trader
	public static final Supplier<Block> ENERGY_NETWORK_TRADER;
	
	//Energy Trader Interface
	public static final Supplier<Block> ENERGY_TRADER_INTERFACE;
	
	/**
	* Block Registration Code
	*/
	private static Supplier<Block> register(String name, Supplier<Block> sup)
	{
		return register(name, getDefaultGenerator(), sup);
	}
	
	private static Supplier<Block> register(String name, Function<Block,Item> itemGenerator, Supplier<Block> sup)
	{
		Supplier<Block> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get()));
		return block;
	}
	
}
