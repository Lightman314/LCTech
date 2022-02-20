package io.github.lightman314.lctech.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.container.*;
import io.github.lightman314.lctech.container.EnergyTraderContainer.EnergyTraderContainerCR;
import io.github.lightman314.lctech.container.EnergyTraderContainer.EnergyTraderContainerUniversal;
import io.github.lightman314.lctech.container.EnergyTraderStorageContainer.EnergyTraderStorageContainerUniversal;
import io.github.lightman314.lctech.container.FluidEditContainer.UniversalFluidEditContainer;
import io.github.lightman314.lctech.container.FluidTraderContainer.FluidTraderContainerCR;
import io.github.lightman314.lctech.container.FluidTraderContainer.FluidTraderContainerUniversal;
import io.github.lightman314.lctech.container.FluidTraderStorageContainer.FluidTraderStorageContainerUniversal;
import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {

	private static final List<ContainerType<?>> CONTAINER_TYPES = new ArrayList<>();
	
	public static final ContainerType<FluidTraderContainer> FLUID_TRADER = register("fluid_trader", (IContainerFactory<FluidTraderContainer>)(windowId, playerInventory, data) -> {
		return new FluidTraderContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<FluidTraderContainerCR> FLUID_TRADER_CR = register("fluid_trader_cr", (IContainerFactory<FluidTraderContainerCR>)(windowId, playerInventory, data) -> {
		BlockPos traderPos = data.readBlockPos();
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new FluidTraderContainerCR(windowId, playerInventory, traderPos, registerEntity);
	});
	public static final ContainerType<FluidTraderContainerUniversal> UNIVERSAL_FLUID_TRADER = register("universal_fluid_trader", (IContainerFactory<FluidTraderContainerUniversal>)(windowId, playerInventory, data)->{
		return new FluidTraderContainerUniversal(windowId, playerInventory, data.readUniqueId());
	});
	
	public static final ContainerType<FluidTraderStorageContainer> FLUID_TRADER_STORAGE = register("fluid_trader_storage", (IContainerFactory<FluidTraderStorageContainer>)(windowId, playerInventory, data) ->{
		return new FluidTraderStorageContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<FluidTraderStorageContainerUniversal> UNIVERSAL_FLUID_TRADER_STORAGE = register("universal_fluid_trader_storage", (IContainerFactory<FluidTraderStorageContainerUniversal>)(windowId, playerInventory, data)->{
		return new FluidTraderStorageContainerUniversal(windowId, playerInventory, data.readUniqueId());
	});
	
	
	public static final ContainerType<FluidEditContainer> FLUID_EDIT = register("fluid_edit", (IContainerFactory<FluidEditContainer>)(windowId, playerInventory, data) ->{
		return new FluidEditContainer(windowId, playerInventory, data.readBlockPos(), data.readInt());
	});
	
	public static final ContainerType<UniversalFluidEditContainer> UNIVERSAL_FLUID_EDIT = register("universal_fluid_edit", (IContainerFactory<UniversalFluidEditContainer>)(windowId, playerInventory, data) ->{
		return new UniversalFluidEditContainer(windowId, playerInventory, data.readUniqueId(), data.readInt());
	});
	
	public static final ContainerType<EnergyTraderContainer> ENERGY_TRADER = register("energy_trader", (IContainerFactory<EnergyTraderContainer>)(windowId, playerInventory, data) ->{
		return new EnergyTraderContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<EnergyTraderContainerCR> ENERGY_TRADER_CR = register("energy_trader_cr", (IContainerFactory<EnergyTraderContainerCR>)(windowId, playerInventory, data) ->{
		BlockPos traderPos = data.readBlockPos();
		CashRegisterTileEntity registerEntity = (CashRegisterTileEntity)playerInventory.player.world.getTileEntity(data.readBlockPos());
		return new EnergyTraderContainerCR(windowId, playerInventory, traderPos, registerEntity);
	});
	public static final ContainerType<EnergyTraderContainerUniversal> ENERGY_TRADER_UNIVERSAL = register("energy_trader_universal", (IContainerFactory<EnergyTraderContainerUniversal>)(windowId, playerInventory, data) ->{
		return new EnergyTraderContainerUniversal(windowId, playerInventory, data.readUniqueId());
	});
	
	public static final ContainerType<EnergyTraderStorageContainer> ENERGY_TRADER_STORAGE = register("energy_trader_storage", (IContainerFactory<EnergyTraderStorageContainer>)(windowId, playerInventory, data) ->{
		return new EnergyTraderStorageContainer(windowId, playerInventory, data.readBlockPos());
	});
	public static final ContainerType<EnergyTraderStorageContainerUniversal> ENERGY_TRADER_STORAGE_UNIVERSAL = register("energy_trader_storage_universal", (IContainerFactory<EnergyTraderStorageContainerUniversal>)(windowId, playerInventory, data) ->{
		return new EnergyTraderStorageContainerUniversal(windowId, playerInventory, data.readUniqueId());
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
