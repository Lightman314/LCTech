package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.menu.traderstorage.AddRemoveTradeEditTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.BasicTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class AddRemoveTradeEditClientTab extends BasicTradeEditClientTab<AddRemoveTradeEditTab> {

	public static final ResourceLocation EXTRAS = new ResourceLocation(LCTech.MODID, "textures/gui/extras.png");
	
	public AddRemoveTradeEditClientTab(TraderStorageScreen screen, AddRemoveTradeEditTab commonTab) { super(screen, commonTab); }
	
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	@Override
	public void onOpen() {
		
		super.onOpen();
		
		this.buttonAddTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getXSize() - 25, this.screen.getGuiTop() + 4, 10, 10, this::AddTrade, EXTRAS, 0, 0));
		this.buttonRemoveTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getXSize() - 14, this.screen.getGuiTop() + 4, 10, 10, this::RemoveTrade, EXTRAS, 10, 0));
		
	}
	
	@Override
	public void tick() {
		
		super.tick();
		
		ITrader trader = this.menu.getTrader();
		if(trader != null)
		{
			this.buttonAddTrade.active = trader.getTradeCount() < trader.getTradeCountLimit();
			this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
		}
		
	}
	
	private void AddTrade(Button button) { this.commonTab.addTrade(); }
	
	private void RemoveTrade(Button button) { this.commonTab.removeTrade(); }
}
