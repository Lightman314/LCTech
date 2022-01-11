package io.github.lightman314.lctech.trader.settings;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.settings.fluid.FluidInputTab;
import io.github.lightman314.lctech.trader.permissions.FluidPermissions;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class FluidTraderSettings extends Settings{

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "fluid_settings");
	
	private static final String UPDATE_INPUT_SIDE = "updateInputSide";
	private static final String UPDATE_OUTPUT_SIDE = "updateOutputSide";
	
	public enum FluidHandlerSettings
	{
		DISABLED,
		INPUT_ONLY,
		OUTPUT_ONLY,
		INPUT_AND_OUTPUT
	}
	
	public FluidTraderSettings(ITrader trader, IMarkDirty markDirty, BiConsumer<ResourceLocation,CompoundTag> sendToServer) { super(trader, markDirty, sendToServer, TYPE); }
	
	DirectionalSettings inputSides = new DirectionalSettings();
	public DirectionalSettings getInputSides() { return this.inputSides; }
	DirectionalSettings outputSides = new DirectionalSettings();
	public DirectionalSettings getOutputSides() { return this.outputSides; }
	
	public FluidHandlerSettings getHandlerSetting(Direction side)
	{
		if(side == null)
			return FluidHandlerSettings.DISABLED;
		if(this.inputSides.get(side))
		{
			if(this.outputSides.get(side))
				return FluidHandlerSettings.INPUT_AND_OUTPUT;
			else
				return FluidHandlerSettings.INPUT_ONLY;
		}
		else if(this.outputSides.get(side))
			return FluidHandlerSettings.OUTPUT_ONLY;
		return FluidHandlerSettings.DISABLED;
	}
	
	public CompoundTag toggleInputSide(Player requestor, Direction side)
	{
		if(!this.trader.hasPermission(requestor, FluidPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", FluidPermissions.EDIT_INPUTS);
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
		if(!this.trader.hasPermission(requestor, FluidPermissions.EDIT_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", FluidPermissions.EDIT_INPUTS);
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
	}
	
	@Override
	public CompoundTag save(CompoundTag compound) {
		
		compound.put("InputSides", this.inputSides.save(new CompoundTag()));
		compound.put("OutputSides", this.outputSides.save(new CompoundTag()));
		
		return compound;
		
	}
	
	@Override
	public void load(CompoundTag compound) {
		
		this.inputSides.load(compound.getCompound("InputSides"));
		this.outputSides.load(compound.getCompound("OutputSides"));
		
	}

	//----------Client Only----------

	@Override
	public List<SettingsTab> getSettingsTabs() {
		return Lists.newArrayList(FluidInputTab.INSTANCE);
	}
	
	@Override
	public List<SettingsTab> getBackEndSettingsTabs() {
		return Lists.newArrayList();
	}
	
	@Override
	public List<PermissionOption> getPermissionOptions() {
		return Lists.newArrayList(BooleanPermission.of(FluidPermissions.EDIT_DRAINABILITY));
	}
	
}
