package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class EnergyTradeEditClientTab extends TraderStorageClientTab<EnergyTradeEditTab> implements InteractionConsumer, IMouseListener {

	public EnergyTradeEditClientTab(Object screen, EnergyTradeEditTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabButtonVisible() { return false; }

	@Override
	public boolean blockInventoryClosing() { return true; }

	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	MoneyValueWidget priceSelection;

	EditBox quantityInput;

	EasyButton buttonToggleTradeType;

	private int selection;

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		EnergyTradeData trade = this.commonTab.getTrade();

		this.tradeDisplay = this.addChild(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.setPosition(screenArea.pos.offset(10, 18));
		this.priceSelection = this.addChild(new MoneyValueWidget(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 40), this.priceSelection, trade == null ? MoneyValue.empty() : trade.getCost(), this::onValueChanged));
		this.priceSelection.drawBG = false;

		this.quantityInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 75, this.screen.getXSize() - 42 - this.getFont().width(EnergyUtil.ENERGY_UNIT), 20, EasyText.empty()));
		this.quantityInput.setValue(trade != null ? String.valueOf(trade.getAmount()): "");

		this.buttonToggleTradeType = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 120), 72, 20, EasyText.empty(), this::ToggleTradeType));

	}

	@Override
	protected void closeAction() { this.selection = -1; }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		if(this.getTrade() == null)
			return;

		this.validateRenderables();

		//Render an arrow to the left of the selected position
		gui.resetColor();
		gui.blit(TraderScreen.GUI_TEXTURE, this.getArrowPosition(), 10, TraderScreen.WIDTH + 8, 18, 8, 6);

		if(this.selection >= 0)
			gui.drawShadowed(EnergyUtil.ENERGY_UNIT, this.screen.getXSize() - 20 - gui.font.width(EnergyUtil.ENERGY_UNIT), 78, 0xFFFFFF);

	}

	private int getArrowPosition() {
		EnergyTradeData trade = this.getTrade();
		if(this.selection < 0)
		{
			if(trade.isSale())
				return 25;
			else
				return 116;
		}
		else
		{
			if(trade.isSale())
				return 99;
			else
				return 41;
		}
	}

	private void validateRenderables() {

		this.priceSelection.visible = this.selection < 0;
		this.quantityInput.visible = this.selection >= 0;
		if(this.quantityInput.visible)
		{
			int maxSellAmount = Integer.MAX_VALUE;
			if(this.menu.getTrader() instanceof EnergyTraderData)
				maxSellAmount = ((EnergyTraderData)this.menu.getTrader()).getMaxEnergy();
			TextInputUtil.whitelistInteger(this.quantityInput, 0, maxSellAmount);
			int currentAmount = TextInputUtil.getIntegerValue(this.quantityInput);
			if(currentAmount != this.getTrade().getAmount())
				this.commonTab.setQuantity(currentAmount);
		}

		this.buttonToggleTradeType.setMessage(this.commonTab.getTrade().getTradeDirection().getName());

	}

	@Override
	public void receiveSelfMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(0);
		}
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(0);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}

	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.changeValue(this.commonTab.getTrade().getCost());
		if(this.selection >= 0)
			this.quantityInput.setValue(String.valueOf(this.commonTab.getTrade().getAmount()));
	}

	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }

	public void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

	public EnergyTradeData getTrade() { return this.commonTab.getTrade(); }

	private void ToggleTradeType(EasyButton button) {
		EnergyTradeData trade = this.getTrade();
		if(trade != null)
			this.commonTab.setType(trade.isSale() ? TradeDirection.PURCHASE : TradeDirection.SALE);
	}

}