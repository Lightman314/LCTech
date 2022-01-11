package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.*;
import io.github.lightman314.lctech.container.*;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {

	private static final List<MenuType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final MenuType<FluidTraderContainer> FLUID_TRADER = register("fluid_trader", (IContainerFactory<FluidTraderContainer>)(windowId, playerInventory, data) -> {
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final MenuType<FluidTraderStorageContainer> FLUID_TRADER_STORAGE = register("fluid_trader_storage", (IContainerFactory<FluidTraderStorageContainer>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderStorageContainer(windowId, playerInventory, tileEntity);
	});
	
	public static final MenuType<FluidTraderContainerCR> FLUID_TRADER_CR = register("fluid_trader_cr", (IContainerFactory<FluidTraderContainerCR>)(windowId, playerInventory, data) -> {
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		CashRegisterBlockEntity registerEntity = (CashRegisterBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderContainerCR(windowId, playerInventory, tileEntity, registerEntity);
	});
	
	public static final MenuType<FluidEditContainer> FLUID_EDIT = register("fluid_edit", (IContainerFactory<FluidEditContainer>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		
		return new FluidEditContainer(windowId, playerInventory, () -> tileEntity, data.readInt());
	});
	
	public static final MenuType<UniversalFluidTraderContainer> UNIVERSAL_FLUID_TRADER = register("universal_fluid_trader", (IContainerFactory<UniversalFluidTraderContainer>)(windowId, playerInventory, data)->{
		return new UniversalFluidTraderContainer(windowId, playerInventory, data.readUUID());
	});
	
	public static final MenuType<UniversalFluidTraderStorageContainer> UNIVERSAL_FLUID_TRADER_STORAGE = register("universal_fluid_trader_storage", (IContainerFactory<UniversalFluidTraderStorageContainer>)(windowId, playerInventory, data)->{
		return new UniversalFluidTraderStorageContainer(windowId, playerInventory, data.readUUID());
	});
	
	public static final MenuType<UniversalFluidEditContainer> UNIVERSAL_FLUID_EDIT = register("universal_fluid_edit", (IContainerFactory<UniversalFluidEditContainer>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		
		return new UniversalFluidEditContainer(windowId, playerInventory, () -> tileEntity, data.readInt());
	});
	
	private static <T extends AbstractContainerMenu> MenuType<T> register(String key, MenuType.MenuSupplier<T> factory)
	{
		MenuType<T> type = new MenuType<>(factory);
		type.setRegistryName(key);
		CONTAINER_TYPES.add(type);
		return type;
	}
	
	@SubscribeEvent
	public static void registerTypes(final RegistryEvent.Register<MenuType<?>> event)
	{
		CONTAINER_TYPES.forEach(type -> event.getRegistry().register(type));
		CONTAINER_TYPES.clear();
	}
	
}
