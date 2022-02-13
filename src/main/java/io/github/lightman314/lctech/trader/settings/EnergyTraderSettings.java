package io.github.lightman314.lctech.trader.settings;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.settings.energy.EnergyInputTab;
import io.github.lightman314.lctech.trader.permissions.EnergyPermissions;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class EnergyTraderSettings extends Settings{

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_settings");
	
	private static final String UPDATE_INPUT_SIDE = "updateInputSide";
	private static final String UPDATE_OUTPUT_SIDE = "updateOutputSide";
	private static final String UPDATE_OUTPUT_MODE = "updateOutputMode";
	
	public enum DrainMode { ALWAYS(0), PURCHASES_ONLY(1);
		
		public final int index;
		DrainMode(int index) { this.index = index; }
		
		public static DrainMode of(int index) {
			for(DrainMode mode : DrainMode.values())
				if(mode.index == index)
					return mode;
			return DrainMode.ALWAYS;
		}
		
	}
	
	public EnergyTraderSettings(ITrader trader, IMarkDirty markDirty, BiConsumer<ResourceLocation,CompoundTag> sendToServer) { super(trader, markDirty, sendToServer, TYPE); }
	
	DirectionalSettings inputSides = new DirectionalSettings();
	public DirectionalSettings getInputSides() { return this.inputSides; }
	DirectionalSettings outputSides = new DirectionalSettings();
	public DirectionalSettings getOutputSides() { return this.outputSides; }
	private DrainMode drainMode = DrainMode.PURCHASES_ONLY;
	public boolean isAlwaysDrainMode() { return this.drainMode == DrainMode.ALWAYS; }
	public boolean isPurchaseDrainMode() { return this.drainMode == DrainMode.PURCHASES_ONLY; }
	//private boolean exportEnergy = false;
	//public boolean exportEnergy() { return this.exportEnergy; }
	
	public CompoundTag toggleInputSide(Player requestor, Direction side)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.inputSides.set(side, !this.inputSides.get(side));
		boolean newValue = this.inputSides.get(side);
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "inputSide." + side.toString(), newValue);
		CompoundTag updateInfo = initUpdateInfo(UPDATE_INPUT_SIDE);
		updateInfo.putInt("side", side.get3DDataValue());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	public CompoundTag toggleOutputSide(Player requestor, Direction side)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.outputSides.set(side, !this.outputSides.get(side));
		boolean newValue = this.outputSides.get(side);
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "outputSide." + side.toString(), newValue);
		CompoundTag updateInfo = initUpdateInfo(UPDATE_OUTPUT_SIDE);
		updateInfo.putInt("side", side.get3DDataValue());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	public CompoundTag toggleDrainMode(Player requestor)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external drain mode", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.drainMode = DrainMode.of(this.drainMode.index + 1);
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "outputMode", this.drainMode.name());
		CompoundTag updateInfo = initUpdateInfo(UPDATE_OUTPUT_MODE);
		updateInfo.putInt("newValue", this.drainMode.index);
		return updateInfo;
	}
	
	/*public CompoundTag toggleExportEnergy(Player requestor)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external drain mode", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.exportEnergy = !this.exportEnergy;
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "exportEnergy", this.exportEnergy);
		CompoundTag updateInfo = initUpdateInfo(UPDATE_EXPORT_ENERGY);
		updateInfo.putBoolean("newValue", this.exportEnergy);
		return updateInfo;
	}*/
	
	@Override
	public void changeSetting(Player requestor, CompoundTag updateInfo) {

		if(this.isUpdateType(updateInfo, UPDATE_INPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.inputSides.get(side))
			{
				CompoundTag result = this.toggleInputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OUTPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.outputSides.get(side))
			{
				CompoundTag result = this.toggleOutputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OUTPUT_MODE))
		{
			DrainMode newValue = DrainMode.of(updateInfo.getInt("newValue"));
			if(newValue != this.drainMode)
			{
				CompoundTag result = this.toggleDrainMode(requestor);
				if(result != null)
					this.markDirty();
			}
		}
		/*else if(this.isUpdateType(updateInfo, UPDATE_EXPORT_ENERGY))
		{
			boolean newValue = updateInfo.getBoolean("newValue");
			if(newValue != this.exportEnergy)
			{
				CompoundTag result = this.toggleExportEnergy(requestor);
				if(result != null)
					this.markDirty();
			}
		}*/
	}
	
	@Override
	public CompoundTag save(CompoundTag compound) {
		
		compound.put("InputSides", this.inputSides.save(new CompoundTag()));
		compound.put("OutputSides", this.outputSides.save(new CompoundTag()));
		compound.putInt("DrainMode", this.drainMode.index);
		
		return compound;
		
	}
	
	@Override
	public void load(CompoundTag compound) {
		
		this.inputSides.load(compound.getCompound("InputSides"));
		this.outputSides.load(compound.getCompound("OutputSides"));
		
		if(compound.contains("DrainMode", Tag.TAG_INT))
			this.drainMode = DrainMode.of(compound.getInt("DrainMode"));
		
	}
	
	//----------Client Only----------

	@Override
	public List<SettingsTab> getSettingsTabs() {
		return Lists.newArrayList(EnergyInputTab.INSTANCE);
	}
	
	@Override
	public List<SettingsTab> getBackEndSettingsTabs() {
		return Lists.newArrayList();
	}
	
	@Override
	public List<PermissionOption> getPermissionOptions() {
		return Lists.newArrayList(BooleanPermission.of(EnergyPermissions.EDIT_DRAINABILITY));
	}
	
}
