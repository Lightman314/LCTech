package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {

	public static void register(IEventBus bus) {
		
		ITEMS.register(bus);
		ModItems.init();
		BLOCKS.register(bus);
		ModBlocks.init();
		
		BLOCK_ENTITIES.register(bus);
		ModBlockEntities.init();

		DATA_COMPONENTS.register(bus);
		ModDataComponents.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LCTech.MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LCTech.MODID);
	
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LCTech.MODID);

	public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, LCTech.MODID);

}
