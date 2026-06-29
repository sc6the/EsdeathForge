package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=37, height=0.0, activatergb=true, displayname="Nine Tails", type=CosmeticType.DEFAULT)
public class Cosmetic037
extends CosmeticModelRenderer {
    CosmeticModelRenderer tail9;
    CosmeticModelRenderer tail8;
    CosmeticModelRenderer tail7;
    CosmeticModelRenderer tail6;
    CosmeticModelRenderer tail5;
    CosmeticModelRenderer tail4;
    CosmeticModelRenderer tail3;
    CosmeticModelRenderer tail2;
    CosmeticModelRenderer tail1;
    CosmeticModelRenderer tail0;
    CosmeticModelRenderer tail;
    HashMap<UUID, Float> actionMap = new HashMap();

    public Cosmetic037(ModelBase base) {
        super(base);
        this.tail = new CosmeticModelRenderer(base, 145, 47);
        this.tail9 = new CosmeticModelRenderer(base, 145, 47);
        this.tail9.addBox(-0.5f, -0.5f, -1.0f, 1, 1, 1);
        this.tail9.setRotationPoint(0.0f, 9.0f, -26.0f);
        this.setRotation(this.tail9, 2.007645f, 0.0f, 0.0f);
        this.tail.addChild(this.tail9);
        this.tail8 = new CosmeticModelRenderer(base, 135, 45);
        this.tail8.addBox(-1.0f, -1.0f, -2.0f, 2, 2, 2);
        this.tail8.setRotationPoint(0.0f, 7.0f, -25.75f);
        this.setRotation(this.tail8, 1.524323f, 0.0f, 0.0f);
        this.tail.addChild(this.tail8);
        this.tail7 = new CosmeticModelRenderer(base, 122, 44);
        this.tail7.addBox(-1.5f, -1.5f, -3.0f, 3, 3, 3);
        this.tail7.setRotationPoint(0.0f, 5.0f, -24.0f);
        this.setRotation(this.tail7, 0.8922867f, 0.0f, 0.0f);
        this.tail.addChild(this.tail7);
        this.tail6 = new CosmeticModelRenderer(base, 105, 43);
        this.tail6.addBox(-2.0f, -2.0f, -4.0f, 4, 4, 4);
        this.tail6.setRotationPoint(0.0f, 3.0f, -21.0f);
        this.setRotation(this.tail6, 0.6320364f, 0.0f, 0.0f);
        this.tail.addChild(this.tail6);
        this.tail5 = new CosmeticModelRenderer(base, 84, 42);
        this.tail5.addBox(-2.5f, -2.5f, -5.0f, 5, 5, 5);
        this.tail5.setRotationPoint(0.0f, 2.0f, -17.0f);
        this.setRotation(this.tail5, 0.2230717f, 0.0f, 0.0f);
        this.tail.addChild(this.tail5);
        this.tail3 = new CosmeticModelRenderer(base, 38, 42);
        this.tail3.addBox(-2.5f, -2.0f, -5.0f, 5, 5, 5);
        this.tail3.setRotationPoint(0.0f, 6.5f, -10.0f);
        this.setRotation(this.tail3, -0.96f, 0.0f, 0.0f);
        this.tail.addChild(this.tail3);
        this.tail4 = new CosmeticModelRenderer(base, 59, 41);
        this.tail4.addBox(-3.0f, -3.0f, -6.0f, 6, 6, 6);
        this.tail4.setRotationPoint(0.0f, 3.0f, -12.0f);
        this.setRotation(this.tail4, -0.22f, 0.0f, 0.0f);
        this.tail.addChild(this.tail4);
        this.tail2 = new CosmeticModelRenderer(base, 20, 43);
        this.tail2.addBox(-2.0f, -2.0f, -5.0f, 4, 4, 5);
        this.tail2.setRotationPoint(0.0f, 10.0f, -7.0f);
        this.setRotation(this.tail2, -0.7807508f, 0.0f, 0.0f);
        this.tail.addChild(this.tail2);
        this.tail1 = new CosmeticModelRenderer(base, 9, 36);
        this.tail1.addBox(-1.5f, -1.5f, -3.0f, 3, 3, 3);
        this.tail1.setRotationPoint(0.0f, 10.0f, -4.333333f);
        this.setRotation(this.tail1, -0.2602503f, 0.0f, 0.0f);
        this.tail.addChild(this.tail1);
        this.tail0 = new CosmeticModelRenderer(base, 0, 46);
        this.tail0.addBox(-1.0f, -1.0f, -3.0f, 4, 4, 3);
        this.tail0.setRotationPoint(-1.0f, 9.0f, -2.0f);
        this.setRotation(this.tail0, 0.0f, 0.0f, 0.0f);
        this.tail.addChild(this.tail0);
    }

    @Override
    public void render(float scale) {
        if (!this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            this.actionMap.put(this.entityIn.getUniqueID(), Float.valueOf(0.0f));
        }
        int tails = 1;
        if (this.cosmetic.getInteger("tails") > 0 && this.cosmetic.getInteger("tails") < 10) {
            tails = this.cosmetic.getInteger("tails");
        }
        float rotation = 1.0f;
        if (this.isSneaking()) {
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        GL11.glScalef((float)0.7f, (float)0.7f, (float)0.7f);
        this.renderRGB("color", 234, 151, 49);
        GL11.glTranslatef((float)0.0f, (float)0.27f, (float)0.06f);
        switch (tails) {
            case 1: {
                GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.01f), (float)0.0f, (float)0.0f);
                break;
            }
            case 2: {
                GL11.glRotatef((float)195.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.05f), (float)0.0f, (float)0.0f);
                rotation = 4.0f;
                break;
            }
            case 3: {
                GL11.glRotatef((float)210.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.04f), (float)0.0f, (float)0.0f);
                rotation = 6.5f;
                break;
            }
            case 4: {
                GL11.glRotatef((float)210.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.035f), (float)0.0f, (float)0.02f);
                rotation = 12.0f;
                break;
            }
            case 5: {
                GL11.glRotatef((float)225.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.01f), (float)0.0f, (float)0.02f);
                rotation = 50.0f;
                break;
            }
            case 6: {
                GL11.glRotatef((float)230.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)((float)(-tails) * 0.0f), (float)0.0f, (float)0.02f);
                rotation = 10000.0f;
                break;
            }
            case 7: {
                GL11.glRotatef((float)240.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.02f);
                rotation = 10000.0f;
                break;
            }
            case 8: {
                GL11.glRotatef((float)250.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.02f);
                rotation = 1000000.0f;
                break;
            }
            case 9: {
                GL11.glRotatef((float)260.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.02f);
                rotation = 1000000.0f;
            }
        }
        for (int i = 0; i < tails; ++i) {
            float wingspeed = 1.0f;
            if (i == 0) {
                wingspeed = 1.5f;
            }
            if (i < 4 && i > 1) {
                wingspeed *= (float)i / 3.5f;
            }
            if (i > 3) {
                wingspeed *= (float)i / 10.0f;
            }
            float f2 = this.ticks_2 / 1.7f + this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
            float pi4 = 0.7853975f;
            this.tail1.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed)) * (float)Math.PI * 0.2f;
            this.tail2.rotationPointX = this.tail1.rotationPointX - (float)Math.sin(this.tail1.rotateAngleY) * 3.0f;
            this.tail2.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - pi4)) * (float)Math.PI * 0.2f;
            this.tail3.rotationPointX = this.tail2.rotationPointX - (float)Math.sin(this.tail2.rotateAngleY) * 4.0f;
            this.tail3.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 2.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail4.rotationPointX = this.tail3.rotationPointX - (float)Math.sin(this.tail3.rotateAngleY) * 3.5f;
            this.tail4.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 3.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail5.rotationPointX = this.tail4.rotationPointX - (float)Math.sin(this.tail4.rotateAngleY) * 5.0f;
            this.tail5.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 4.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail6.rotationPointX = this.tail5.rotationPointX - (float)Math.sin(this.tail5.rotateAngleY) * 4.0f;
            this.tail6.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 5.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail7.rotationPointX = this.tail6.rotationPointX - (float)Math.sin(this.tail6.rotateAngleY) * 3.0f;
            this.tail7.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 6.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail8.rotationPointX = this.tail7.rotationPointX - (float)Math.sin(this.tail7.rotateAngleY) * 2.0f;
            this.tail8.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 7.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail9.rotationPointX = this.tail8.rotationPointX - (float)Math.sin(this.tail8.rotateAngleY) * 1.0f;
            this.tail9.rotateAngleY = MathHelper.cos((float)(f2 * 0.9f * wingspeed - 8.0f * pi4)) * (float)Math.PI * 0.2f;
            this.tail1.rotateAngleX = -0.26f + MathHelper.cos((float)(f2 * 0.5f * wingspeed)) * (float)Math.PI * 0.1f;
            this.tail2.rotationPointY = this.tail1.rotationPointY + (float)Math.sin(this.tail1.rotateAngleX) * 3.0f;
            this.tail2.rotationPointZ = this.tail1.rotationPointZ - (float)Math.cos(this.tail1.rotateAngleX) * 3.0f;
            this.tail2.rotateAngleX = -0.78f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - pi4)) * (float)Math.PI * 0.1f;
            this.tail3.rotationPointY = this.tail2.rotationPointY + (float)Math.sin(this.tail2.rotateAngleX) * 4.0f;
            this.tail3.rotationPointZ = this.tail2.rotationPointZ - (float)Math.cos(this.tail2.rotateAngleX) * 4.0f;
            this.tail3.rotateAngleX = -1.11f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 2.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail4.rotationPointY = this.tail3.rotationPointY + (float)Math.sin(this.tail3.rotateAngleX) * 3.5f;
            this.tail4.rotationPointZ = this.tail3.rotationPointZ - (float)Math.cos(this.tail3.rotateAngleX) * 3.5f;
            this.tail4.rotateAngleX = -0.18f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 3.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail5.rotationPointY = this.tail4.rotationPointY + (float)Math.sin(this.tail4.rotateAngleX) * 5.0f;
            this.tail5.rotationPointZ = this.tail4.rotationPointZ - (float)Math.cos(this.tail4.rotateAngleX) * 5.0f;
            this.tail5.rotateAngleX = 0.22f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 4.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail6.rotationPointY = this.tail5.rotationPointY + (float)Math.sin(this.tail5.rotateAngleX) * 4.0f;
            this.tail6.rotationPointZ = this.tail5.rotationPointZ - (float)Math.cos(this.tail5.rotateAngleX) * 4.0f;
            this.tail6.rotateAngleX = 0.63f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 5.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail7.rotationPointY = this.tail6.rotationPointY + (float)Math.sin(this.tail6.rotateAngleX) * 3.0f;
            this.tail7.rotationPointZ = this.tail6.rotationPointZ - (float)Math.cos(this.tail6.rotateAngleX) * 3.0f;
            this.tail7.rotateAngleX = 0.89f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 6.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail8.rotationPointY = this.tail7.rotationPointY + (float)Math.sin(this.tail7.rotateAngleX) * 2.0f;
            this.tail8.rotationPointZ = this.tail7.rotationPointZ - (float)Math.cos(this.tail7.rotateAngleX) * 2.0f;
            this.tail8.rotateAngleX = 1.52f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 7.0f * pi4)) * (float)Math.PI * 0.1f;
            this.tail9.rotationPointY = this.tail8.rotationPointY + (float)Math.sin(this.tail8.rotateAngleX) * 2.0f;
            this.tail9.rotationPointZ = this.tail8.rotationPointZ - (float)Math.cos(this.tail8.rotateAngleX) * 2.0f;
            this.tail9.rotateAngleX = 2.0f + MathHelper.cos((float)(f2 * 0.5f * wingspeed - 8.0f * pi4)) * (float)Math.PI * 0.1f;
            this.bindCosmeticTexture("resources.png");
            GL11.glPushMatrix();
            GL11.glRotatef((float)(-20 * i), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glTranslatef((float)((float)i / rotation), (float)0.0f, (float)0.0f);
            this.tail.render(scale);
            GL11.glPopMatrix();
        }
        super.render(scale);
    }

    @Override
    public void performAnimation() {
        if (this.actionMap.containsKey(this.entityIn.getUniqueID()) && this.entityIn.prevDistanceWalkedModified == this.entityIn.distanceWalkedModified) {
            this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(this.actionMap.get(this.entityIn.getUniqueID()).floatValue() + 0.004f));
        }
    }
}

