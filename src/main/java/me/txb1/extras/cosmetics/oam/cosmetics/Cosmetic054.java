package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=54, activatergb=true, displayname="Shoes", height=0.0, type=CosmeticType.DEFAULT)
public class Cosmetic054
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

    public Cosmetic054(ModelBase base) {
        super(base);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.Shape2 = new CosmeticModelRenderer(base, 51, 0);
        this.Shape2.addBox(0.0f, -6.0f, -0.8f, 4, 4, 1);
        this.Shape2.setRotationPoint(4.4f, 19.1f, -2.1f);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0.0f, 0.0f, 0.0f);
        this.Shape1.addChild(this.Shape2);
        this.Shape3 = new CosmeticModelRenderer(base, 17, 6).setTextureSize(64, 32);
        this.Shape3.addBox(-1.0f, 0.0f, 0.0f, 1, 4, 6);
        this.Shape3.setRotationPoint(5.0f, -6.0f, -0.8f);
        this.Shape3.setTextureSize(64, 32);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape3);
        this.Shape4 = new CosmeticModelRenderer(base, 35, 10).setTextureSize(64, 32);
        this.Shape4.addBox(0.0f, 0.0f, 0.0f, 1, 4, 6);
        this.Shape4.setRotationPoint(-1.0f, -6.0f, -0.8f);
        this.Shape4.setTextureSize(64, 32);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape4);
        this.Shape5 = new CosmeticModelRenderer(base, 32, 22).setTextureSize(64, 32);
        this.Shape5.addBox(0.0f, 0.0f, -2.0f, 4, 1, 2);
        this.Shape5.setRotationPoint(0.0f, -5.7f, 5.2f);
        this.Shape5.setTextureSize(64, 32);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape5);
        this.Shape6 = new CosmeticModelRenderer(base, 35, 0).setTextureSize(64, 32);
        this.Shape6.addBox(0.0f, 0.0f, 0.0f, 6, 3, 1);
        this.Shape6.setRotationPoint(-1.0f, -5.0f, 5.2f);
        this.Shape6.setTextureSize(64, 32);
        this.Shape6.mirror = true;
        this.setRotation(this.Shape6, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape6);
        this.Shape7 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.Shape7.addBox(0.0f, 0.0f, 0.0f, 6, 2, 1);
        this.Shape7.setRotationPoint(-1.0f, -4.0f, 6.2f);
        this.Shape7.setTextureSize(64, 32);
        this.Shape7.mirror = true;
        this.setRotation(this.Shape7, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape7);
        this.Shape8 = new CosmeticModelRenderer(base, 0, 20).setTextureSize(64, 32);
        this.Shape8.addBox(0.0f, 0.0f, 0.0f, 6, 1, 8);
        this.Shape8.setRotationPoint(-1.0f, -2.0f, -0.8f);
        this.Shape8.setTextureSize(64, 32);
        this.Shape8.mirror = true;
        this.setRotation(this.Shape8, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape8);
        this.Shape9 = new CosmeticModelRenderer(base, 54, 19).setTextureSize(64, 32);
        this.Shape9.addBox(0.0f, -3.0f, 0.0f, 4, 2, 1);
        this.Shape9.setRotationPoint(0.0f, -4.7f, 3.6f);
        this.Shape9.setTextureSize(64, 32);
        this.Shape9.mirror = true;
        this.setRotation(this.Shape9, -0.1919862f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape9);
        this.Shape10 = new CosmeticModelRenderer(base, 35, 1).setTextureSize(64, 32);
        this.Shape10.addBox(0.0f, 0.0f, 0.0f, 6, 2, 1);
        this.Shape10.setRotationPoint(-1.0f, -4.0f, 7.2f);
        this.Shape10.setTextureSize(64, 32);
        this.Shape10.mirror = true;
        this.setRotation(this.Shape10, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape10);
        this.Shape11 = new CosmeticModelRenderer(base, 7, 27).setTextureSize(64, 32);
        this.Shape11.addBox(0.0f, 0.0f, 0.0f, 6, 1, 1);
        this.Shape11.setRotationPoint(-1.0f, -2.0f, 7.2f);
        this.Shape11.setTextureSize(64, 32);
        this.Shape11.mirror = true;
        this.setRotation(this.Shape11, 0.0f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape11);
        this.Shape12 = new CosmeticModelRenderer(base, 0, 30).setTextureSize(64, 32);
        this.Shape12.addBox(0.0f, 0.0f, 0.0f, 4, 1, 1);
        this.Shape12.setRotationPoint(0.0f, -4.2f, 6.3f);
        this.Shape12.setTextureSize(64, 32);
        this.Shape12.mirror = true;
        this.setRotation(this.Shape12, -0.0698132f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape12);
    }

    @Override
    public void render(float f5) {
        GL11.glPushMatrix();
        this.bindCosmeticTexture("shoes.png");
        this.renderRGB("color", 100, 100, 100);
        this.Shape1.rotateAngleY = 3.14f;
        GL11.glScalef((float)0.8f, (float)0.8f, (float)0.8f);
        for (int i = 0; i < 2; ++i) {
            switch (i) {
                case 0: {
                    this.Shape1.rotateAngleX = MathHelper.cos((float)(this.ticks_2 * 0.6662f)) * 1.4f * this.ticks_3 * 0.84f;
                    this.Shape1.rotationPointX = 8.8f;
                    break;
                }
                case 1: {
                    this.Shape1.rotateAngleX = MathHelper.cos((float)(this.ticks_2 * 0.6662f + (float)Math.PI)) * 1.4f * this.ticks_3 * 0.84f;
                    this.Shape1.rotationPointX = 4.0f;
                }
            }
            if (this.entityIn.isRiding()) {
                this.Shape1.rotateAngleX = -1.2566371f;
                this.Shape1.rotateAngleY = -0.31415927f;
            }
            if (this.isSneaking()) {
                this.Shape1.rotationPointZ = 5.0f;
                this.Shape1.rotationPointY = 12.2f;
            } else {
                this.Shape1.rotationPointZ = 0.1f;
                this.Shape1.rotationPointY = 12.0f;
            }
            this.Shape1.render(f5);
        }
        GL11.glPopMatrix();
        super.render(f5);
    }
}

