package io.github.lightman314.lctech.common.core;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.blocks.*;
import io.github.lightman314.lctech.common.blocks.traderblocks.*;
import io.github.lightman314.lctech.common.blocks.traderinterface.*;
import io.github.lightman314.lctech.common.items.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.RegistryObject;

public class ModBlocks {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	private static BiFunction<Block,ItemGroup,Item> getDefaultGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new BlockItem(block, properties);
		};
	}
	private static BiFunction<Block,ItemGroup,Item> getFluidTankGenerator() {
		return (block, tab) -> {
			Item.Properties properties = new Item.Properties();
			if(tab != null)
				properties.tab(tab);
			return new FluidTankItem(block, properties);
		};
	}
	
	static {
		
		IRON_TANK = register("iron_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.ironTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.GLASS)
				));
		GOLD_TANK = register("gold_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.goldTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.GLASS)
				));
		DIAMOND_TANK = register("diamond_tank", LightmansCurrency.MACHINE_GROUP, getFluidTankGenerator(), () -> new FluidTankBlock(
				() -> TechConfig.SERVER.diamondTankCapacity.get() * FluidAttributes.BUCKET_VOLUME,
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, 5.0f)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.GLASS)
				));
		
		FLUID_TAP = register("fluid_tap", LightmansCurrency.TRADING_GROUP, () -> new FluidTapBlock(
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.GLASS),
				Block.box(4d, 0d, 4d, 12d, 16d, 12d)
				));
		FLUID_TAP_BUNDLE = register("fluid_tap_bundle", LightmansCurrency.TRADING_GROUP, () -> new FluidTapBundleBlock(
				Block.Properties.of(Material.GLASS)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.GLASS)
				));
		
		FLUID_NETWORK_TRADER_1 = register("fluid_trader_server_sml", LightmansCurrency.TRADING_GROUP, () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.SMALL_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_2 = register("fluid_trader_server_med", LightmansCurrency.TRADING_GROUP, () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.MEDIUM_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_3 = register("fluid_trader_server_lrg", LightmansCurrency.TRADING_GROUP, () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.LARGE_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		FLUID_NETWORK_TRADER_4 = register("fluid_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, () -> new NetworkFluidTraderBlock(
				NetworkFluidTraderBlock.EXTRA_LARGE_SERVER_COUNT,
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		
		FLUID_TRADER_INTERFACE = register("fluid_trader_interface", LightmansCurrency.MACHINE_GROUP, () -> new FluidTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		
		BATTERY_SHOP = register("battery_shop", LightmansCurrency.TRADING_GROUP, () -> new EnergyTraderBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		
		ENERGY_NETWORK_TRADER = register("energy_trader_server", LightmansCurrency.TRADING_GROUP, () -> new NetworkEnergyTraderBlock(
				Block.Properties.of(Material.METAL)
				.strength(3.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		
		ENERGY_TRADER_INTERFACE = register("energy_trader_interface", LightmansCurrency.MACHINE_GROUP, () -> new EnergyTraderInterfaceBlock(
				Block.Properties.of(Material.METAL)
				.strength(5.0f, Float.POSITIVE_INFINITY)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				));
		
	}
	
	//Fluid Tanks
	public static final RegistryObject<Block> IRON_TANK;
	public static final RegistryObject<Block> GOLD_TANK;
	public static final RegistryObject<Block> DIAMOND_TANK;
	
	//Fluid traders
	public static final RegistryObject<Block> FLUID_TAP;
	
	public static final RegistryObject<Block> FLUID_TAP_BUNDLE;
	
	//Universal Fluid Traders
	public static final RegistryObject<Block> FLUID_NETWORK_TRADER_1;
	public static final RegistryObject<Block> FLUID_NETWORK_TRADER_2;
	public static final RegistryObject<Block> FLUID_NETWORK_TRADER_3;
	public static final RegistryObject<Block> FLUID_NETWORK_TRADER_4;
	
	//Fluid Trader Interface
	public static final RegistryObject<Block> FLUID_TRADER_INTERFACE;
	
	//Energy Trader
	public static final RegistryObject<Block> BATTERY_SHOP;
	
	//Universal Energy Trader
	public static final RegistryObject<Block> ENERGY_NETWORK_TRADER;
	
	//Energy Trader Interface
	public static final RegistryObject<Block> ENERGY_TRADER_INTERFACE;
	
	/**
	* Block Registration Code
	*/
	private static RegistryObject<Block> register(String name, ItemGroup itemGroup, Supplier<Block> sup)
	{
		return register(name, itemGroup, getDefaultGenerator(), sup);
	}
	
	private static RegistryObject<Block> register(String name, ItemGroup itemGroup, BiFunction<Block,ItemGroup,Item> itemGenerator, Supplier<Block> sup)
	{
		RegistryObject<Block> block = ModRegistries.BLOCKS.register(name, sup);
		if(block != null)
			ModRegistries.ITEMS.register(name, () -> itemGenerator.apply(block.get(), itemGroup));
		return block;
	}
	
}