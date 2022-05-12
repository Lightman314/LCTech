package io.github.lightman314.lctech.core;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRegistries {

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		BLOCKS.register(bus);
		BLOCK_ENTITIES.register(bus);
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LCTech.MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LCTech.MODID);
	
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LCTech.MODID);
	
}
