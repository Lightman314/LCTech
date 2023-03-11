package io.github.lightman314.lctech.client.gui.widget;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class FluidEditWidget extends Widget implements IScrollable{

	private static final List<Fluid> BLACKLISTED_FLUIDS = Lists.newArrayList(Fluids.EMPTY);
	public static void BlacklistFluid(Fluid fluid) { if(!BLACKLISTED_FLUIDS.contains(fluid)) BLACKLISTED_FLUIDS.add(fluid); }
	
	private int scroll = 0;
	
	private final int columns;
	private final int rows;
	
	private final int searchOffX;
	private final int searchOffY;
	
	private static List<Fluid> allFluids = null;
	
	List<Fluid> searchResultFluids;
	
	private String searchString;
	
	TextFieldWidget searchInput;
	
	private final IFluidEditListener listener;
	
	private final FontRenderer font;
	
	public FluidEditWidget(int x, int y, int columns, int rows, IFluidEditListener listener) {
		super(x, y, columns * 18, rows * 18, EasyText.empty());
		this.listener = listener;
		
		this.columns = columns;
		this.rows = rows;
		
		this.searchOffX = this.width - 90;
		this.searchOffY = -13;
		
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;
		
		//Set the search to the default value to initialize the list
		this.modifySearch("");
		
	}
	
	public static void initFluidList() {
		if(allFluids != null)
			return;
		
		allFluids = Lists.newArrayList();
		
		ForgeRegistries.FLUIDS.forEach(fluid ->{
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
		
		//Repopulate the searchResultItems list
		if(this.searchString.length() > 0)
		{
			//Search the display name
			this.searchResultFluids = Lists.newArrayList();
			for(Fluid fluid : allFluids)
			{
				//Search the fluid name
				if(fluid.getAttributes().getDisplayName(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME)).getString().toLowerCase().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
				//Search the registry name
				else if(fluid.getRegistryName().toString().contains(this.searchString)) {
					this.searchResultFluids.add(fluid);
				}
			}
		}
		else //No search string, so the result is just the allFluids list
			this.searchResultFluids = allFluids;
	}
	
	public void init(Function<TextFieldWidget,TextFieldWidget> addWidget, Function<ScrollListener,ScrollListener> addListener) {
		this.searchInput = addWidget.apply(new TextFieldWidget(this.font, this.x + this.searchOffX + 2, this.y + this.searchOffY + 2, 79, 9, EasyText.translatable("gui.lightmanscurrency.item_edit.search")));
		this.searchInput.setBordered(false);
		this.searchInput.setMaxLength(32);
		this.searchInput.setTextColor(0xFFFFFF);
		
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.searchInput.visible = this.visible;
		
		if(!this.visible)
			return;
		
		this.searchInput.tick();
		if(!this.searchInput.getValue().toLowerCase().contentEquals(this.searchString))
			this.modifySearch(this.searchInput.getValue());
		
		int index = this.scroll * this.columns;
		for(int y = 0; y < this.rows && index < this.searchResultFluids.size(); ++y)
		{
			int yPos = this.y + y * 18;
			for(int x = 0; x < this.columns && index < this.searchResultFluids.size(); ++x)
			{
				//Get the slot position
				int xPos = this.x + x * 18;
				//Render the slot background
				RenderUtil.bindTexture(ItemEditWidget.GUI_TEXTURE);
				RenderUtil.color4f(1f, 1f, 1f, 1f);
				this.blit(pose, xPos, yPos, 0, 0, 18, 18);
				//Render the slots item
				ItemRenderUtil.drawItemStack(this, this.font, FluidItemUtil.getFluidDispayItem(this.searchResultFluids.get(index)), xPos + 1, yPos + 1);
				index++;
			}
		}
		
		//Render the search field
		RenderUtil.bindTexture(ItemEditWidget.GUI_TEXTURE);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
		this.blit(pose, this.x + this.searchOffX, this.y + this.searchOffY, 18, 0, 90, 12);
		
	}
	
	public void renderTooltips(Screen screen, MatrixStack pose, int mouseX, int mouseY) {
		if(!this.visible)
			return;
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultFluids.size())
			{
				screen.renderTooltip(pose, FluidFormatUtil.getFluidName(new FluidStack(this.searchResultFluids.get(hoveredSlot), 1000)), mouseX, mouseY);
			}
		}
	}
	
	private int isMouseOverSlot(double mouseX, double mouseY) {
		
		int foundColumn = -1;
		int foundRow = -1;
		
		for(int x = 0; x < this.columns && foundColumn < 0; ++x)
		{
			if(mouseX >= this.x + x * 18 && mouseX < this.x + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < this.rows && foundRow < 0; ++y)
		{
			if(mouseY >= this.y + y * 18 && mouseY < this.y + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * this.columns) + foundColumn;
	}
	
	public interface IFluidEditListener {
		void onFluidClicked(FluidStack fluid);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultFluids.size())
			{
				FluidStack fluid = new FluidStack(this.searchResultFluids.get(hoveredSlot), FluidAttributes.BUCKET_VOLUME);
				this.listener.onFluidClicked(fluid);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.scroll < this.getMaxScroll())
				this.scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				this.scroll--;
			else
				return false;
		}
		return true;
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.refreshPage();
	}
	
}
