package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class EnergyTradeEditClientTab extends TraderStorageClientTab<EnergyTradeEditTab> implements InteractionConsumer {

	public EnergyTradeEditClientTab(TraderStorageScreen screen, EnergyTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }
	
	@Override
	public MutableComponent getTooltip() { return Component.empty(); }
	
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
		this.tradeDisplay.move(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
		this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + this.screen.getXSize() / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 40, Component.empty(), trade == null ? CoinValue.EMPTY : trade.getCost(), this.font, this::onValueChanged, this.screen::addRenderableTabWidget));
		this.priceSelection.drawBG = false;
		this.priceSelection.init();
		
		this.quantityInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 75, this.screen.getXSize() - 42 - this.font.width(EnergyUtil.ENERGY_UNIT), 20, Component.empty()));
		this.quantityInput.setValue(trade != null ? String.valueOf(trade.getAmount()): "");
		
		this.buttonToggleTradeType = this.screen.addRenderableTabWidget(Button.builder(Component.empty(), this::ToggleTradeType).pos(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 120).size(72, 20).build());
		
	}
	
	@Override
	public void onClose() { this.selection = -1; }
	
	@Override
	public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		//Render an arrow to the left of the selected position
		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		GuiComponent.blit(pose, this.getArrowPosition(), this.screen.getGuiTop() + 10, TraderScreen.WIDTH + 8, 18, 8, 6);
		
		if(this.selection >= 0)
			this.font.drawShadow(pose, EnergyUtil.ENERGY_UNIT, this.screen.getGuiLeft() + this.screen.getXSize() - 20 - this.font.width(EnergyUtil.ENERGY_UNIT), this.screen.getGuiTop() + 78, 0xFFFFFF);
		
	}
	
	private int getArrowPosition() {
		EnergyTradeData trade = this.getTrade();
		if(this.selection < 0)
		{
			if(trade.isSale())
				return this.screen.getGuiLeft() + 25;
			else
				return this.screen.getGuiLeft() + 116;
		}
		else
		{
			if(trade.isSale())
				return this.screen.getGuiLeft() + 99;
			else
				return this.screen.getGuiLeft() + 41;
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
			if(this.menu.getTrader() instanceof EnergyTraderData)
				maxSellAmount = ((EnergyTraderData)this.menu.getTrader()).getMaxEnergy();
			TextInputUtil.whitelistInteger(this.quantityInput, 0, maxSellAmount);
			int currentAmount = TextInputUtil.getIntegerValue(this.quantityInput);
			if(currentAmount != this.getTrade().getAmount())
				this.commonTab.setQuantity(currentAmount);
		}
		
		this.buttonToggleTradeType.setMessage(Component.translatable("gui.button.lightmanscurrency.tradedirection." + this.commonTab.getTrade().getTradeDirection().name().toLowerCase()));
		
	}
	
	@Override
	public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {
		
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
			this.priceSelection.setCoinValue(this.commonTab.getTrade().getCost());
		if(this.selection >= 0)
			this.quantityInput.setValue(String.valueOf(this.commonTab.getTrade().getAmount()));
	}
	
	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}
	
	public void onValueChanged(CoinValue value) { this.commonTab.setPrice(value); }
	
	public EnergyTradeData getTrade() { return this.commonTab.getTrade(); }
	
	private void ToggleTradeType(Button button) {
		if(this.getTrade() != null)
			this.commonTab.setType(this.getTrade().getTradeDirection().next());
	}
	
}
