package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=43, height=0.0, activatergb=true, displayname="Ender Dragon", type=CosmeticType.DEFAULT)
public class Cosmetic043
extends CosmeticModelRenderer {
    private CosmeticModelRenderer head;
    private CosmeticModelRenderer spine;
    private CosmeticModelRenderer jaw;
    private CosmeticModelRenderer body;
    private CosmeticModelRenderer rearLeg;
    private CosmeticModelRenderer frontLeg;
    private CosmeticModelRenderer rearLegTip;
    private CosmeticModelRenderer frontLegTip;
    private CosmeticModelRenderer rearFoot;
    private CosmeticModelRenderer frontFoot;
    private CosmeticModelRenderer wing;
    private CosmeticModelRenderer wingTip;
    private float partialTicks;
    float f = 0.0f;
    public double[][] ringBuffer = new double[64][3];
    public int ringBufferIndex = -1;

    public Cosmetic043(ModelBase base) {
        super(base);
        float f = -16.0f;
        this.head = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.head.setTextureOffset(176, 44);
        this.head.addBox(-6.0f, -1.0f, -8.0f + f, 12, 5, 16);
        this.head.setTextureOffset(112, 30);
        this.head.addBox(-8.0f, -8.0f, 6.0f + f, 16, 16, 16);
        this.head.mirror = true;
        this.head.setTextureOffset(0, 0);
        this.head.addBox(-5.0f, -12.0f, 12.0f + f, 2, 4, 6);
        this.head.setTextureOffset(112, 0);
        this.head.addBox(-5.0f, -3.0f, -6.0f + f, 2, 2, 4);
        this.head.mirror = false;
        this.head.setTextureOffset(0, 0);
        this.head.addBox(3.0f, -12.0f, 12.0f + f, 2, 4, 6);
        this.head.setTextureOffset(112, 0);
        this.head.addBox(3.0f, -3.0f, -6.0f + f, 2, 2, 4);
        this.jaw = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.jaw.setRotationPoint(0.0f, 4.0f, 8.0f + f);
        this.jaw.setTextureOffset(176, 65);
        this.jaw.addBox(-6.0f, 0.0f, -16.0f, 12, 4, 16);
        this.head.addChild(this.jaw);
        this.spine = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.spine.setTextureOffset(192, 104);
        this.spine.addBox(-5.0f, -5.0f, -5.0f, 10, 10, 10);
        this.spine.setTextureOffset(48, 0);
        this.spine.addBox(-1.0f, -9.0f, -3.0f, 2, 4, 6);
        this.body = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.body.setRotationPoint(0.0f, 4.0f, 8.0f);
        this.body.setTextureOffset(0, 0);
        this.body.addBox(-12.0f, 0.0f, -16.0f, 24, 24, 64);
        this.body.setTextureOffset(220, 53);
        this.body.addBox(-1.0f, -6.0f, -10.0f, 2, 6, 12);
        this.body.addBox(-1.0f, -6.0f, 10.0f, 2, 6, 12);
        this.body.addBox(-1.0f, -6.0f, 30.0f, 2, 6, 12);
        this.wing = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wing.setRotationPoint(-12.0f, 5.0f, 2.0f);
        this.wing.setTextureOffset(112, 88);
        this.wing.addBox(-56.0f, -4.0f, -4.0f, 56, 8, 8);
        this.wing.setTextureOffset(-56, 88);
        this.wing.addBox(-56.0f, 0.0f, 2.0f, 56, 0, 56);
        this.wingTip = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wingTip.setRotationPoint(-56.0f, 0.0f, 0.0f);
        this.wingTip.setTextureOffset(112, 136);
        this.wingTip.addBox(-56.0f, -2.0f, -2.0f, 56, 4, 4);
        this.wingTip.setTextureOffset(-56, 144);
        this.wingTip.addBox(-56.0f, 0.0f, 2.0f, 56, 0, 56);
        this.wing.addChild(this.wingTip);
        this.frontLeg = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.frontLeg.setRotationPoint(-12.0f, 20.0f, 2.0f);
        this.frontLeg.setTextureOffset(112, 104);
        this.frontLeg.addBox(-4.0f, -4.0f, -4.0f, 8, 24, 8);
        this.frontLegTip = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.frontLegTip.setRotationPoint(0.0f, 20.0f, -1.0f);
        this.frontLegTip.setTextureOffset(226, 138);
        this.frontLegTip.addBox(-3.0f, -1.0f, -3.0f, 6, 24, 6);
        this.frontLeg.addChild(this.frontLegTip);
        this.frontFoot = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.frontFoot.setRotationPoint(0.0f, 23.0f, 0.0f);
        this.frontFoot.setTextureOffset(144, 104);
        this.frontFoot.addBox(-4.0f, 0.0f, -12.0f, 8, 4, 16);
        this.frontLegTip.addChild(this.frontFoot);
        this.rearLeg = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.rearLeg.setRotationPoint(-16.0f, 16.0f, 42.0f);
        this.rearLeg.addBox(-8.0f, -4.0f, -8.0f, 16, 32, 16);
        this.rearLegTip = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.rearLegTip.setRotationPoint(0.0f, 32.0f, -4.0f);
        this.rearLegTip.setTextureOffset(196, 0);
        this.rearLegTip.addBox(-6.0f, -2.0f, 0.0f, 12, 32, 12);
        this.rearLeg.addChild(this.rearLegTip);
        this.rearFoot = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.rearFoot.setRotationPoint(0.0f, 31.0f, 4.0f);
        this.rearFoot.setTextureOffset(112, 0);
        this.rearFoot.addBox(-9.0f, 0.0f, -20.0f, 18, 6, 24);
        this.rearLegTip.addChild(this.rearFoot);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/entity/enderdragon/dragon.png"));
        this.renderRGB("color", 100, 100, 100);
        GL11.glScalef((float)0.09f, (float)0.09f, (float)0.09f);
        float swing = (float)Math.cos(this.ticks_4 / 5.0f);
        float swing1 = (float)Math.cos(this.ticks_2 / 4.0f) * 2.0f;
        float pos = this.cosmetic.getString("pos").equals("null") ? 14.0f : -14.0f;
        GL11.glTranslatef((float)(swing1 + pos), (float)swing, (float)-2.0f);
        EntityPlayer entitydragon = (EntityPlayer)this.entityIn;
        this.f = !this.entityIn.isSprinting() ? (this.f += 0.001f) : (this.f += 0.004f);
        this.jaw.rotateAngleX = (float)(Math.sin(this.f * (float)Math.PI * 2.0f) + 1.0) * 0.2f;
        float f1 = (float)(Math.sin(this.f * (float)Math.PI * 2.0f - 1.0f) + 1.0);
        f1 = (f1 * f1 * 1.0f + f1 * 2.0f) * 0.05f;
        GL11.glTranslatef((float)0.0f, (float)(f1 - 2.0f), (float)-3.0f);
        GL11.glRotatef((float)(f1 * 2.0f), (float)1.0f, (float)0.0f, (float)0.0f);
        float f2 = -30.0f;
        float f4 = 0.0f;
        float f5 = 1.5f;
        double[] adouble = this.getMovementOffsets(6, this.partialTicks);
        float f6 = this.updateRotations(this.getMovementOffsets(5, this.partialTicks)[0] - this.getMovementOffsets(10, this.partialTicks)[0]);
        float f7 = this.updateRotations(this.getMovementOffsets(5, this.partialTicks)[0] + (double)(f6 / 2.0f));
        f2 += 2.0f;
        float f8 = this.f * (float)Math.PI * 2.0f;
        f2 = 20.0f;
        float f3 = -12.0f;
        for (int i = 0; i < 5; ++i) {
            double[] adouble1 = this.getMovementOffsets(5 - i, this.partialTicks);
            float f9 = (float)Math.cos((float)i * 0.45f + f8) * 0.15f;
            this.spine.rotateAngleY = this.updateRotations(adouble1[0] - adouble[0]) * (float)Math.PI / 180.0f * f5;
            this.spine.rotateAngleX = f9 + (float)(adouble1[1] - adouble[1]) * (float)Math.PI / 180.0f * f5 * 5.0f;
            this.spine.rotateAngleZ = -this.updateRotations(adouble1[0] - (double)f7) * (float)Math.PI / 180.0f * f5;
            this.spine.rotationPointY = f2;
            this.spine.rotationPointZ = f3;
            this.spine.rotationPointX = f4;
            f2 = (float)((double)f2 + Math.sin(this.spine.rotateAngleX) * 10.0);
            f3 = (float)((double)f3 - Math.cos(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0);
            f4 = (float)((double)f4 - Math.sin(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0);
            this.spine.render(scale);
        }
        this.head.rotationPointY = f2;
        this.head.rotationPointZ = f3;
        this.head.rotationPointX = f4;
        double[] adouble2 = this.getMovementOffsets(0, this.partialTicks);
        this.head.rotateAngleY = this.updateRotations(adouble2[0] - adouble[0]) * (float)Math.PI / 180.0f * 1.0f;
        this.head.rotateAngleZ = -this.updateRotations(adouble2[0] - (double)f7) * (float)Math.PI / 180.0f * 1.0f;
        this.head.render(scale);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-f6 * f5 * 1.0f), (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glTranslatef((float)0.0f, (float)-1.0f, (float)0.0f);
        this.body.rotateAngleZ = 0.0f;
        this.body.render(scale);
        for (int j = 0; j < 2; ++j) {
            GL11.glEnable((int)2884);
            float f11 = this.f * (float)Math.PI * 2.0f;
            this.wing.rotateAngleX = 0.125f - (float)Math.cos(f11) * 0.2f;
            this.wing.rotateAngleY = 0.25f;
            this.wing.rotateAngleZ = (float)(Math.sin(f11) + 0.125) * 0.8f;
            this.wingTip.rotateAngleZ = -((float)(Math.sin(f11 + 2.0f) + 0.5)) * 0.75f;
            this.rearLeg.rotateAngleX = 1.0f + f1 * 0.1f;
            this.rearLegTip.rotateAngleX = 0.5f + f1 * 0.1f;
            this.rearFoot.rotateAngleX = 0.75f + f1 * 0.1f;
            this.frontLeg.rotateAngleX = 1.3f + f1 * 0.1f;
            this.frontLegTip.rotateAngleX = -0.5f - f1 * 0.1f;
            this.frontFoot.rotateAngleX = 0.75f + f1 * 0.1f;
            this.wing.render(scale);
            this.frontLeg.render(scale);
            this.rearLeg.render(scale);
            GL11.glScalef((float)-1.0f, (float)1.0f, (float)1.0f);
            if (j != 0) continue;
            GL11.glCullFace((int)1028);
        }
        GL11.glPopMatrix();
        GL11.glCullFace((int)1029);
        GL11.glDisable((int)2884);
        float f10 = -((float)Math.sin(this.f * (float)Math.PI * 2.0f)) * 0.0f;
        f8 = this.f * (float)Math.PI * 2.0f;
        f2 = 10.0f;
        f3 = 60.0f;
        f4 = 0.0f;
        adouble = this.getMovementOffsets(11, this.partialTicks);
        for (int k = 0; k < 12; ++k) {
            adouble2 = this.getMovementOffsets(12 + k, this.partialTicks);
            f10 = (float)((double)f10 + Math.sin((float)k * 0.45f + f8) * (double)0.05f);
            this.spine.rotateAngleY = (this.updateRotations(adouble2[0] - adouble[0]) * f5 + 180.0f) * (float)Math.PI / 180.0f;
            this.spine.rotateAngleX = f10 + (float)(adouble2[1] - adouble[1]) * (float)Math.PI / 180.0f * f5 * 5.0f;
            this.spine.rotateAngleZ = this.updateRotations(adouble2[0] - (double)f7) * (float)Math.PI / 180.0f * f5;
            this.spine.rotationPointY = f2;
            this.spine.rotationPointZ = f3;
            this.spine.rotationPointX = f4;
            f2 = (float)((double)f2 + Math.sin(this.spine.rotateAngleX) * 10.0);
            f3 = (float)((double)f3 - Math.cos(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0);
            f4 = (float)((double)f4 - Math.sin(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0);
            this.spine.render(scale);
        }
        GL11.glEnable((int)2896);
        GL11.glPopMatrix();
        super.render(scale);
    }

    private float updateRotations(double p_78214_1_) {
        while (p_78214_1_ >= 180.0) {
            p_78214_1_ -= 360.0;
        }
        while (p_78214_1_ < -180.0) {
            p_78214_1_ += 360.0;
        }
        return (float)p_78214_1_;
    }

    public double[] getMovementOffsets(int p_70974_1_, float p_70974_2_) {
        EntityPlayer entity = (EntityPlayer)this.entityIn;
        if (entity.getHealth() <= 0.0f) {
            p_70974_2_ = 0.0f;
        }
        p_70974_2_ = 1.0f - p_70974_2_;
        int i = this.ringBufferIndex - p_70974_1_ * 1 & 0x3F;
        int j = this.ringBufferIndex - p_70974_1_ * 1 - 1 & 0x3F;
        double[] adouble = new double[3];
        double d0 = this.ringBuffer[i][0];
        double d1 = MathHelper.wrapAngleTo180_double((double)(this.ringBuffer[j][0] - d0));
        adouble[0] = d0 + d1 * (double)p_70974_2_;
        d0 = this.ringBuffer[i][1];
        d1 = this.ringBuffer[j][1] - d0;
        adouble[1] = d0 + d1 * (double)p_70974_2_;
        adouble[2] = this.ringBuffer[i][2] + (this.ringBuffer[j][2] - this.ringBuffer[i][2]) * (double)p_70974_2_;
        return adouble;
    }
}

