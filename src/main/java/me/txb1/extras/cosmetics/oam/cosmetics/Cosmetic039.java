package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=39, height=0.0, activatergb=true, displayname="Ootsutsuki Horns", type=CosmeticType.DEFAULT)
public class Cosmetic039
extends CosmeticModelRenderer {
    CosmeticModelRenderer Shape1;
    CosmeticModelRenderer Shape2;
    CosmeticModelRenderer Shape3;
    CosmeticModelRenderer Shape4;

    public Cosmetic039(ModelBase base) {
        super(base);
        this.Shape1 = new CosmeticModelRenderer(base, 11, 11);
        this.Shape1.addBox(0.0f, 0.0f, 0.0f, 2, 2, 1);
        this.Shape1.setRotationPoint(-3.0f, -8.0f, -5.0f);
        this.Shape1.setTextureSize(64, 64);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0.5410521f, 0.0f, 0.0f);
        this.Shape2 = new CosmeticModelRenderer(base, 10, 13);
        this.Shape2.addBox(0.0f, 0.0f, 0.0f, 1, 3, 1);
        this.Shape2.setRotationPoint(-2.5f, -9.0f, -5.0f);
        this.Shape2.setTextureSize(64, 64);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0.5410521f, 0.0f, 0.0f);
        this.Shape3 = new CosmeticModelRenderer(base, 9, 10);
        this.Shape3.addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.Shape3.setRotationPoint(-2.5f, -7.5f, -5.1f);
        this.Shape3.setTextureSize(64, 64);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0.5585054f, 0.0f, 0.0f);
        this.Shape4 = new CosmeticModelRenderer(base, 11, 12);
        this.Shape4.addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.Shape4.setRotationPoint(-2.5f, -10.0f, -5.0f);
        this.Shape4.setTextureSize(64, 64);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0.2617994f, 0.0f, 0.0f);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("resources.png");
        this.renderRGB("color", 100, 100, 100);
        this.setHeadRotations();
        for (int i = 0; i < 2; ++i) {
            this.Shape1.render(scale);
            GL11.glTranslatef((float)0.0365f, (float)0.0f, (float)0.0f);
            GL11.glScalef((float)1.3f, (float)1.0f, (float)1.0f);
            this.Shape2.render(scale);
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.0f);
            GL11.glScalef((float)1.0f, (float)1.0f, (float)1.0f);
            this.Shape3.render(scale);
            GL11.glTranslatef((float)-0.038f, (float)0.0f, (float)0.0f);
            GL11.glScalef((float)0.7f, (float)1.0f, (float)1.0f);
            this.Shape4.render(scale);
            GL11.glTranslatef((float)0.282f, (float)0.0f, (float)0.0151f);
            GL11.glScalef((float)1.06f, (float)1.0f, (float)1.06f);
        }
        super.render(scale);
    }
}

