package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=7, height=0.5, activatergb=true, displayname="Witch Hat", type=CosmeticType.DEFAULT)
public class Cosmetic007
extends CosmeticModelRenderer {
    CosmeticModelRenderer witchHat;

    public Cosmetic007(ModelBase base) {
        super(base);
        this.witchHat = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        this.witchHat.setRotationPoint(-5.0f, -10.03125f, -5.0f);
        this.witchHat.setTextureOffset(0, 64).addBox(0.0f, 0.0f, 0.0f, 10, 2, 10);
        CosmeticModelRenderer modelrenderer = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        modelrenderer.setRotationPoint(1.75f, -4.0f, 2.0f);
        modelrenderer.setTextureOffset(0, 76).addBox(0.0f, 0.0f, 0.0f, 7, 4, 7);
        modelrenderer.rotateAngleX = -0.05235988f;
        modelrenderer.rotateAngleZ = 0.02617994f;
        this.witchHat.addChild(modelrenderer);
        CosmeticModelRenderer modelrenderer1 = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        modelrenderer1.setRotationPoint(1.75f, -4.0f, 2.0f);
        modelrenderer1.setTextureOffset(0, 87).addBox(0.0f, 0.0f, 0.0f, 4, 4, 4);
        modelrenderer1.rotateAngleX = -0.10471976f;
        modelrenderer1.rotateAngleZ = 0.05235988f;
        modelrenderer.addChild(modelrenderer1);
        CosmeticModelRenderer modelrenderer2 = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        modelrenderer2.setRotationPoint(1.75f, -2.0f, 2.0f);
        modelrenderer2.setTextureOffset(0, 95).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1, 0.25f);
        modelrenderer2.rotateAngleX = -0.20943952f;
        modelrenderer2.rotateAngleZ = 0.10471976f;
        modelrenderer1.addChild(modelrenderer2);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        this.renderRGB("color", 100, 60, 0);
        this.bindCosmeticTexture("witchhat.png");
        this.setHeadRotations();
        this.witchHat.render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

