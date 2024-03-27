package io.github.lightman314.lctech;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.IntOption;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nonnull;

public class TechConfig {

	public static void init() {}

	public static final Common COMMON = new Common();

	public static final Server SERVER = new Server();

	public static class Common extends ConfigFile
	{

		protected Common() { super("lctech-common"); }

		//Crafting
		public final BooleanOption canCraftFluidTraders = BooleanOption.createTrue();
		public final BooleanOption canCraftFluidTanks = BooleanOption.createTrue();
		public final BooleanOption canCraftVoidTanks = BooleanOption.createTrue();
		public final BooleanOption canCraftEnergyTraders = BooleanOption.createTrue();
		public final BooleanOption canCraftBatteries = BooleanOption.createTrue();

		@Override
		protected void setup(@Nonnull ConfigBuilder configBuilder) {

			configBuilder.comment("Crafting Settings.").push("crafting");

			configBuilder.comment("Whether Fluid Traders can be crafted.",
							"Also affects crafting of fluid trader accessories (Fluid Trader Interface, Fluid Capacity Upgrades, etc.)",
							"Disabling will not remove any existing Fluid Traders from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.add("allowFluidTraderCrafting", this.canCraftFluidTraders);

			configBuilder.comment("Whether Fluid Tanks can be crafted.",
							"Disabling will not remove any existing fluid tanks from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.add("allowFluidTankCrafting", this.canCraftFluidTanks);

			configBuilder.comment("Wheter the Void Tank can be crafted.",
							"Disabling will not remove any existing fluid tanks from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.add("allowVoidTankCrafting", this.canCraftVoidTanks);

			configBuilder.comment("Whether Energy Traders can be crafted.",
							"Also affects crafting of energy trader accessories (Energy Trader Interface, Energy Capacity Upgrades, etc.)",
							"Disabling will not remove any existing Energy Traders from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.add("allowEnergyTraderCrafting", this.canCraftEnergyTraders);

			configBuilder.comment("Whether Batteries can be crafted.",
							"Disabling will not remove any existing Batteries from the world, nor prevent their use.",
							"/reload required for changes to take effect.")
					.add("allowBatteryCrafting", this.canCraftBatteries);

			configBuilder.pop();
		}
	}

	public static class Server extends SyncedConfigFile
	{

		protected Server() { super("lctech-server", new ResourceLocation(LCTech.MODID,"server")); }

		//Fluid
		//Fluid Trader
		public final IntOption fluidTraderDefaultStorage = IntOption.create(10,1,1000);
		public final IntOption fluidTradeMaxQuantity = IntOption.create(10,1);
		
		//Fluid Tanks
		
		public final IntOption ironTankCapacity = IntOption.create(10,1,1000);
		public final IntOption goldTankCapacity = IntOption.create(25,1,1000);
		public final IntOption diamondTankCapacity = IntOption.create(100,1,1000);
		
		//Fluid Upgrades
		
		public final IntOption fluidUpgradeCapacity1 = IntOption.create(10,1,1000);
		public final IntOption fluidUpgradeCapacity2 = IntOption.create(25,1,1000);
		public final IntOption fluidUpgradeCapacity3 = IntOption.create(100,1,1000);
		
		//Fluid Trader Interface
		public final IntOption fluidRestockSpeed = IntOption.create(10 * FluidType.BUCKET_VOLUME, 1);

		//Energy Trader
		public final IntOption energyTraderDefaultStorage = IntOption.create(100000, 1000, 100000000);
		public final IntOption energyTradeMaxQuantity = IntOption.create(Integer.MAX_VALUE, 1);
		
		//Batteries
		public final IntOption batteryCapacity = IntOption.create(100000, 1000);
		public final IntOption largeBatteryCapacity = IntOption.create(1000000, 1000);
		
		//Energy Upgrades
		public final IntOption energyUpgradeCapacity1 = IntOption.create(100000, 1000, 100000000);
		public final IntOption energyUpgradeCapacity2 = IntOption.create(250000, 1000, 100000000);
		public final IntOption energyUpgradeCapacity3 = IntOption.create(1000000, 1000, 100000000);
		
		//Energy Trader Interface
		public final IntOption energyRestockSpeed = IntOption.create(100000, 1000);

		@Override
		protected void setup(@Nonnull ConfigBuilder configBuilder) {

			configBuilder.comment("Fluid Settings").push("fluid");

			configBuilder.comment("Fluid Trader Settings").push("trader");

			configBuilder.comment("The amount of fluid storage a fluid trade has by default in Buckets (1,000mB).")
					.add("tradeStorage", this.fluidTraderDefaultStorage);

			configBuilder.comment("The maximum quantity of fluids allowed to be sold or purchased in a single trade in Buckets (1,000mB).",
							"Regardless of the input, it will always be enforced to be less than or equal to the fluid trades current maximum capacity.")
					.add("tradeQuantityLimit", this.fluidTradeMaxQuantity);

			configBuilder.pop();

			configBuilder.comment("Fluid Tank Settings").push("tank");

			configBuilder.comment("The amount of fluid storage the Iron Tank can hold in Buckets (1,000mB).")
					.add("ironTankCapacity", this.ironTankCapacity);

			configBuilder.comment("The amount of fluid storage the Gold Tank can hold in Buckets (1,000mB).")
					.add("goldTankCapacity", this.goldTankCapacity);

			configBuilder.comment("The amount of fluid storage the Diamond Tank can hold in Buckets (1,000mB).")
					.add("diamondTankCapacity", this.diamondTankCapacity);

			configBuilder.pop();

			configBuilder.comment("Fluid Upgrade Settings").push("upgrades");

			configBuilder.comment("The amount of fluid storage added by the first Fluid Capacity upgrade (Iron) in Buckets (1,000mB).")
					.add("upgradeCapacity1", this.fluidUpgradeCapacity1);

			configBuilder.comment("The amount of fluid storage added by the second Fluid Capacity upgrade (Gold) in Buckets (1,000mB).")
					.add("upgradeCapacity2", this.fluidUpgradeCapacity2);

			configBuilder.comment("The amount of fluid storage added by the third Fluid Capacity upgrade (Diamond) in Buckets (1,000mB).")
					.add("upgradeCapacity3", this.fluidUpgradeCapacity3);

			configBuilder.pop();

			configBuilder.comment("Fluid Trader Interface Settings").push("interface");

			configBuilder.comment("The amount of fluid in mB that can be drained or restocked in a single drain tick (once per second).")
					.add("restockRate", this.fluidRestockSpeed);

			configBuilder.pop().pop();

			configBuilder.comment("Energy Settings").push("energy");

			configBuilder.comment("Energy Trader Settings").push("trader");

			configBuilder.comment("The amount of FE an energy trader has by default.")
					.add("traderStorage", this.energyTraderDefaultStorage);
			configBuilder.comment("The maximum amount of FE an energy trader can sell or purchase in a single trade.",
							"Regardless of the input, it will always be enforced to be less than or equal to the energy traders current maximum capacity")
					.add("tradeQuantityLimit", this.energyTradeMaxQuantity);

			configBuilder.pop();

			configBuilder.comment("Battery Settings").push("battery");

			configBuilder.comment("The amount of FE a Battery can hold.")
					.add("batteryCapacity", this.batteryCapacity);

			configBuilder.comment("The amount of FE a Large Battery can hold.")
					.add("largeBatteryCapacity", this.largeBatteryCapacity);

			configBuilder.pop();

			configBuilder.comment("Energy Upgrade Settings").push("upgrades");

			configBuilder.comment("The amount of energy storage added by the first Energy Capacity Upgrade (Iron).")
					.add("upgradeCapacity1", this.energyUpgradeCapacity1);

			configBuilder.comment("The amount of energy storage added by the second Energy Capacity Upgrade (Gold).")
					.add("upgradeCapacity2", this.energyUpgradeCapacity2);

			configBuilder.comment("The amount of energy storage added by the third Energy Capacity Upgrade (Diamond).")
					.add("upgradeCapacity3", this.energyUpgradeCapacity3);

			configBuilder.pop();

			configBuilder.comment("Energy Interface Settings").push("interface");

			configBuilder.comment("The amount of FE that can be drained or restocked in a single drain tick (once per second).")
					.add("restockRate", this.energyRestockSpeed);

			configBuilder.pop().pop();
		}

	}
	
}
