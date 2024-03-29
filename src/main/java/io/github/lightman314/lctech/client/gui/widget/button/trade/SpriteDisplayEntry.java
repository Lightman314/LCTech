package io.github.lightman314.lctech.client.gui.widget.button.trade;

import java.util.List;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpriteDisplayEntry extends DisplayEntry {

	private final ResourceLocation sprite;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	
	public static DisplayEntry of(ResourceLocation sprite, int u, int v, int width, int height, List<Component> tooltips) { return new SpriteDisplayEntry(sprite, u, v, width, height, tooltips); }
	
	private SpriteDisplayEntry(ResourceLocation sprite, int u, int v, int width, int height, List<Component> tooltips) {
		super(tooltips);
		this.sprite = sprite;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
	}
	
	//Force left to be on the exact edge
	private int getLeft(int x, int availableWidth) { return x; }
	private int getTop(int y, int availableHeight) { return y + (availableHeight / 2) - (this.height / 2); }
	
	@Override
	public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
		int left = this.getLeft(x + area.xOffset(), area.width());
		int top = this.getTop(y + area.yOffset(), area.height());
		return mouseX >= left && mouseX < left + this.width && mouseY >= top && mouseY < top + this.height;
	}

	@Override
	public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
		if(this.sprite == null)
			return;
		int left = this.getLeft(x + area.xOffset(), area.width());
		int top = this.getTop(y + area.yOffset(), area.height());
		gui.resetColor();
		gui.blit(this.sprite, left, top, u, v, this.width, this.height);
	}

}
