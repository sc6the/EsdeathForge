package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=53, height=0.0, activatergb=true, displayname="Dragon Tail", type=CosmeticType.DEFAULT)
public class Cosmetic053
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
    CosmeticModelRenderer Shape13;
    HashMap<UUID, Float> actionMap = new HashMap();

    public Cosmetic053(ModelBase base) {
        super(base);
        this.textureWidth = 512.0f;
        this.textureHeight = 256.0f;
        this.Shape6 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(512, 256);
        this.Shape6.addBox(-3.0f, -3.0f, 0.0f, 6, 6, 11);
        this.Shape6.setRotationPoint(1.5f, -13.6f, -14.0f);
        this.Shape6.setTextureSize(512, 256);
        this.Shape6.mirror = true;
        this.setRotation(this.Shape6, -0.1919862f, 0.0f, 0.0f);
        this.Shape7 = new CosmeticModelRenderer(base, 36, 0).setTextureSize(512, 256);
        this.Shape7.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape7.setRotationPoint(-0.5f, -4.5f, 1.0f);
        this.Shape7.setTextureSize(512, 256);
        this.Shape7.mirror = true;
        this.setRotation(this.Shape7, 0.0514f, 0.0f, 0.0f);
        this.Shape6.addChild(this.Shape7);
        this.Shape8 = new CosmeticModelRenderer(base, 47, 0).setTextureSize(512, 256);
        this.Shape8.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape8.setRotationPoint(-0.5f, -4.5f, 6.5f);
        this.Shape8.setTextureSize(512, 256);
        this.Shape8.mirror = true;
        this.setRotation(this.Shape8, -0.05241f, 0.0f, 0.0f);
        this.Shape6.addChild(this.Shape8);
        this.Shape3 = new CosmeticModelRenderer(base, 0, 22).setTextureSize(512, 256);
        this.Shape3.addBox(-2.5f, -2.5f, 0.0f, 5, 5, 8);
        this.Shape3.setRotationPoint(0.0f, -0.4f, 10.0f);
        this.Shape3.setTextureSize(512, 256);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, -0.24f, 0.0f, 0.0f);
        this.Shape6.addChild(this.Shape3);
        this.Shape9 = new CosmeticModelRenderer(base, 57, 0).setTextureSize(512, 256);
        this.Shape9.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape9.setRotationPoint(-0.5f, -4.0f, 3.0f);
        this.Shape9.setTextureSize(512, 256);
        this.Shape9.mirror = true;
        this.setRotation(this.Shape9, -0.06f, 0.0f, 0.0f);
        this.Shape3.addChild(this.Shape9);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 42).setTextureSize(512, 256);
        this.Shape1.addBox(-2.0f, 0.0f, 0.0f, 4, 7, 4);
        this.Shape1.setRotationPoint(0.0f, 2.0f, 7.0f);
        this.Shape1.setTextureSize(512, 256);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 1.44419f, 0.0f, 0.0f);
        this.Shape3.addChild(this.Shape1);
        this.Shape10 = new CosmeticModelRenderer(base, 35, 7).setTextureSize(512, 256);
        this.Shape10.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape10.setRotationPoint(-0.5f, 2.0f, 5.5f);
        this.Shape10.setTextureSize(512, 256);
        this.Shape10.mirror = true;
        this.setRotation(this.Shape10, -1.663225f, 0.0f, 0.0f);
        this.Shape1.addChild(this.Shape10);
        this.Shape2 = new CosmeticModelRenderer(base, 0, 56).setTextureSize(512, 256);
        this.Shape2.addBox(-1.5f, 0.0f, 0.0f, 3, 7, 3);
        this.Shape2.setRotationPoint(0.0f, 6.5f, 0.5f);
        this.Shape2.setTextureSize(512, 256);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, -0.13663f, 0.0174533f, 0.0f);
        this.Shape1.addChild(this.Shape2);
        this.Shape11 = new CosmeticModelRenderer(base, 45, 7).setTextureSize(512, 256);
        this.Shape11.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape11.setRotationPoint(-0.5f, 2.0f, 4.0f);
        this.Shape11.setTextureSize(512, 256);
        this.Shape11.mirror = true;
        this.setRotation(this.Shape11, -1.678515f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape11);
        this.Shape4 = new CosmeticModelRenderer(base, 0, 68).setTextureSize(512, 256);
        this.Shape4.addBox(-1.0f, 0.0f, 0.0f, 2, 7, 2);
        this.Shape4.setRotationPoint(0.0f, 6.5f, 0.5f);
        this.Shape4.setTextureSize(512, 256);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, -0.1077151f, 0.0f, 0.0f);
        this.Shape2.addChild(this.Shape4);
        this.Shape12 = new CosmeticModelRenderer(base, 56, 7).setTextureSize(512, 256);
        this.Shape12.addBox(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.Shape12.setRotationPoint(-0.5f, 2.0f, 2.5f);
        this.Shape12.setTextureSize(512, 256);
        this.Shape12.mirror = true;
        this.setRotation(this.Shape12, -1.6928833f, 0.0f, 0.0f);
        this.Shape4.addChild(this.Shape12);
        this.Shape5 = new CosmeticModelRenderer(base, 0, 80).setTextureSize(512, 256);
        this.Shape5.addBox(-0.5f, 0.0f, 0.0f, 1, 7, 1);
        this.Shape5.setRotationPoint(-0.0f, 7.0f, 0.5f);
        this.Shape5.setTextureSize(512, 256);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0.3151491f, 0.0f, 0.0f);
        this.Shape4.addChild(this.Shape5);
        this.Shape13 = new CosmeticModelRenderer(base, 0, 110).setTextureSize(512, 256);
        this.Shape13.addBox(0.0f, 0.0f, 0.0f, 10, 1, 12);
        this.Shape13.setRotationPoint(-5.0f, 0.0f, 0.5f);
        this.Shape13.setTextureSize(512, 256);
        this.Shape13.mirror = true;
        this.setRotation(this.Shape13, -1.5542465f, 0.0f, 0.0f);
        this.Shape5.addChild(this.Shape13);
    }

    @Override
    public void render(float scale) {
        if (!this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            this.actionMap.put(this.entityIn.getUniqueID(), Float.valueOf(0.0f));
        }
        this.bindCosmeticTexture("dragontail.png");
        this.renderRGB("color", 100, 100, 100);
        GL11.glTranslatef((float)-0.045f, (float)1.07f, (float)0.5f);
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)-0.4f, (float)0.9f);
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        float wingspeed = 1.0f;
        float f2 = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
        float pi4 = 0.7853975f;
        this.Shape6.rotateAngleY = MathHelper.cos((float)f2) * 0.1f;
        this.Shape3.rotateAngleY = MathHelper.cos((float)f2) * 0.2f;
        this.Shape1.rotateAngleY = MathHelper.cos((float)f2) * 0.3f;
        this.Shape2.rotateAngleZ = -MathHelper.cos((float)f2) * 0.35f;
        this.Shape4.rotateAngleZ = -MathHelper.cos((float)f2) * 0.4f;
        this.Shape5.rotateAngleZ = -MathHelper.cos((float)f2) * 0.45f;
        this.Shape6.render(scale);
    }

    @Override
    public void performAnimation() {
        if (this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            if (this.entityIn.prevDistanceWalkedModified == this.entityIn.distanceWalkedModified) {
                this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(this.actionMap.get(this.entityIn.getUniqueID()).floatValue() + 0.004f));
            } else if (this.entityIn.isSprinting()) {
                this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(this.actionMap.get(this.entityIn.getUniqueID()).floatValue() + 0.03f));
            } else {
                this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(this.actionMap.get(this.entityIn.getUniqueID()).floatValue() + 0.01f));
            }
        }
        super.performAnimation();
    }
}

