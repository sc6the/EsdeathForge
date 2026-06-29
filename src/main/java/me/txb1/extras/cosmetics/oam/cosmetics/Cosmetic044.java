package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=44, height=0.2, activatergb=true, displayname="Deadmau5 Ears", type=CosmeticType.DEFAULT)
public class Cosmetic044
extends CosmeticModelRenderer {
    CosmeticModelRenderer bipedDeadmau5Head;

    public Cosmetic044(ModelBase base) {
        super(base);
        this.bipedDeadmau5Head = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.bipedDeadmau5Head.addBox(-3.0f, -6.0f, -1.0f, 6, 6, 1, 0.0f);
    }

    @Override
    public void render(float scale) {
        this.renderRGB("color", 100, 100, 100);
        this.bindCosmeticTexture("deadmau5_ears.png");
        this.bipedDeadmau5Head.rotationPointX = 0.0f;
        this.bipedDeadmau5Head.rotationPointY = 0.0f;
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)-0.375f, (float)0.0f);
        for (int i = 0; i < 2; ++i) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(0.375f * (float)(i * 2 - 1)), (float)0.0f, (float)0.0f);
            float f2 = 1.3333334f;
            GL11.glScalef((float)f2, (float)f2, (float)f2);
            this.bipedDeadmau5Head.render(scale);
            GL11.glPopMatrix();
            GL11.glScalef((float)-1.0f, (float)1.0f, (float)1.0f);
            GL11.glTranslatef((float)-0.75f, (float)0.0f, (float)0.0f);
        }
        super.render(scale);
    }
}

