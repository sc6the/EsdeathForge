package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=36, height=0.1, activatergb=true, displayname="Cap", type=CosmeticType.DEFAULT)
public class Cosmetic036
extends CosmeticModelRenderer {
    CosmeticModelRenderer top;
    CosmeticModelRenderer bottom;

    public Cosmetic036(ModelBase base) {
        super(base);
        this.top = new CosmeticModelRenderer(base).setTextureSize(64, 32);
        this.top.addBox(-4.0f, -10.0f, -4.0f, 8, 1, 8);
        this.top.addBox(-3.0f, -10.0f, -5.0f, 6, 1, 1);
        this.top.addBox(-3.0f, -10.0f, 4.0f, 6, 1, 1);
        this.top.addBox(-5.0f, -8.0f, -5.0f, 10, 1, 10);
        this.top.addBox(-5.0f, -9.0f, -4.0f, 10, 1, 8);
        this.top.addBox(-4.0f, -9.0f, 4.0f, 8, 1, 1);
        this.top.addBox(-4.0f, -9.0f, -5.0f, 8, 1, 1);
        this.bottom = new CosmeticModelRenderer(base).setTextureSize(64, 32);
        this.bottom.addBox(-5.0f, -7.0f, -5.0f, 10, 1, 13);
        this.bottom.addBox(-4.0f, -7.0f, 8.0f, 8, 1, 1);
    }

    @Override
    public void render(float scale) {
        GL11.glDisable((int)2896);
        this.setHeadRotations();
        GL11.glScalef((float)1.01f, (float)1.01f, (float)-1.01f);
        GL11.glTranslatef((float)0.0f, (float)-0.057f, (float)0.0f);
        this.bindCosmeticTexture("resources.png");
        this.renderRGB("color", 10, 10, 10);
        this.top.render(scale);
        this.renderRGB("colorbottom", 100, 100, 100);
        this.bottom.render(scale);
        GL11.glEnable((int)2896);
        super.render(scale);
    }
}

