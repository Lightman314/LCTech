package io.github.lightman314.lctech.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blocks.*;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.BlockItemPair;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
	
	private enum BlockItemType { NORMAL, FLUID_TANK }
	
	private static final List<Block> BLOCKS = Lists.newArrayList();
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	//Fluid Tanks
	public static final BlockItemPair IRON_TANK = register("iron_tank", BlockItemType.FLUID_TANK, LightmansCurrency.MACHINE_GROUP, new FluidTankBlock(
			10 * FluidAttributes.BUCKET_VOLUME,
			Block.Properties.create(Material.GLASS)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, 5.0f)
			.sound(SoundType.GLASS)
			));
	public static final BlockItemPair GOLD_TANK = register("gold_tank", BlockItemType.FLUID_TANK, LightmansCurrency.MACHINE_GROUP, new FluidTankBlock(
			25 * FluidAttributes.BUCKET_VOLUME,
			Block.Properties.create(Material.GLASS)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, 5.0f)
			.sound(SoundType.GLASS)
			));
	
	//Fluid traders
	public static final BlockItemPair FLUID_TAP = register("fluid_tap", LightmansCurrency.TRADING_GROUP, new FluidTapBlock(
			Block.Properties.create(Material.GLASS)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.GLASS),
			Block.makeCuboidShape(4d, 0d, 4d, 12d, 16d, 12d)
			));
	
	public static final BlockItemPair FLUID_TAP_BUNDLE = register("fluid_tap_bundle", LightmansCurrency.TRADING_GROUP, new FluidTapBundleBlock(
			Block.Properties.create(Material.GLASS)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.GLASS)
			));
	
	//Universal Fluid Traders
	public static final BlockItemPair FLUID_SERVER_SML = register("fluid_trader_server_sml", LightmansCurrency.TRADING_GROUP, new FluidTraderServerBlock(
			2,
			Block.Properties.create(Material.IRON)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			));
	
	public static final BlockItemPair FLUID_SERVER_MED = register("fluid_trader_server_med", LightmansCurrency.TRADING_GROUP, new FluidTraderServerBlock(
			4,
			Block.Properties.create(Material.IRON)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			));
	public static final BlockItemPair FLUID_SERVER_LRG = register("fluid_trader_server_lrg", LightmansCurrency.TRADING_GROUP, new FluidTraderServerBlock(
			6,
			Block.Properties.create(Material.IRON)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			));
	public static final BlockItemPair FLUID_SERVER_XLRG = register("fluid_trader_server_xlrg", LightmansCurrency.TRADING_GROUP, new FluidTraderServerBlock(
			8,
			Block.Properties.create(Material.IRON)
			.harvestTool(ToolType.PICKAXE)
			.notSolid()
			.hardnessAndResistance(3.0f, Float.POSITIVE_INFINITY)
			.sound(SoundType.METAL)
			));
	
	private static BlockItemPair register(String name, ItemGroup itemGroup, Block block) { return register(name, BlockItemType.NORMAL, itemGroup, block); }
	
	private static BlockItemPair register(String name, BlockItemType type, ItemGroup itemGroup, Block block)
	{
		block.setRegistryName(name);
		BLOCKS.add(block);
		if(block.getRegistryName() != null)
		{
			Item item = null;
			switch(type)
			{
				case FLUID_TANK:
					item = new FluidTankItem(block, new Item.Properties().group(itemGroup));
					break;
				default:
					item = new BlockItem(block, new Item.Properties().group(itemGroup));
			}
			if(item != null)
			{
				item.setRegistryName(name);
				ITEMS.add(item);
			}
			return new BlockItemPair(block,item);
		}
		return new BlockItemPair(block,null);
	}
	
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event)
	{
		BLOCKS.forEach(block -> event.getRegistry().register(block));
		BLOCKS.clear();
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
