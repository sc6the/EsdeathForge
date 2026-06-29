package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=38, height=0.1, activatergb=false, displayname="Yondu Fin", type=CosmeticType.DEFAULT)
public class Cosmetic038
extends CosmeticModelRenderer {
    CosmeticModelRenderer Shape1;
    CosmeticModelRenderer Shape2;
    CosmeticModelRenderer Shape3;
    CosmeticModelRenderer Shape4;
    CosmeticModelRenderer Shape5;
    CosmeticModelRenderer Shape6;
    CosmeticModelRenderer Shape7;
    CosmeticModelRenderer Shape8;
    CosmeticModelRenderer Shape9;
    CosmeticModelRenderer Shape10;
    CosmeticModelRenderer Shape11;
    CosmeticModelRenderer Shape12;
    CosmeticModelRenderer model;

    public Cosmetic038(ModelBase base) {
        super(base);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape1.addBox(0.0f, 0.0f, 0.0f, 1, 2, 7);
        this.Shape1.setRotationPoint(-0.5f, -10.0f, -4.0f);
        this.Shape1.setTextureSize(64, 32);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0.0f, 0.0f, 0.0f);
        this.Shape2 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape2.addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
        this.Shape2.setRotationPoint(-0.5f, -9.0f, 2.7f);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, -0.2443461f, 0.0f, 0.0f);
        this.Shape3 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape3.addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
        this.Shape3.setRotationPoint(-0.5f, -12.0f, -5.0f);
        this.Shape3.setTextureSize(64, 32);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0.3839724f, 0.0f, 0.0f);
        this.Shape4 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape4.addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
        this.Shape4.setRotationPoint(-0.5f, -11.0f, -4.0f);
        this.Shape4.setTextureSize(64, 32);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0.6981317f, 0.0f, 0.0f);
        this.Shape5 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape5.addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
        this.Shape5.setRotationPoint(-0.5f, -10.9f, 2.0f);
        this.Shape5.setTextureSize(64, 32);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0.2268928f, 0.0f, 0.0f);
        this.Shape6 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape6.addBox(0.0f, 0.0f, 0.0f, 1, 1, 2);
        this.Shape6.setRotationPoint(-0.5f, -11.9f, 2.0f);
        this.Shape6.setTextureSize(64, 32);
        this.Shape6.mirror = true;
        this.setRotation(this.Shape6, -0.2617994f, 0.0f, 0.0f);
        this.Shape7 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape7.addBox(0.0f, 0.0f, 0.0f, 1, 2, 4);
        this.Shape7.setRotationPoint(-0.5f, -12.5f, -1.9f);
        this.Shape7.setTextureSize(64, 32);
        this.Shape7.mirror = true;
        this.setRotation(this.Shape7, -0.1570796f, 0.0f, 0.0f);
        this.Shape8 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape8.addBox(0.0f, 0.0f, 0.0f, 1, 3, 1);
        this.Shape8.setRotationPoint(-0.5f, -12.7f, -3.7f);
        this.Shape8.setTextureSize(64, 32);
        this.Shape8.mirror = true;
        this.setRotation(this.Shape8, 0.8656833f, 0.0f, 0.0f);
        this.Shape9 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape9.addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.Shape9.setRotationPoint(-0.5f, -14.0f, -5.0f);
        this.Shape9.setTextureSize(64, 32);
        this.Shape9.mirror = true;
        this.setRotation(this.Shape9, 0.0f, 0.0f, 0.0f);
        this.Shape10 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape10.addBox(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Shape10.setRotationPoint(-0.5f, -13.8f, -4.0f);
        this.Shape10.setTextureSize(64, 32);
        this.Shape10.mirror = true;
        this.setRotation(this.Shape10, -0.3316126f, 0.0f, 0.0f);
        this.Shape11 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape11.addBox(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Shape11.setRotationPoint(-0.5f, -13.0f, -4.0f);
        this.Shape11.setTextureSize(64, 32);
        this.Shape11.mirror = true;
        this.setRotation(this.Shape11, 0.0f, 0.0f, 0.0f);
        this.Shape12 = new CosmeticModelRenderer(base, 0, 0);
        this.Shape12.addBox(0.0f, 0.0f, 0.0f, 1, 2, 4);
        this.Shape12.setRotationPoint(-0.5f, -11.0f, -1.0f);
        this.Shape12.setTextureSize(64, 32);
        this.Shape12.mirror = true;
        this.setRotation(this.Shape12, 0.0f, 0.0f, 0.0f);
        this.model = new CosmeticModelRenderer(base).setTextureSize(64, 32);
        this.model.addBox(-0.5f, -15.0f, -6.0f, 1, 10, 12);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("resources.png");
        GL11.glDisable((int)2896);
        GL11.glColor3f((float)0.12f, (float)0.0f, (float)0.0f);
        this.setHeadRotations();
        this.Shape1.render(scale);
        this.Shape2.render(scale);
        this.Shape3.render(scale);
        this.Shape4.render(scale);
        this.Shape5.render(scale);
        this.Shape6.render(scale);
        this.Shape7.render(scale);
        this.Shape8.render(scale);
        this.Shape9.render(scale);
        this.Shape10.render(scale);
        this.Shape11.render(scale);
        this.Shape12.render(scale);
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.bindCosmeticTexture("yondu_fin.png");
        GL11.glScalef((float)1.01f, (float)1.0f, (float)1.0f);
        this.model.render(scale);
        GL11.glEnable((int)2896);
        super.render(scale);
    }
}

