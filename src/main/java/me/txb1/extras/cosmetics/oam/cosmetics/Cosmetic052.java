package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=52, height=0.0, activatergb=false, displayname="Rockets", type=CosmeticType.LIMITED)
public class Cosmetic052
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

    public Cosmetic052(ModelBase base) {
        super(base);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(512, 512);
        this.Shape1.addBox(0.0f, 0.0f, 0.0f, 3, 49, 3);
        this.Shape1.setRotationPoint(0.5f, -25.0f, 0.5f);
        this.Shape1.setTextureSize(512, 512);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0.0f, 0.0f, 0.0f);
        this.Shape2 = new CosmeticModelRenderer(base, 23, 0).setTextureSize(512, 512);
        this.Shape2.addBox(0.0f, 0.0f, 0.0f, 14, 34, 14);
        this.Shape2.setRotationPoint(-5.0f, -58.0f, -5.0f);
        this.Shape2.setTextureSize(512, 512);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0.0f, 0.0f, 0.0f);
        this.Shape3 = new CosmeticModelRenderer(base, 0, 61).setTextureSize(512, 512);
        this.Shape3.addBox(0.0f, 0.0f, 0.0f, 12, 1, 12);
        this.Shape3.setRotationPoint(-4.0f, -59.0f, -4.0f);
        this.Shape3.setTextureSize(512, 512);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0.0f, 0.0f, 0.0f);
        this.Shape4 = new CosmeticModelRenderer(base, 0, 80).setTextureSize(512, 512);
        this.Shape4.addBox(0.0f, 0.0f, 0.0f, 10, 1, 10);
        this.Shape4.setRotationPoint(-3.0f, -60.0f, -3.0f);
        this.Shape4.setTextureSize(512, 512);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0.0f, 0.0f, 0.0f);
        this.Shape5 = new CosmeticModelRenderer(base, 0, 97).setTextureSize(512, 512);
        this.Shape5.addBox(0.0f, 0.0f, 0.0f, 8, 1, 8);
        this.Shape5.setRotationPoint(-2.0f, -61.0f, -2.0f);
        this.Shape5.setTextureSize(512, 512);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0.0f, 0.0f, 0.0f);
        this.Shape6 = new CosmeticModelRenderer(base, 0, 114).setTextureSize(512, 512);
        this.Shape6.addBox(0.0f, 0.0f, 0.0f, 6, 1, 6);
        this.Shape6.setRotationPoint(-1.0f, -62.0f, -1.0f);
        this.Shape6.setTextureSize(512, 512);
        this.Shape6.mirror = true;
        this.setRotation(this.Shape6, 0.0f, 0.0f, 0.0f);
        this.Shape7 = new CosmeticModelRenderer(base, 0, 130).setTextureSize(512, 512);
        this.Shape7.addBox(0.0f, 0.0f, 0.0f, 4, 1, 4);
        this.Shape7.setRotationPoint(0.0f, -63.0f, 0.0f);
        this.Shape7.setTextureSize(512, 512);
        this.Shape7.mirror = true;
        this.setRotation(this.Shape7, 0.0f, 0.0f, 0.0f);
        this.Shape8 = new CosmeticModelRenderer(base, 0, 142).setTextureSize(512, 512);
        this.Shape8.addBox(0.0f, 0.0f, 0.0f, 2, 1, 2);
        this.Shape8.setRotationPoint(1.0f, -64.0f, 1.0f);
        this.Shape8.setTextureSize(512, 512);
        this.Shape8.mirror = true;
        this.setRotation(this.Shape8, 0.0f, 0.0f, 0.0f);
        this.Shape9 = new CosmeticModelRenderer(base, 88, 0).setTextureSize(512, 512);
        this.Shape9.addBox(0.0f, 0.0f, 0.0f, 1, 17, 1);
        this.Shape9.setRotationPoint(-1.0f, -25.0f, -1.0f);
        this.Shape9.setTextureSize(512, 512);
        this.Shape9.mirror = true;
        this.setRotation(this.Shape9, -0.2268928f, -0.1570796f, 0.296706f);
        this.Shape10 = new CosmeticModelRenderer(base, 0, 155).setTextureSize(512, 512);
        this.Shape10.addBox(0.0f, 0.0f, 0.0f, 15, 34, 15);
        this.Shape10.setRotationPoint(-5.5f, -58.0f, -5.5f);
        this.Shape10.setTextureSize(512, 512);
        this.Shape10.mirror = true;
        this.setRotation(this.Shape10, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("newyear2018.png");
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.2f, (float)0.0f);
            GL11.glRotatef((float)25.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.04f);
        GL11.glRotatef((float)-10.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)10.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)0.48f, (float)-0.2f, (float)-0.01f);
        GL11.glRotatef((float)60.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)-0.2f, (float)0.5f, (float)0.1f);
        for (int i = 0; i < 3; ++i) {
            GL11.glPushMatrix();
            GL11.glRotatef((float)(20 * i), (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glScalef((float)0.15f, (float)0.2f, (float)0.15f);
            switch (i) {
                case 0: {
                    GL11.glRotatef((float)10.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                    break;
                }
                case 2: {
                    GL11.glRotatef((float)-8.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                }
            }
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
            GL11.glPopMatrix();
        }
    }
}

