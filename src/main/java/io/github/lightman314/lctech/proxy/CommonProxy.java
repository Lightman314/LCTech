package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public class CommonProxy {

	public boolean isClient() { return false; }

	public void init(IEventBus eventBus, ModContainer container) {}

	public void setupClient() {}

	public void handleTankStackPacket(TankStackCache.PacketBuilder data) { }
	
}
