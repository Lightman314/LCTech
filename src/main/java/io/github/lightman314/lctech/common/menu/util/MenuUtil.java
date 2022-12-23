package io.github.lightman314.lctech.common.menu.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class MenuUtil {

	/*
	 * Copy/paste of AbstractContainerMenu.clearContainer(Player,Container)
	 */
	public static void clearContainer(Player player, Container container) {
		if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
	         for(int j = 0; j < container.getContainerSize(); ++j) {
	        	 player.drop(container.removeItemNoUpdate(j), false);
	         }

	      } else {
	         for(int i = 0; i < container.getContainerSize(); ++i) {
	            Inventory inventory = player.getInventory();
	            if (inventory.player instanceof ServerPlayer) {
	               inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
	            }
	         }

	      }
	}
	
}
