package io.github.lightman314.lctech.mixin.client;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EasyGuiGraphics.class)
public interface EasyGuiGraphicsAccessor {

    @Accessor(value = "offset",remap = false)
    ScreenPosition getOffset();

}
