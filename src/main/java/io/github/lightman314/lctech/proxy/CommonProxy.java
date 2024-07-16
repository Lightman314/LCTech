package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;

public class CommonProxy {

	public boolean isClient() { return false; }

	public void setupClient() {}

	public void handleTankStackPacket(TankStackCache.PacketBuilder data) { }
	
}
