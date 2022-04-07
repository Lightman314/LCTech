package io.github.lightman314.lctech;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class TechConfig {

	public static class Server
	{
		
		//Fluid
		//Fluid Trader
		public final ForgeConfigSpec.IntValue fluidTraderDefaultStorage;
		public final ForgeConfigSpec.IntValue fluidTradeMaxQuantity;
		
		//Fluid Tanks
		public final ForgeConfigSpec.IntValue ironTankCapacity;
		public final ForgeConfigSpec.IntValue goldTankCapacity;
		public final ForgeConfigSpec.IntValue diamondTankCapacity;
		
		//Fluid Upgrades
		public final ForgeConfigSpec.IntValue fluidUpgradeCapacity1;
		public final ForgeConfigSpec.IntValue fluidUpgradeCapacity2;
		public final ForgeConfigSpec.IntValue fluidUpgradeCapacity3;
		
		//Energy
		//Energy Trader
		public final ForgeConfigSpec.IntValue energyTraderDefaultStorage;
		public final ForgeConfigSpec.IntValue energyTradeMaxQuantity;
		
		//Batteries
		public final ForgeConfigSpec.IntValue batteryCapacity;
		public final ForgeConfigSpec.IntValue largeBatteryCapacity;
		
		//Energy Upgrades
		public final ForgeConfigSpec.IntValue energyUpgradeCapacity1;
		public final ForgeConfigSpec.IntValue energyUpgradeCapacity2;
		public final ForgeConfigSpec.IntValue energyUpgradeCapacity3;
		
		Server(ForgeConfigSpec.Builder builder)
		{
			
			builder.comment("Fluid Settings").push("fluid");
			
			builder.comment("Fluid Trader Settings").push("trader");
			
			this.fluidTraderDefaultStorage = builder.comment("The amount of fluid storage a fluid trade has by default in Buckets (1,000mB).")
					.defineInRange("tradeStorage", 10, 1, 1000);
			
			this.fluidTradeMaxQuantity = builder.comment("The maximum quantity of fluids allowed to be sold or purchased in a single trade in Buckets (1,000mB).",
					"Regardless of the input, it will always be enforced to be less than or equal to the fluid trades current maximum capacity.")
					.defineInRange("tradeQuantityLimit", 10, 1, Integer.MAX_VALUE);
			
			builder.pop();
			
			builder.comment("Fluid Tank Settings").push("tank");
			
			this.ironTankCapacity = builder.comment("The amount of fluid storage the Iron Tank can hold in Buckets (1,000mB).")
					.defineInRange("ironTankCapacity", 10, 1, 1000);
			
			this.goldTankCapacity = builder.comment("The amount of fluid storage the Gold Tank can hold in Buckets (1,000mB).")
					.defineInRange("goldTankCapacity", 25, 1, 1000);
			
			this.diamondTankCapacity = builder.comment("The amount of fluid storage the Diamond Tank can hold in Buckets (1,000mB).")
					.defineInRange("diamondTankCapacity", 100, 1, 1000);
			
			builder.pop();
			
			builder.comment("Fluid Upgrade Settings").push("upgrades");
			
			this.fluidUpgradeCapacity1 = builder.comment("The amount of fluid storage added by the first Fluid Capacity upgrade (Iron) in Buckets (1,000mB).")
					.defineInRange("upgradeCapacity1", 10, 1, 1000);
			
			this.fluidUpgradeCapacity2 = builder.comment("The amount of fluid storage added by the second Fluid Capacity upgrade (Gold) in Buckets (1,000mB).")
					.defineInRange("upgradeCapacity2", 25, 1, 1000);
			
			this.fluidUpgradeCapacity3 = builder.comment("The amount of fluid storage added by the third Fluid Capacity upgrade (Diamond) in Buckets (1,000mB).")
					.defineInRange("upgradeCapacity3", 100, 1, 1000);
			
			builder.pop();
			builder.pop();
			
			builder.comment("Energy Settings").push("energy");
			
			builder.comment("Energy Trader Settings").push("trader");
			
			this.energyTraderDefaultStorage = builder.comment("The amount of FE an energy trader has by default.")
					.defineInRange("traderStorage", 100000, 1000, 100000000);
			this.energyTradeMaxQuantity = builder.comment("The maximum amount of FE an energy trader can sell or purchase in a single trade.",
					"Regardless of the input, it will always be enforced to be less than or equal to the energy traders current maximum capacity")
					.defineInRange("tradeQuantityLimit", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
			
			builder.pop();
			
			builder.comment("Battery Settings").push("battery");
			
			this.batteryCapacity = builder.comment("The amount of FE a Battery can hold.")
					.defineInRange("batteryCapacity", 100000, 1000, Integer.MAX_VALUE);
			
			this.largeBatteryCapacity = builder.comment("The amount of FE a Large Battery can hold.")
					.defineInRange("largeBatteryCapacity", 1000000, 1000, Integer.MAX_VALUE);
			
			builder.pop();
			
			builder.comment("Energy Upgrade Settings").push("upgrades");
			
			this.energyUpgradeCapacity1 = builder.comment("The amount of energy storage added by the first Energy Capacity Upgrade (Iron).")
					.defineInRange("upgradeCapacity1", 100000, 1000, 100000000);
			
			this.energyUpgradeCapacity2 = builder.comment("The amount of energy storage added by the second Energy Capacity Upgrade (Gold).")
					.defineInRange("upgradeCapacity2", 250000, 1000, 100000000);
			
			this.energyUpgradeCapacity3 = builder.comment("The amount of energy storage added by the third Energy Capacity Upgrade (Diamond).")
					.defineInRange("upgradeCapacity3", 1000000, 1000, 100000000);
			
			builder.pop();
			
			builder.pop();
			
		}
		
	}
	
	public static final ForgeConfigSpec serverSpec;
	public static final TechConfig.Server SERVER;
	
	static
	{
		//Server
		final Pair<Server,ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
		serverSpec = serverPair.getRight();
		SERVER = serverPair.getLeft();
	}
	
}
