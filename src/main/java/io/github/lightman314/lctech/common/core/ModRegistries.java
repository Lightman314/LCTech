package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRegistries {

	public static void register(IEventBus bus) {
		
		ITEMS.register(bus);
		ModItems.init();
		BLOCKS.register(bus);
		ModBlocks.init();
		
		BLOCK_ENTITIES.register(bus);
		ModBlockEntities.init();
		
		MENUS.register(bus);
		ModMenus.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LCTech.MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LCTech.MODID);
	
	public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, LCTech.MODID);
	
	public static final DeferredRegister<ContainerType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, LCTech.MODID);
	
}