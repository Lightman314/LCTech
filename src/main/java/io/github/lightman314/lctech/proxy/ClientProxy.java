package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.blockentity.FluidTankBlockEntityRenderer;
import io.github.lightman314.lctech.client.renderer.blockentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.client.resourcepacks.data.model_variants.TechProperties;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import io.github.lightman314.lctech.common.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterVariantPropertiesEvent;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientProxy extends CommonProxy{

	@Override
	public boolean isClient() { return true; }

	@Override
	public void init(IEventBus eventBus) {
		//Setup Register Variant Properties event
		eventBus.addListener(this::registerVariantProperties);
		//Register normal event listeners
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void setupClient()
	{
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankBlockEntityRenderer::new);

		//Setup custom item renderers
		LCItemRenderer.registerBlockEntitySource(this::checkForFluidTanks);

	}

	private void registerVariantProperties(RegisterVariantPropertiesEvent event)
	{
		event.register(VersionUtil.modResource(LCTech.MODID,"fluid_render_data"),TechProperties.FLUID_RENDER_DATA);
	}

	private BlockEntity checkForFluidTanks(Block block)
	{
		if(block instanceof IFluidTankBlock)
			return new FluidTankBlockEntity(BlockPos.ZERO,block.defaultBlockState());
		return null;
	}

	@SubscribeEvent
	public void onLogin(ClientPlayerNetworkEvent.LoggingIn event)
	{
		//Initialize the fluid edit widgets fluid list
		FluidEditWidget.initFluidList();
	}

	@Override
	public void handleTankStackPacket(TankStackCache.PacketBuilder data) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.level != null)
		{
			data.build(mc.level).init(false);
		}
	}
	
}