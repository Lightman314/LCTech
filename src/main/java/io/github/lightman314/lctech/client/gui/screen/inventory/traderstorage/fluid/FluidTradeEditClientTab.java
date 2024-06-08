package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget.IFluidEditListener;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidTradeEditTab;
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
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;

public class FluidTradeEditClientTab extends TraderStorageClientTab<FluidTradeEditTab> implements InteractionConsumer, IFluidEditListener, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 71;
	private static final int COLUMNS = 10;
	private static final int ROWS = 2;

	public FluidTradeEditClientTab(Object screen, FluidTradeEditTab commonTab) { super(screen, commonTab); }

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

	EasyButton buttonAddBucket;
	EasyButton buttonRemoveBucket;

	FluidEditWidget fluidEdit;
	ScrollBarWidget fluidEditScroll;

	EasyButton buttonToggleTradeType;

	private int selection;

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		FluidTradeData trade = this.getTrade();

		this.tradeDisplay = this.addChild(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.setPosition(screenArea.pos.offset(10, 18));
		this.priceSelection = this.addChild(new MoneyValueWidget(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 40), this.priceSelection, trade == null ? MoneyValue.empty() : trade.getCost(), this::onValueChanged));
		this.priceSelection.drawBG = false;

		this.fluidEdit = this.addChild(new FluidEditWidget(screenArea.pos.offset(X_OFFSET, Y_OFFSET), COLUMNS, ROWS, this));

		this.fluidEditScroll = this.addChild(new ScrollBarWidget(screenArea.pos.offset(X_OFFSET + 18 * COLUMNS, Y_OFFSET), 18 * ROWS, this.fluidEdit));
		this.fluidEditScroll.smallKnob = true;

		this.buttonAddBucket = this.addChild(new IconButton(screenArea.pos.offset(74, 38), this::ChangeQuantity, IconData.of(FluidStorageClientTab.GUI_TEXTURE, 32, 0)));
		this.buttonRemoveBucket = this.addChild(new IconButton(screenArea.pos.offset(113, 38), this::ChangeQuantity, IconData.of(FluidStorageClientTab.GUI_TEXTURE, 48, 0)));

		this.buttonToggleTradeType = this.addChild(new EasyTextButton(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 15, 80, 20, EasyText.empty(), this::ToggleTradeType));

	}

	@Override
	public void closeAction() { this.selection = -1; }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		if(this.getTrade() == null)
			return;

		this.validateRenderables();

		//Render the local quantity text
		if(this.selection >= 0)
		{
			String quantityText = this.getTrade().getBucketQuantity() + "B";
			int textWidth = gui.font.width(quantityText);
			gui.drawString(quantityText, 1 + (this.screen.getXSize() / 2) - (textWidth / 2), 42, 0xFFFFFF);
		}

		//Render a down arrow over the selected position
		gui.resetColor();
		gui.blit(TraderScreen.GUI_TEXTURE, this.getArrowPosition(), 10, TraderScreen.WIDTH + 8, 18, 8, 6);

	}

	private int getArrowPosition() {

		FluidTradeData trade = this.getTrade();
		if(this.selection == -1)
		{
			if(trade.isSale())
				return 25;
			else
				return 63;
		}
		else
		{
			if(trade.isSale())
				return 72;
			else
				return 16;
		}
	}

	private void validateRenderables() {

		this.priceSelection.visible = this.selection < 0;
		this.fluidEdit.visible = this.selection >= 0;

		this.buttonAddBucket.visible = this.buttonRemoveBucket.visible = this.selection >= 0;
		if(this.buttonAddBucket.visible)
			this.buttonAddBucket.active = this.getTrade().getBucketQuantity() < this.getTrade().getMaxBucketQuantity();
		if(this.buttonRemoveBucket.visible)
			this.buttonRemoveBucket.active = this.getTrade().getBucketQuantity() > 1;

		this.buttonToggleTradeType.setMessage(this.getTrade().getTradeDirection().getName());

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
		if(trade instanceof FluidTradeData t)
		{
			ItemStack heldItem = this.menu.getHeldItem();
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
			{
				if(this.selection != 0 && heldItem.isEmpty())
					this.changeSelection(0);
				else
					this.commonTab.setFluid(FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY));
			}
		}
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trade instanceof FluidTradeData t)
		{
			ItemStack heldItem = this.menu.getHeldItem();
			if(t.isSale())
			{
				if(this.selection != 0 && heldItem.isEmpty())
					this.changeSelection(0);
				else
					this.commonTab.setFluid(FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY));
			}
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}

	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.changeValue(this.getTrade().getCost());
		if(this.selection == 0 && !this.getTrade().isSale())
			this.fluidEdit.refreshSearch();
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

	public FluidTradeData getTrade() { return this.commonTab.getTrade(); }

	public void onFluidClicked(FluidStack fluid) {
		this.commonTab.setFluid(fluid);
	}

	private void ChangeQuantity(EasyButton button) {
		if(this.getTrade() != null)
		{
			int deltaQuantity = 1;
			if(button == this.buttonRemoveBucket)
				deltaQuantity = -1;
			this.commonTab.setQuantity(this.getTrade().getBucketQuantity() + deltaQuantity);
		}
	}

	private void ToggleTradeType(EasyButton button) {
		FluidTradeData trade = this.getTrade();
		if(trade != null)
			this.commonTab.setType(trade.isSale() ? TradeDirection.PURCHASE : TradeDirection.SALE);
	}

}