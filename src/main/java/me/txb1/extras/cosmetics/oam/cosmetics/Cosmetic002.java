package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=2, height=0.0, activatergb=true, displayname="Dragon Wings", type=CosmeticType.DEFAULT)
public class Cosmetic002
extends CosmeticModelRenderer {
    CosmeticModelRenderer wing;
    CosmeticModelRenderer wingTip;
    CosmeticModelRenderer wing_bone;
    CosmeticModelRenderer wing_skin;
    CosmeticModelRenderer wingTip_bone;
    CosmeticModelRenderer wingTip_skin;
    HashMap<UUID, Float> actionMap = new HashMap();

    public Cosmetic002(ModelBase base) {
        super(base);
        this.wing_skin = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wing_bone = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wingTip_skin = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wingTip_bone = new CosmeticModelRenderer(base).setTextureSize(256, 256);
        this.wing_skin.setTextureOffset(-56, 88);
        this.wingTip_skin.setTextureOffset(-56, 144);
        this.wing_bone.setTextureOffset(112, 88);
        this.wingTip_bone.setTextureOffset(112, 136);
        this.wing = new CosmeticModelRenderer(base);
        this.wing.setRotationPoint(-12.0f, 5.0f, 2.0f);
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
        float size;
        if (!this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            this.actionMap.put(this.entityIn.getUniqueID(), Float.valueOf(0.0f));
        }
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        this.bindEntityTexture("enderdragon/dragon.png");
        if (this.isSneaking()) {
            GL11.glRotatef((float)10.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)0.17f, (float)0.0f);
        }
        GL11.glRotatef((float)-52.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)-0.05f, (float)0.1f);
        this.renderRGB("color", 100, 100, 100);
        float f = size = this.cosmetic.getInteger("scale") == 0 ? 0.15f : 0.01f * (float)this.cosmetic.getInteger("scale");
        if (size < 0.098f || size > 0.2f) {
            size = 0.15f;
        }
        GL11.glScalef((float)size, (float)size, (float)size);
        for (int var23 = 0; var23 < 2; ++var23) {
            GL11.glRotatef((float)30.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glEnable((int)2884);
            float var21 = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
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

    @Override
    public void performAnimation() {
        if (this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            float value = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
            value = this.entityIn.prevDistanceWalkedModified != this.entityIn.distanceWalkedModified ? (!this.entityIn.onGround ? (value += 0.016f) : (value += 0.01f)) : (value += 0.004f);
            if (this.entityIn.isSprinting()) {
                value += 0.01f;
            }
            this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(value));
        }
        super.performAnimation();
    }
}

