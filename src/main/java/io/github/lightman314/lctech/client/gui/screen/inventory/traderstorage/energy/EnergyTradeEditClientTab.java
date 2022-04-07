package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class EnergyTradeEditClientTab extends TraderStorageClientTab<EnergyTradeEditTab> implements InteractionConsumer, ICoinValueInput{

	public EnergyTradeEditClientTab(TraderStorageScreen screen, EnergyTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }
	
	@Override
	public Component getTooltip() { return new TextComponent(""); }
	
	@Override
	public boolean tabButtonVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }
	
	TradeButton tradeDisplay;
	CoinValueInput priceSelection;
	
	EditBox quantityInput;
	
	Button buttonToggleTradeType;
	
	private int selection;
	
	@Override
	public void onOpen() {
		
		EnergyTradeData trade = this.commonTab.getTrade();
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 22, this.screen.getGuiTop() + 18);
		this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiTop() + 40, new TextComponent(""), trade == null ? CoinValue.EMPTY : trade.getCost(), this));
		this.priceSelection.drawBG = false;
		this.priceSelection.init();
		
		this.quantityInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 75, this.screen.getXSize() - 42 - this.font.width(EnergyUtil.ENERGY_UNIT), 20, new TextComponent("")));
		this.quantityInput.setValue(trade != null ? String.valueOf(trade.getAmount()): "");
		
		this.buttonToggleTradeType = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 15, 80, 20, new TextComponent(""), this::ToggleTradeType));
		
	}
	
	@Override
	public void onClose() { this.selection = -1; }
	
	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		//Render an arrow to the left of the selected position
		RenderSystem.setShaderTexture(0, EnergyStorageClientTab.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		this.screen.blit(pose, this.screen.getGuiLeft() + 2, this.screen.getGuiTop() + this.getArrowPosition(), 36, 0, 18, 18);
		
		if(this.selection >= 0)
			this.font.drawShadow(pose, EnergyUtil.ENERGY_UNIT, this.screen.getGuiLeft() + this.screen.getXSize() - 20 - this.font.width(EnergyUtil.ENERGY_UNIT), this.screen.getGuiTop() + 78, 0xFFFFFF);
		
	}
	
	private int getArrowPosition() {
		if(this.getTrade().isSale())
		{
			if(this.selection < 0)
				return 18;
			else
				return 35;
		}
		else
		{
			if(this.selection < 0)
				return 32;
			else
				return 15;
		}
	}
	
	private void validateRenderables() {
		
		this.priceSelection.visible = this.selection < 0;
		if(this.priceSelection.visible)
			this.priceSelection.tick();
		this.quantityInput.visible = this.selection >= 0;
		if(this.quantityInput.visible)
		{
			int maxSellAmount = Integer.MAX_VALUE;
			if(this.menu.getTrader() instanceof IEnergyTrader)
				maxSellAmount = ((IEnergyTrader)this.menu.getTrader()).getMaxEnergy();
			TextInputUtil.whitelistInteger(this.quantityInput, 0, maxSellAmount);
			int currentAmount = TextInputUtil.getIntegerValue(this.quantityInput);
			if(currentAmount != this.getTrade().getAmount())
				this.commonTab.setQuantity(currentAmount);
		}
		
		this.buttonToggleTradeType.setMessage(new TranslatableComponent("gui.button.lightmanscurrency.tradedirection." + this.commonTab.getTrade().getTradeDirection().name().toLowerCase()));
		
	}
	
	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
	}
	
	@Override
	public void receiveSelfMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}
	
	@Override
	public void onTradeButtonInputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof EnergyTradeData)
		{
			EnergyTradeData t = (EnergyTradeData)trade;
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(0);
		}
	}
	
	@Override
	public void onTradeButtonOutputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof EnergyTradeData)
		{
			EnergyTradeData t = (EnergyTradeData)trade;
			if(t.isSale())
				this.changeSelection(0);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}
	
	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.setCoinValue(this.commonTab.getTrade().getCost());
		if(this.selection >= 0)
			this.quantityInput.setValue(String.valueOf(this.commonTab.getTrade().getAmount()));
	}
	
	@Override
	public void onTradeButtonInteraction(ITrader trader, ITradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}
	
	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		if(button instanceof AbstractWidget)
			this.screen.addRenderableTabWidget((AbstractWidget)button);
		return button;
	}
	
	@Override
	public int getWidth() { return this.screen.width; }

	@Override
	public Font getFont() { return this.font; }
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) { this.commonTab.setPrice(input.getCoinValue()); }
	
	public EnergyTradeData getTrade() { return this.commonTab.getTrade(); }
	
	private void ToggleTradeType(Button button) {
		if(this.getTrade() != null)
			this.commonTab.setType(this.getTrade().getTradeDirection().next());
	}
	
}
