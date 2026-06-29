package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=16, height=0.0, activatergb=false, displayname="NewYear2017", type=CosmeticType.LIMITED)
public class Cosmetic016
extends CosmeticModelRenderer {
    CosmeticModelRenderer bannerSlate;
    CosmeticModelRenderer bannerStand;
    CosmeticModelRenderer bannerTop;

    public Cosmetic016(ModelBase base) {
        super(base);
        this.bannerSlate = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 64);
        this.bannerSlate.addBox(-10.0f, 0.0f, -2.0f, 20, 40, 1, 0.0f);
        this.bannerStand = new CosmeticModelRenderer(base, 44, 0).setTextureSize(64, 64);
        this.bannerStand.addBox(-1.0f, -30.0f, -1.0f, 2, 42, 2, 0.0f);
        this.bannerTop = new CosmeticModelRenderer(base, 0, 42).setTextureSize(64, 64);
        this.bannerTop.addBox(-10.0f, -32.0f, -1.0f, 20, 2, 2, 0.0f);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        GL11.glEnable((int)2977);
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.23f, (float)0.0f);
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)0.02f, (float)0.0f);
        GL11.glScalef((float)0.6f, (float)0.44f, (float)0.7f);
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)0.3f, (float)-0.2f);
        GL11.glScalef((float)0.65f, (float)-0.65f, (float)0.65f);
        this.bindCosmeticTexture("banner_base.png");
        this.bannerSlate.rotationPointY = -32.0f;
        this.bannerSlate.render(0.0625f);
        this.bannerStand.render(0.0625f);
        this.bannerTop.render(0.0625f);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.6f, (float)0.44f, (float)0.7f);
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)0.3f, (float)-0.2f);
        GL11.glScalef((float)0.026f, (float)0.026f, (float)-0.026f);
        GL11.glTranslatef((float)-30.0f, (float)0.0f, (float)3.7f);
        GL11.glDepthMask((boolean)true);
        GL11.glPushMatrix();
        String text = "Happy";
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.02f);
        this.renderText(text, this.getStringWidth(text) / 2 + 1, -5, me.txb1.extras.cosmetics.oam.OamCosmeticManager.rainbow());
        GL11.glPopMatrix();
        text = "new";
        this.renderText(text, this.getStringWidth(text) / 2 + 12, 9, me.txb1.extras.cosmetics.oam.OamCosmeticManager.rainbow());
        text = "Year";
        this.renderText(text, this.getStringWidth(text) / 2 + 6, 22, me.txb1.extras.cosmetics.oam.OamCosmeticManager.rainbow());
        text = "2017";
        this.renderText(text, this.getStringWidth(text) / 2 + 6, 36, me.txb1.extras.cosmetics.oam.OamCosmeticManager.rainbow());
        GL11.glEnable((int)2896);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        super.render(scale);
    }
}

