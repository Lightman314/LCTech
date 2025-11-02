package io.github.lightman314.lctech.proxy;

import com.google.common.base.Suppliers;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.blockentity.FluidTankBlockEntityRenderer;
import io.github.lightman314.lctech.client.renderer.blockentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.client.resourcepacks.data.model_variants.TechProperties;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigSelectionScreen;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterVariantPropertiesEvent;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

public class ClientProxy extends CommonProxy{

	@Override
	public void init() {
		//Setup Register Variant Properties event
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerVariantProperties);
		//Register normal event listeners
		MinecraftForge.EVENT_BUS.register(this);
        FMLModContainer container = FMLJavaModLoadingContext.get().getContainer();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, Suppliers.memoize(() -> ConfigSelectionScreen.createFactory(container,TechConfig.COMMON,TechConfig.SERVER)));
	}

	@Override
	public void setupClient()
	{
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankBlockEntityRenderer::new);
	}

	private void registerVariantProperties(RegisterVariantPropertiesEvent event)
	{
		event.register(VersionUtil.modResource(LCTech.MODID,"fluid_render_data"),TechProperties.FLUID_RENDER_DATA);
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