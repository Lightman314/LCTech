package io.github.lightman314.lctech.client.gui.widget.button.trade;

import java.util.List;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;

public class SpriteDisplayEntry extends DisplayEntry {

    private final NormalSprite sprite;

    public static DisplayEntry of(NormalSprite sprite, List<Component> tooltips) { return new SpriteDisplayEntry(sprite, tooltips); }

    private SpriteDisplayEntry(NormalSprite sprite, List<Component> tooltips) {
        super(tooltips);
        this.sprite = sprite;
    }

    //Force left to be on the exact edge
    private int getLeft(int x, int availableWidth) { return x; }
    private int getTop(int y, int availableHeight) { return y + (availableHeight / 2) - (this.sprite.getHeight() / 2); }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = this.getLeft(x + area.xOffset(), area.width());
        int top = this.getTop(y + area.yOffset(), area.height());
        return mouseX >= left && mouseX < left + this.sprite.getWidth() && mouseY >= top && mouseY < top + this.sprite.getHeight();
    }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        if(this.sprite == null)
            return;
        int left = this.getLeft(x + area.xOffset(), area.width());
        int top = this.getTop(y + area.yOffset(), area.height());
        gui.resetColor();
        this.sprite.render(gui,left,top);
    }

}