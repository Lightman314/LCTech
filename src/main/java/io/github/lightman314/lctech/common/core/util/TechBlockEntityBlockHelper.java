package io.github.lightman314.lctech.common.core.util;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.resources.ResourceLocation;

public class TechBlockEntityBlockHelper {

	public static final ResourceLocation FLUID_TRADER_TYPE = new ResourceLocation(LCTech.MODID, "fluid_trader");
	public static final ResourceLocation ENERGY_TRADER_TYPE = new ResourceLocation(LCTech.MODID, "energy_trader");
	
	public static void init() {}
	
	static {
		BlockEntityBlockHelper.addBlocksToBlockEntity(FLUID_TRADER_TYPE, ModBlocks.FLUID_TAP, ModBlocks.FLUID_TAP_BUNDLE, ModBlocks.FLUID_NETWORK_TRADER_1,
				ModBlocks.FLUID_NETWORK_TRADER_2, ModBlocks.FLUID_NETWORK_TRADER_3, ModBlocks.FLUID_NETWORK_TRADER_4);
		BlockEntityBlockHelper.addBlocksToBlockEntity(ENERGY_TRADER_TYPE, ModBlocks.BATTERY_SHOP, ModBlocks.ENERGY_NETWORK_TRADER);
	}
	
}