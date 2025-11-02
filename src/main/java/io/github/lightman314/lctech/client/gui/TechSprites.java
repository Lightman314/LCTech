package io.github.lightman314.lctech.client.gui;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleHeightSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.VerticalSliceSprite;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;

public class TechSprites {

    public static final NormalSprite DRAINABLE_ACTIVE = new NormalSprite(SpriteSource.createTop(VersionUtil.modResource(LCTech.MODID,"common/drainable"),8,8));
    public static final FixedSizeSprite DRAINABLE_INACTIVE = new NormalSprite(SpriteSource.createTop(VersionUtil.modResource(LCTech.MODID,"common/drainable"),8,8));

    public static final FixedSizeSprite FILLABLE_ACTIVE = new NormalSprite(SpriteSource.createTop(VersionUtil.modResource(LCTech.MODID,"common/fillable"),8,8));
    public static final FixedSizeSprite FILLABLE_INACTIVE = new NormalSprite(SpriteSource.createBottom(VersionUtil.modResource(LCTech.MODID,"common/fillable"),8,8));

    public static final FlexibleHeightSprite TANK_BACKGROUND = new VerticalSliceSprite(SpriteSource.create(VersionUtil.modResource(LCTech.MODID,"common/tank_background"),18,66),8);
    public static final FlexibleHeightSprite TANK_FOREGROUND = new VerticalSliceSprite(SpriteSource.create(VersionUtil.modResource(LCTech.MODID,"common/tank_foreground"),18,66),8);

    public static final FlexibleHeightSprite BATTERY_BACKGROUND = new VerticalSliceSprite(SpriteSource.create(VersionUtil.modResource(LCTech.MODID,"common/battery_background"),18,90),8);
    public static final FlexibleHeightSprite BATTERY_FILLER = new VerticalSliceSprite(SpriteSource.create(VersionUtil.modResource(LCTech.MODID,"common/battery_filler"),16,88),0);

}