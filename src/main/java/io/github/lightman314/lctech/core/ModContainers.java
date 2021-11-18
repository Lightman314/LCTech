package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.container.*;
import io.github.lightman314.lctech.tileentities.*;
import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {

	private static final List<ContainerType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final ContainerType<FluidTraderContainer> FLUID_TRADER = register("fluid_trader", (IContainerFactory<FluidTraderContainer>)(windowId, playerInventory, data) -> {
		FluidTraderTileEntity tileEntity = (FluidTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new FluidTraderContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final ContainerType<FluidTraderStorageContainer> FLUID_TRADER_STORAGE = register("fluid_trader_storage", (IContainerFactory<FluidTraderStorageContainer>)(windowId, playerInventory, data) ->{
		FluidTraderTileEntity tileEntity = (FluidTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new FluidTraderStorageContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final ContainerType<FluidTraderContainerCR> FLUID_TRADER_CR = register("fluid_trader_cr", (IContainerFactory<FluidTraderContainerCR>)(windowId, playerInventory, data) -> {
		FluidTraderTileEntity tileEntity = (FluidTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new FluidTraderContainerCR(windowId, playerInventory, tileEntity, registerEntity);
	});
	
	public static final ContainerType<FluidEditContainer> FLUID_EDIT = register("fluid_edit", (IContainerFactory<FluidEditContainer>)(windowId, playerInventory, data) ->{
		FluidTraderTileEntity tileEntity = (FluidTraderTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		
		return new FluidEditContainer(windowId, playerInventory, () -> tileEntity, data.readInt());
	});
	
	private static <T extends Container> ContainerType<T> register(String key, ContainerType.IFactory<T> factory)
	{
		ContainerType<T> type = new ContainerType<>(factory);
		type.setRegistryName(key);
		CONTAINER_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<ContainerType<?>> event)
	{
		CONTAINER_TYPES.forEach(type -> event.getRegistry().register(type));
		CONTAINER_TYPES.clear();
	}
	
}
