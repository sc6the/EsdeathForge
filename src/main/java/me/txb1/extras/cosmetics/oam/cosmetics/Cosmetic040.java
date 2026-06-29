package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;

@CosmeticInfo(id=40, height=0.0, activatergb=false, displayname="Rinne Sharingan", type=CosmeticType.DEFAULT)
public class Cosmetic040
extends CosmeticModelRenderer {
    CosmeticModelRenderer model;

    public Cosmetic040(ModelBase base) {
        super(base);
        this.model = new CosmeticModelRenderer(base, 0, 0).setTextureSize(32, 32);
        this.model.addBox(-1.0f, -7.0f, -4.05f, 2, 2, 1);
    }

    @Override
    public void render(float scale) {
        this.setHeadRotations();
        this.bindCosmeticTexture("rinne_sharingan.png");
        this.model.render(scale);
        super.render(scale);
    }
}

