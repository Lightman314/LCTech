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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class EnergyTraderSettings extends Settings{

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_settings");
	
	private static final String UPDATE_INPUT_SIDE = "updateInputSide";
	private static final String UPDATE_OUTPUT_SIDE = "updateOutputSide";
	
	public enum EnergyHandlerSettings
	{
		DISABLED,
		INPUT_ONLY,
		OUTPUT_ONLY,
		INPUT_AND_OUTPUT
	}
	
	public EnergyTraderSettings(ITrader trader, IMarkDirty markDirty, BiConsumer<ResourceLocation,CompoundNBT> sendToServer) { super(trader, markDirty, sendToServer, TYPE); }
	
	DirectionalSettings inputSides = new DirectionalSettings();
	public DirectionalSettings getInputSides() { return this.inputSides; }
	DirectionalSettings outputSides = new DirectionalSettings();
	public DirectionalSettings getOutputSides() { return this.outputSides; }
	
	public EnergyHandlerSettings getHandlerSettings(Direction side)
	{
		if(side == null)
			return EnergyHandlerSettings.DISABLED;
		if(this.inputSides.get(side))
		{
			if(this.outputSides.get(side))
				return EnergyHandlerSettings.INPUT_AND_OUTPUT;
			else
				return EnergyHandlerSettings.INPUT_ONLY;
		}
		else if(this.outputSides.get(side))
			return EnergyHandlerSettings.OUTPUT_ONLY;
		return EnergyHandlerSettings.DISABLED;
	}
	
	public CompoundNBT toggleInputSide(PlayerEntity requestor, Direction side)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.inputSides.set(side, !this.inputSides.get(side));
		boolean newValue = this.inputSides.get(side);
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "inputSide." + side.toString(), newValue);
		CompoundNBT updateInfo = initUpdateInfo(UPDATE_INPUT_SIDE);
		updateInfo.putInt("side", side.getIndex());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	public CompoundNBT toggleOutputSide(PlayerEntity requestor, Direction side)
	{
		if(!this.trader.hasPermission(requestor, EnergyPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", EnergyPermissions.EDIT_INPUTS);
			return null;
		}
		this.outputSides.set(side, !this.outputSides.get(side));
		boolean newValue = this.outputSides.get(side);
		this.trader.getCoreSettings().getLogger().LogSettingsChange(requestor, "outputSide." + side.toString(), newValue);
		CompoundNBT updateInfo = initUpdateInfo(UPDATE_OUTPUT_SIDE);
		updateInfo.putInt("side", side.getIndex());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	@Override
	public void changeSetting(PlayerEntity requestor, CompoundNBT updateInfo) {
		if(this.isUpdateType(updateInfo, UPDATE_INPUT_SIDE))
		{
			Direction side = Direction.byIndex(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.inputSides.get(side))
			{
				CompoundNBT result = this.toggleInputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OUTPUT_SIDE))
		{
			Direction side = Direction.byIndex(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.outputSides.get(side))
			{
				CompoundNBT result = this.toggleOutputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT compound) {
		
		compound.put("InputSides", this.inputSides.save(new CompoundNBT()));
		compound.put("OutputSides", this.outputSides.save(new CompoundNBT()));
		
		return compound;
		
	}
	
	@Override
	public void load(CompoundNBT compound) {
		
		this.inputSides.load(compound.getCompound("InputSides"));
		this.outputSides.load(compound.getCompound("OutputSides"));
		
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
