package io.github.lightman314.lctech.util;

import java.util.UUID;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeSupplier;

public class SafeTradingOffice {

	public static UniversalTraderData getData(UUID traderID)
	{
		return DistExecutor.safeRunForDist(() -> new ClientGetter(traderID), () -> new ServerGetter(traderID));
	}
	
	public static UniversalFluidTraderData getFluidData(UUID traderID) {
		UniversalTraderData data = getData(traderID);
		if(data instanceof UniversalFluidTraderData)
			return (UniversalFluidTraderData)data;
		return null;
	}
	
	private static class ClientGetter implements SafeSupplier<UniversalTraderData>
	{
		private static final long serialVersionUID = 6653014137959343476L;
		final UUID traderID;
		public ClientGetter(UUID traderID) { this.traderID = traderID; }
		@Override
		public UniversalTraderData get() { return ClientTradingOffice.getData(this.traderID); }
	}
	private static class ServerGetter implements SafeSupplier<UniversalTraderData>
	{
		private static final long serialVersionUID = -1092326795456459138L;
		final UUID traderID;
		public ServerGetter(UUID traderID) { this.traderID = traderID; }
		@Override
		public UniversalTraderData get() { return TradingOffice.getData(this.traderID); }
	}
	
}
