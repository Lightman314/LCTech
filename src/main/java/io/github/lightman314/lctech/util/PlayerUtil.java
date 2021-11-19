package io.github.lightman314.lctech.util;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerUtil {

	/**
	 * Places the given item in the players inventory, or spawns it in the world should their inventory be full.
	 */
	public static void givePlayerItem(PlayerEntity player, ItemStack item)
	{
		if(!player.addItemStackToInventory(item) && !player.world.isRemote) {
			ItemEntity entity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), item);
			player.world.addEntity(entity);
		}
	}
	
}
