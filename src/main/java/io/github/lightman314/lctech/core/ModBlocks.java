package io.github.lightman314.lctech.core;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.blocks.*;
import io.github.lightman314.lctech.blocks.traderblocks.*;
import io.github.lightman314.lctech.blocks.traderinterface.*;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;

@ObjectHolder(LCTech.MODID)
public class ModBlocks {
	
	private static BiFunction<Block,CreativeModeTab,Item> getDefaultGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new BlockItem(block, properties);
		};
	}
	private static BiFunction<Block,CreativeModeTab,Item> getFluidTankGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new FluidTankItem(block, properties);
		};
	}
	
	public static void init() {
		
		register("iron_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.ironTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.sound(SoundType.GLASS)
				));
		register("gold_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.goldTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.sound(SoundType.GLASS)
				));
		register("diamond_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.diamondTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.sound(SoundType.GLASS)
				));
		
		register("fluid_tap", LightmansCurrency.TRADING_GROUP, () -> new FluidTapBlock(
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS),
				Block.box(4d, 0d, 4d, 12d, 16d, 12d)
				));
		register("fluid_tap_bundle", LightmansCurrency.TRADING_GROUP, () -> new FluidTapBundleBlock(
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.GLASS)
				));
		
		register("fluid_trader_server_sml", LightmansCurrency.TRADING_GROUP, () -> new FluidTraderServerBlock(
				FluidTraderServerBlock.SMALL_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		register("fluid_trader_server_med", LightmansCurrency.TRADING_GROUP, () -> new FluidTraderServerBlock(
				FluidTraderServerBlock.MEDIUM_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		register("fluid_trader_server_lrg", LightmansCurrency.TRADING_GROUP, () -> new FluidTraderServerBlock(
				FluidTraderServerBlock.LARGE_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		register("fluid_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, () -> new FluidTraderServerBlock(
				FluidTraderServerBlock.EXTRA_LARGE_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		
		register("fluid_trader_interface", LightmansCurrency.MACHINE_GROUP, () -> new FluidTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		
		register("battery_shop", LightmansCurrency.TRADING_GROUP, () -> new EnergyTraderBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		
		register("energy_trader_server", LightmansCurrency.TRADING_GROUP, () -> new EnergyTraderServerBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		
		register("energy_trader_interface", LightmansCurrency.MACHINE_GROUP, () -> new EnergyTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.sound(SoundType.METAL)
				));
		
	}
	
	//Fluid Tanks
	public static final Block IRON_TANK = null;
	public static final Block GOLD_TANK = null;
	public static final Block DIAMOND_TANK = null;
	
	//Fluid traders
	public static final Block FLUID_TAP = null;
	
	public static final Block FLUID_TAP_BUNDLE = null;
	
	//Universal Fluid Traders
	@ObjectHolder("fluid_trader_server_sml")
	public static final Block FLUID_SERVER_SML = null;
	@ObjectHolder("fluid_trader_server_med")
	public static final Block FLUID_SERVER_MED = null;
	@ObjectHolder("fluid_trader_server_lrg")
	public static final Block FLUID_SERVER_LRG = null;
	@ObjectHolder("fluid_trader_server_xlrg")
	public static final Block FLUID_SERVER_XLRG = null;
	
	//Fluid Trader Interface
	public static final Block FLUID_TRADER_INTERFACE = null;
	
	//Energy Trader
	public static final Block BATTERY_SHOP = null;
	
	//Universal Energy Trader
	@ObjectHolder("energy_trader_server")
	public static final Block ENERGY_SERVER = null;
	
	//Energy Trader Interface
	public static final Block ENERGY_TRADER_INTERFACE = null;
	
	/**
	* Block Registration Code
	*/
	private static void register(String name, CreativeModeTab itemGroup, Supplier<Block> sup)
	{
		register(name, itemGroup, getDefaultGenerator(), sup);
	}
	
	private static void register(String name, CreativeModeTab itemGroup, BiFunction<Block,CreativeModeTab,Item> itemGenerator, Supplier<Block> sup)
	{
		RegistryObject<Block> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get(), itemGroup));
	}
	
}
