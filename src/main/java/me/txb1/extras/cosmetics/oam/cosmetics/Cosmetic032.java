package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=32, height=0.0, activatergb=false, displayname="Spiderlegs", type=CosmeticType.DEFAULT)
public class Cosmetic032
extends CosmeticModelRenderer {
    long countdown = 0L;
    CosmeticModelRenderer spiderLeg1;
    CosmeticModelRenderer spiderLeg2;
    CosmeticModelRenderer spiderLeg3;
    CosmeticModelRenderer spiderLeg4;
    CosmeticModelRenderer spiderLeg5;
    CosmeticModelRenderer spiderLeg6;
    CosmeticModelRenderer spiderLeg7;
    CosmeticModelRenderer spiderLeg8;

    public Cosmetic032(ModelBase base) {
        super(base);
        float f = 0.0f;
        int i = 15;
        this.spiderLeg1 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg1.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg1.setRotationPoint(-4.0f, i, 2.0f);
        this.spiderLeg2 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg2.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg2.setRotationPoint(4.0f, i, 2.0f);
        this.spiderLeg3 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg3.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg3.setRotationPoint(-4.0f, i, 1.0f);
        this.spiderLeg4 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg4.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg4.setRotationPoint(4.0f, i, 1.0f);
        this.spiderLeg5 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg5.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg5.setRotationPoint(-4.0f, i, 0.0f);
        this.spiderLeg6 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg6.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg6.setRotationPoint(4.0f, i, 0.0f);
        this.spiderLeg7 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg7.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg7.setRotationPoint(-4.0f, i, -1.0f);
        this.spiderLeg8 = new CosmeticModelRenderer(base, 18, 0).setTextureSize(64, 32);
        this.spiderLeg8.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, f);
        this.spiderLeg8.setRotationPoint(4.0f, i, -1.0f);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        float f = 0.7853982f;
        this.spiderLeg1.rotateAngleZ = -f;
        this.spiderLeg2.rotateAngleZ = f;
        this.spiderLeg3.rotateAngleZ = -f * 0.74f;
        this.spiderLeg4.rotateAngleZ = f * 0.74f;
        this.spiderLeg5.rotateAngleZ = -f * 0.74f;
        this.spiderLeg6.rotateAngleZ = f * 0.74f;
        this.spiderLeg7.rotateAngleZ = -f;
        this.spiderLeg8.rotateAngleZ = f;
        float f1 = -0.0f;
        float f2 = 0.3926991f;
        this.spiderLeg1.rotateAngleY = f2 * 2.0f + f1;
        this.spiderLeg2.rotateAngleY = -f2 * 2.0f - f1;
        this.spiderLeg3.rotateAngleY = f2 * 1.0f + f1;
        this.spiderLeg4.rotateAngleY = -f2 * 1.0f - f1;
        this.spiderLeg5.rotateAngleY = -f2 * 1.0f + f1;
        this.spiderLeg6.rotateAngleY = f2 * 1.0f - f1;
        this.spiderLeg7.rotateAngleY = -f2 * 2.0f + f1;
        this.spiderLeg8.rotateAngleY = f2 * 2.0f - f1;
        float f3 = -(MathHelper.cos((float)(this.ticks_2 * 0.6662f * 2.0f + 0.0f)) * 0.4f) * this.ticks_3;
        float f4 = -(MathHelper.cos((float)(this.ticks_2 * 0.6662f * 2.0f + (float)Math.PI)) * 0.4f) * this.ticks_3;
        float f5 = -(MathHelper.cos((float)(this.ticks_2 * 0.6662f * 2.0f + 1.5707964f)) * 0.4f) * this.ticks_3;
        float f6 = -(MathHelper.cos((float)(this.ticks_2 * 0.6662f * 2.0f + 4.712389f)) * 0.4f) * this.ticks_3;
        float f7 = Math.abs(MathHelper.sin((float)(this.ticks_2 * 0.6662f + 0.0f)) * 0.4f) * this.ticks_3;
        float f8 = Math.abs(MathHelper.sin((float)(this.ticks_2 * 0.6662f + (float)Math.PI)) * 0.4f) * this.ticks_3;
        float f9 = Math.abs(MathHelper.sin((float)(this.ticks_2 * 0.6662f + 1.5707964f)) * 0.4f) * this.ticks_3;
        float f10 = Math.abs(MathHelper.sin((float)(this.ticks_2 * 0.6662f + 4.712389f)) * 0.4f) * this.ticks_3;
        this.spiderLeg1.rotateAngleY += f3;
        this.spiderLeg2.rotateAngleY += -f3;
        this.spiderLeg3.rotateAngleY += f4;
        this.spiderLeg4.rotateAngleY += -f4;
        this.spiderLeg5.rotateAngleY += f5;
        this.spiderLeg6.rotateAngleY += -f5;
        this.spiderLeg7.rotateAngleY += f6;
        this.spiderLeg8.rotateAngleY += -f6;
        this.spiderLeg1.rotateAngleZ += f7;
        this.spiderLeg2.rotateAngleZ += -f7;
        this.spiderLeg3.rotateAngleZ += f8;
        this.spiderLeg4.rotateAngleZ += -f8;
        this.spiderLeg5.rotateAngleZ += f9;
        this.spiderLeg6.rotateAngleZ += -f9;
        this.spiderLeg7.rotateAngleZ += f10;
        this.spiderLeg8.rotateAngleZ += -f10;
        this.bindEntityTexture("spider/spider.png");
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.2f);
        }
        this.spiderLeg1.render(scale);
        this.spiderLeg2.render(scale);
        this.spiderLeg3.render(scale);
        this.spiderLeg4.render(scale);
        this.spiderLeg5.render(scale);
        this.spiderLeg6.render(scale);
        this.spiderLeg7.render(scale);
        this.spiderLeg8.render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

