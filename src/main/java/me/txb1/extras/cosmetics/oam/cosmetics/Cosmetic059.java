package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticManager;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=59, height=0.0, activatergb=true, displayname="Mini Me", type=CosmeticType.DEFAULT)
public class Cosmetic059
extends CosmeticModelRenderer {
    public CosmeticModelRenderer bipedHead;
    public CosmeticModelRenderer bipedHeadwear;
    CosmeticModelRenderer wing;
    CosmeticModelRenderer wingTip;
    CosmeticModelRenderer wing_bone;
    CosmeticModelRenderer wing_skin;
    CosmeticModelRenderer wingTip_bone;
    CosmeticModelRenderer wingTip_skin;

    public Cosmetic059(ModelBase base) {
        super(base);
        this.bipedHead = new CosmeticModelRenderer(base, 0, 0);
        this.bipedHead.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f);
        this.bipedHead.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedHeadwear = new CosmeticModelRenderer(base, 32, 0);
        this.bipedHeadwear.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.5f);
        this.bipedHeadwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.wing_skin = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wing_bone = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wingTip_skin = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wingTip_bone = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wing_skin.setTextureOffset(-56, 88);
        this.wingTip_skin.setTextureOffset(-56, 144);
        this.wing_bone.setTextureOffset(112, 88);
        this.wingTip_bone.setTextureOffset(112, 136);
        this.wing = new CosmeticModelRenderer(base);
        this.wing.setRotationPoint(-27.0f, 5.0f, 2.0f);
        this.wing_bone.addBox(-56.0f, -4.0f, -4.0f, 56, 8, 8);
        this.wing.addChild(this.wing_bone);
        this.wing_skin.addBox(-56.0f, 0.0f, 2.0f, 56, 0, 56);
        this.wing.addChild(this.wing_skin);
        this.wingTip = new CosmeticModelRenderer(base);
        this.wingTip.setRotationPoint(-56.0f, 0.0f, 0.0f);
        this.wingTip_bone.addBox(-56.0f, -2.0f, -2.0f, 56, 4, 4);
        this.wingTip.addChild(this.wingTip_bone);
        this.wingTip_skin.addBox(-56.0f, 0.0f, 2.0f, 56, 0, 56);
        this.wingTip.addChild(this.wingTip_skin);
        this.wing.addChild(this.wingTip);
    }

    @Override
    public void render(float scale) {
        float pos;
        this.bindSkinTexture();
        float swing = 0.125f - (float)Math.cos((double)this.ticks_4 / 9.0) * 0.2f;
        float f = pos = this.cosmetic.getString("pos").equals("null") ? 1.3f : -1.3f;
        if (CosmeticManager.hasEntityCosmetic(this.entityIn, 11)) {
            this.renderMultiColor();
        }
        GL11.glTranslatef((float)pos, (float)(0.17f + swing), (float)0.0f);
        GL11.glScalef((float)0.67f, (float)0.67f, (float)0.67f);
        this.bipedHead.render(scale);
        this.bipedHeadwear.render(scale);
        this.renderRGB("color", 100, 100, 100);
        this.bindEntityTexture("enderdragon/dragon.png");
        float size = 0.15f;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)-0.3f, (float)0.0f);
        GL11.glScalef((float)size, (float)size, (float)size);
        for (int var23 = 0; var23 < 2; ++var23) {
            GL11.glRotatef((float)30.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glEnable((int)2884);
            float var21 = this.ticks_4 / 9.0f;
            this.wing.rotateAngleX = 0.125f - (float)Math.cos(var21) * 0.2f;
            this.wing.rotateAngleY = 0.25f;
            this.wing.rotateAngleZ = (float)(Math.sin(var21) + 0.125) * 0.5f;
            this.wingTip.rotateAngleZ = -((float)(Math.sin(var21 + 2.0f) + 0.5)) * 0.7f;
            this.wing.render(scale);
            GL11.glScalef((float)-1.0f, (float)1.0f, (float)1.0f);
            if (var23 != 0) continue;
            GL11.glCullFace((int)1028);
            GL11.glRotatef((float)30.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        }
        GL11.glCullFace((int)1029);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)2896);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

