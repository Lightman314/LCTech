package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.*;
import io.github.lightman314.lctech.menu.*;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {

	private static final List<MenuType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final MenuType<FluidTraderMenu> FLUID_TRADER = register("fluid_trader", (IContainerFactory<FluidTraderMenu>)(windowId, playerInventory, data) -> {
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderMenu(windowId, playerInventory, tileEntity);
	});
	
	public static final MenuType<FluidTraderStorageMenu> FLUID_TRADER_STORAGE = register("fluid_trader_storage", (IContainerFactory<FluidTraderStorageMenu>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderStorageMenu(windowId, playerInventory, tileEntity);
	});
	
	public static final MenuType<FluidTraderMenuCR> FLUID_TRADER_CR = register("fluid_trader_cr", (IContainerFactory<FluidTraderMenuCR>)(windowId, playerInventory, data) -> {
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		CashRegisterBlockEntity registerEntity = (CashRegisterBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new FluidTraderMenuCR(windowId, playerInventory, tileEntity, registerEntity);
	});
	
	public static final MenuType<FluidEditMenu> FLUID_EDIT = register("fluid_edit", (IContainerFactory<FluidEditMenu>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		
		return new FluidEditMenu(windowId, playerInventory, () -> tileEntity, data.readInt());
	});
	
	public static final MenuType<UniversalFluidTraderMenu> UNIVERSAL_FLUID_TRADER = register("universal_fluid_trader", (IContainerFactory<UniversalFluidTraderMenu>)(windowId, playerInventory, data)->{
		return new UniversalFluidTraderMenu(windowId, playerInventory, data.readUUID());
	});
	
	public static final MenuType<UniversalFluidTraderStorageMenu> UNIVERSAL_FLUID_TRADER_STORAGE = register("universal_fluid_trader_storage", (IContainerFactory<UniversalFluidTraderStorageMenu>)(windowId, playerInventory, data)->{
		return new UniversalFluidTraderStorageMenu(windowId, playerInventory, data.readUUID());
	});
	
	public static final MenuType<UniversalFluidEditMenu> UNIVERSAL_FLUID_EDIT = register("universal_fluid_edit", (IContainerFactory<UniversalFluidEditMenu>)(windowId, playerInventory, data) ->{
		FluidTraderBlockEntity tileEntity = (FluidTraderBlockEntity)playerInventory.player.level.getBlockEntity(data.readBlockPos());
		
		return new UniversalFluidEditMenu(windowId, playerInventory, () -> tileEntity, data.readInt());
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
