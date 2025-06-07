package io.github.lightman314.lctech.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class FluidEditWidget extends EasyWidgetWithChildren implements IScrollable, ITooltipSource {

	private static final List<Fluid> BLACKLISTED_FLUIDS = Lists.newArrayList(Fluids.EMPTY);
	public static void BlacklistFluid(Fluid fluid) { if(!BLACKLISTED_FLUIDS.contains(fluid)) BLACKLISTED_FLUIDS.add(fluid); }

	private int scroll = 0;

	private final int columns;
	private final int rows;

	private final ScreenPosition searchOffset;

	private static List<Fluid> allFluids = null;

	List<Fluid> searchResultFluids;

	private String searchString;

	EditBox searchInput;

	private final Consumer<FluidStack> handler;

	private final Font font;


	private FluidEditWidget(@Nonnull Builder builder) {
		super(builder);
		this.handler = builder.handler;

		this.columns = builder.columns;
		this.rows = builder.rows;

		this.searchOffset = Objects.requireNonNullElse(builder.searchOffset,ScreenPosition.of(this.width - 90, -13));

		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;

		//Set the search to the default value to initialize the list
		this.modifySearch("");

	}

	public static void initFluidList() {
		if(allFluids != null)
			return;

		allFluids = new ArrayList<>();

		BuiltInRegistries.FLUID.forEach(fluid ->{
			if(!BLACKLISTED_FLUIDS.contains(fluid) && fluid.isSource(fluid.defaultFluidState()))
				allFluids.add(fluid);
		});

	}

	public int getMaxScroll() { return Math.max(((this.searchResultFluids.size() - 1) / this.columns) - this.rows + 1, 0); }

	public void refreshPage() {
		if(this.scroll < 0)
			this.scroll = 0;
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
	}

	public void refreshSearch() { this.modifySearch(this.searchString); }

	public void modifySearch(String newSearch) {
		this.searchString = newSearch.toLowerCase();

		//Repopulate the searchResultFluids list
		if(!this.searchString.isEmpty())
		{
			//Search the display name
			this.searchResultFluids = Lists.newArrayList();
			for(Fluid fluid : allFluids)
			{
				//Search the fluid name
				if(fluid.getFluidType().getDescription(new FluidStack(fluid, FluidType.BUCKET_VOLUME)).getString().toLowerCase().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
				//Search the registry name
				else if(BuiltInRegistries.FLUID.getKey(fluid).toString().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
			}
		}
		else //No search string, so the result is just the allFluids list
			this.searchResultFluids = allFluids;
	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		this.searchInput = this.addChild(new EditBox(this.font, area.x + this.searchOffset.x + 2, area.y + this.searchOffset.y + 2, 79, 9, LCText.GUI_ITEM_EDIT_SEARCH.get()));
		this.searchInput.setBordered(false);
		this.searchInput.setMaxLength(32);
		this.searchInput.setTextColor(0xFFFFFF);
		this.searchInput.setResponder(this::modifySearch);
		this.addChild(ScrollListener.builder()
				.area(area)
				.listener(this)
				.build());
		this.addChild(ScrollBarWidget.builder()
				.onRight(this)
				.smallKnob()
				.addon(EasyAddonHelper.visibleCheck(this::isVisible))
				.build());
	}

	@Override
	protected void renderTick() { this.searchInput.visible = this.visible; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {

		int index = this.scroll * this.columns;
		for(int y = 0; y < this.rows && index < this.searchResultFluids.size(); ++y)
		{
			int yPos = y * 18;
			for(int x = 0; x < this.columns && index < this.searchResultFluids.size(); ++x)
			{
				//Get the slot position
				int xPos = x * 18;
				//Render the slot background
				gui.resetColor();
				gui.blit(ItemEditWidget.GUI_TEXTURE, xPos, yPos, 0, 0, 18, 18);
				//Render the slots item
				gui.renderItem(FluidItemUtil.getFluidDispayItem(this.searchResultFluids.get(index)), xPos + 1, yPos + 1);
				index++;
			}
		}

		//Render the search field
		gui.resetColor();
		gui.blit(ItemEditWidget.GUI_TEXTURE, this.searchOffset, 18, 0, 90, 12);

	}

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) {
		if(!this.visible)
			return null;
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultFluids.size())
				return Lists.newArrayList(FluidFormatUtil.getFluidName(new FluidStack(this.searchResultFluids.get(hoveredSlot), 1000)));
		}
		return null;
	}

	private int isMouseOverSlot(double mouseX, double mouseY) {

		int foundColumn = -1;
		int foundRow = -1;

		for(int x = 0; x < this.columns && foundColumn < 0; ++x)
		{
			if(mouseX >= this.getX() + x * 18 && mouseX < this.getX() + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < this.rows && foundRow < 0; ++y)
		{
			if(mouseY >= this.getY() + y * 18 && mouseY < this.getY() + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * this.columns) + foundColumn;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		//Do nothing if not active/visible
		if(!this.isActive())
			return false;
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultFluids.size())
			{
				FluidStack fluid = new FluidStack(this.searchResultFluids.get(hoveredSlot), FluidType.BUCKET_VOLUME);
				this.handler.accept(fluid);
				return true;
			}
		}
		return false;
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.refreshPage();
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@ParametersAreNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(18,18); }

		@Override
		protected Builder getSelf() { return this; }

		int columns = 1;
		int rows = 1;
		@Nullable
		ScreenPosition searchOffset = null;
		private Consumer<FluidStack> handler = s -> {};

		public Builder columns(int columns) { this.columns = columns; this.changeWidth(this.columns * 18); return this; }
		public Builder rows(int rows) { this.rows = rows; this.changeHeight(this.rows * 18); return this; }
		public Builder searchOffset(int searchOffX, int searchOffY) { return this.searchOffset(ScreenPosition.of(searchOffX,searchOffY)); }
		public Builder searchOffset(ScreenPosition searchOffset) { this.searchOffset = searchOffset; return this; }
		public Builder handler(Consumer<FluidStack> handler) { this.handler = handler; return this; }

		public FluidEditWidget build() { return new FluidEditWidget(this); }

	}

}