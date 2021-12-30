package io.github.lightman314.lctech.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerUtil {

	/**
	 * Places the given item in the players inventory, or spawns it in the world should their inventory be full.
	 */
	public static void givePlayerItem(Player player, ItemStack item)
	{
		if(!player.addItem(item) && !player.level.isClientSide) {
			ItemEntity entity = new ItemEntity(player.level, player.position().x, player.position().y, player.position().z, item);
			player.level.addFreshEntity(entity);
		}
	}
	
}
