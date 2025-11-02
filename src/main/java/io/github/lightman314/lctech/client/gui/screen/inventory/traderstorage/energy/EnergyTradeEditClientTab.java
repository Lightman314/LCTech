package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
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
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyTradeEditClientTab extends TraderStorageClientTab<EnergyTradeEditTab> implements TradeInteractionHandler, IMouseListener {

	public EnergyTradeEditClientTab(Object screen, EnergyTradeEditTab commonTab) { super(screen, commonTab); }

	@Override
	public IconData getIcon() { return IconUtil.ICON_TRADER; }

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

		this.quantityInput = this.addChild(TextInputUtil.intBuilder()
				.position(screenArea.pos.offset(20,75))
				.width(screenArea.width - 42 - this.getFont().width(EnergyUtil.ENERGY_UNIT))
				.handler(this.commonTab::setQuantity)
				.apply(IntParser.builder().min(0).consumer())
				.startingValue(trade != null ? trade.getAmount() : 0)
				.build());

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
	public void renderBG(EasyGuiGraphics gui) {

		if(this.getTrade() == null)
			return;

		this.validateRenderables();

		//Render an arrow to the left of the selected position
		gui.resetColor();
        SpriteUtil.SMALL_ARROW_DOWN.render(gui,this.getArrowPosition(),10);
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

	}

	@Override
	protected void OpenMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}

	@Override
	public void HandleTradeInputInteraction(TraderData traderData, TradeData trade, TradeInteractionData tradeInteractionData, int i) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(0);
		}
	}

	@Override
	public void HandleTradeOutputInteraction(TraderData traderData, TradeData trade, TradeInteractionData tradeInteractionData, int i) {
		if(trade instanceof EnergyTradeData t)
		{
			if(t.isSale())
				this.changeSelection(0);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}

	@Override
	public void HandleOtherTradeInteraction(TraderData traderData, TradeData tradeData, TradeInteractionData tradeInteractionData) {

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