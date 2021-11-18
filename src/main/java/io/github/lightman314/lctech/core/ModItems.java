package io.github.lightman314.lctech.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
	
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	//Fluid Shard
	public static final Item FLUID_SHARD = register("fluid_shard", new FluidShardItem(new Item.Properties().maxStackSize(1)));
	
	
	private static Item register(String name, Item item)
	{
		item.setRegistryName(name);
		ITEMS.add(item);
		return item;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
