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
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
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

public class EnergyTradeEditClientTab extends TraderStorageClientTab<EnergyTradeEditTab> implements TradeInteractionHandler, IMouseListener {

	public EnergyTradeEditClientTab(Object screen, EnergyTradeEditTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabVisible() { return false; }

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

		this.tradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset(10,18))
				.context(this.menu::getContext)
				.trade(this.commonTab::getTrade)
				.build());
		this.priceSelection = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 40))
				.oldIfNotFirst(firstOpen,this.priceSelection)
				.startingValue(trade)
				.valueHandler(this::onValueChanged)
				.build());

		this.quantityInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 75, this.screen.getXSize() - 42 - this.getFont().width(EnergyUtil.ENERGY_UNIT), 20, EasyText.empty()));
		this.quantityInput.setValue(trade != null ? String.valueOf(trade.getAmount()): "");

		this.buttonToggleTradeType = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(20,120))
				.size(72,20)
				.text(() -> this.commonTab.getTrade().getTradeDirection().getName())
				.pressAction(this::ToggleTradeType)
				.build());

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

	}

	@Override
	protected void OpenMessage(@Nonnull LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData traderData, @Nonnull TradeData trade, @Nonnull TradeInteractionData tradeInteractionData, int i) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(0);
		}
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData traderData, @Nonnull TradeData trade, @Nonnull TradeInteractionData tradeInteractionData, int i) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(0);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData traderData, @Nonnull TradeData tradeData, @Nonnull TradeInteractionData tradeInteractionData) {

	}

	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.changeValue(this.commonTab.getTrade().getCost());
		if(this.selection >= 0)
			this.quantityInput.setValue(String.valueOf(this.commonTab.getTrade().getAmount()));
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.HandleInteractionClick((int)mouseX, (int)mouseY, button, this);
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