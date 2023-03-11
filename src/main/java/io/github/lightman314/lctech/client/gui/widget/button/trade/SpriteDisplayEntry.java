package io.github.lightman314.lctech.client.gui.widget.button.trade;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SpriteDisplayEntry extends DisplayEntry{

	private final ResourceLocation sprite;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	
	public static DisplayEntry of(ResourceLocation sprite, int u, int v, int width, int height, List<ITextComponent> tooltips) { return new SpriteDisplayEntry(sprite, u, v, width, height, tooltips); }
	
	private SpriteDisplayEntry(ResourceLocation sprite, int u, int v, int width, int height, List<ITextComponent> tooltips) {
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
		int left = this.getLeft(x + area.xOffset, area.width);
		int top = this.getTop(y + area.yOffset, area.height);
		return mouseX >= left && mouseX < left + this.width && mouseY >= top && mouseY < top + this.height;
	}

	@Override
	public void render(AbstractGui gui, MatrixStack pose, int x, int y, DisplayData area) {
		if(this.sprite == null)
			return;
		int left = this.getLeft(x + area.xOffset, area.width);
		int top = this.getTop(y + area.yOffset, area.height);
		RenderUtil.bindTexture(this.sprite);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
		gui.blit(pose, left, top, u, v, this.width, this.height);
	}

}
