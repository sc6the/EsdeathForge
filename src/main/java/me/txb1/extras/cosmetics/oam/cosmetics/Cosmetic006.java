package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=6, height=0.3, activatergb=false, displayname="Sheriff Hat", type=CosmeticType.DEFAULT)
public class Cosmetic006
extends CosmeticModelRenderer {
    CosmeticModelRenderer sheriffHat;

    public Cosmetic006(ModelBase base) {
        super(base);
        this.sheriffHat = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        this.sheriffHat.setRotationPoint(-5.0f, -10.03125f, -5.0f);
        this.sheriffHat.setTextureOffset(0, 64).addBox(0.0f, 0.0f, 0.0f, 10, 2, 10);
        CosmeticModelRenderer modelrenderer = new CosmeticModelRenderer(base).setTextureSize(64, 128);
        modelrenderer.setRotationPoint(1.75f, -4.0f, 2.0f);
        modelrenderer.setTextureOffset(0, 76).addBox(0.0f, 0.0f, 0.0f, 7, 4, 7);
        modelrenderer.rotateAngleX = -0.05235988f;
        modelrenderer.rotateAngleZ = 0.02617994f;
        this.sheriffHat.addChild(modelrenderer);
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
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.bindCosmeticTexture("sheriff.png");
        this.setHeadRotations();
        this.sheriffHat.render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

