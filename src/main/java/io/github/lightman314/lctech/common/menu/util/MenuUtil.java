package io.github.lightman314.lctech.common.menu.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;

public class MenuUtil {

	/*
	 * Copy/paste of AbstractContainerMenu.clearContainer(Player,Container)
	 */
	public static void clearContainer(PlayerEntity player, IInventory container) {
		if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).hasDisconnected()) {
	         for(int j = 0; j < container.getContainerSize(); ++j) {
	        	 player.drop(container.removeItemNoUpdate(j), false);
	         }

	      } else {
	         for(int i = 0; i < container.getContainerSize(); ++i) {
	            PlayerInventory inventory = player.inventory;
	            if (inventory.player instanceof ServerPlayerEntity) {
	               inventory.placeItemBackInInventory(player.level, container.removeItemNoUpdate(i));
	            }
	         }

	      }
	}
	
}
