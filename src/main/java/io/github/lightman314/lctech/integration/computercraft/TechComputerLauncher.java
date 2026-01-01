package io.github.lightman314.lctech.integration.computercraft;

import net.neoforged.bus.api.IEventBus;

public class TechComputerLauncher {

    public static void setup(IEventBus bus)
    {
        TechComputerHelper.setup(bus);
    }

}
