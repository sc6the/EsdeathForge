package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=46, height=0.0, activatergb=false, displayname="Angel Wings", type=CosmeticType.DEFAULT)
public class Cosmetic046
extends CosmeticModelRenderer {
    CosmeticModelRenderer Shape1;
    CosmeticModelRenderer Shape2;
    CosmeticModelRenderer Shape3;
    CosmeticModelRenderer Shape4;
    CosmeticModelRenderer Shape5;
    CosmeticModelRenderer Shape6;
    CosmeticModelRenderer wing;
    CosmeticModelRenderer wingTip;
    HashMap<UUID, Float> actionMap = new HashMap();

    public Cosmetic046(ModelBase base) {
        super(base);
        this.wing = new CosmeticModelRenderer(base);
        this.wing.setRotationPoint(-12.0f, 5.0f, 2.0f);
        this.wingTip = new CosmeticModelRenderer(base);
        this.wingTip.setRotationPoint(-56.0f, 1.5f, 1.5f);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(512, 256);
        this.Shape1.addBox(-61.0f, 2.0f, 1.0f, 61, 93, 1);
        this.wingTip.addChild(this.Shape1);
        this.Shape2 = new CosmeticModelRenderer(base, 133, 0).setTextureSize(512, 256);
        this.Shape2.addBox(-55.0f, 5.0f, 2.0f, 55, 93, 1);
        this.wing.addChild(this.Shape2);
        this.Shape3 = new CosmeticModelRenderer(base, 0, 101).setTextureSize(512, 256);
        this.Shape3.addBox(-55.0f, 0.0f, 0.0f, 55, 5, 5);
        this.wing.addChild(this.Shape3);
        this.Shape4 = new CosmeticModelRenderer(base, 131, 103).setTextureSize(512, 256);
        this.Shape4.addBox(-61.0f, 0.0f, 0.0f, 61, 2, 2);
        this.wingTip.addChild(this.Shape4);
        this.Shape5 = new CosmeticModelRenderer(base, 0, 120).setTextureSize(512, 256);
        this.Shape5.addBox(-57.0f, 1.0f, 1.0f, 3, 3, 3);
        this.wing.addChild(this.Shape5);
        this.Shape6 = new CosmeticModelRenderer(base, 260, 0).setTextureSize(512, 256);
        this.Shape6.addBox(-61.0f, -51.0f, 1.0f, 61, 51, 1);
        this.wingTip.addChild(this.Shape6);
        this.wing.addChild(this.wingTip);
    }

    @Override
    public void render(float scale) {
        if (!this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            this.actionMap.put(this.entityIn.getUniqueID(), Float.valueOf(0.0f));
        }
        this.bindCosmeticTexture("angelwings.png");
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.2f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-0.06f, (float)0.0f);
        }
        GL11.glRotatef((float)15.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.02f);
        GL11.glScalef((float)0.15f, (float)0.15f, (float)0.15f);
        for (int i = 0; i < 2; ++i) {
            switch (i) {
                case 0: {
                    GL11.glRotatef((float)30.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                    break;
                }
                case 1: {
                    GL11.glRotatef((float)60.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                }
            }
            float ticks = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
            this.wing.rotateAngleY = 0.125f - (float)Math.cos(ticks) * 0.2f;
            this.wingTip.rotateAngleY = (float)Math.cos(ticks + 2.0f) / 1.5f;
            this.wing.render(scale);
            GL11.glScalef((float)-1.0f, (float)1.0f, (float)1.0f);
        }
        super.render(scale);
    }

    @Override
    public void performAnimation() {
        if (this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            float ticks = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
            if (this.entityIn.isSneaking()) {
                ticks += 0.003f;
            } else {
                ticks += 0.007f;
                if (this.entityIn.isSprinting()) {
                    ticks += 0.017f;
                }
            }
            this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(ticks));
        }
        super.performAnimation();
    }
}

