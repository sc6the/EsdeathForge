package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=20, height=0.0, activatergb=false, displayname="Twitch", type=CosmeticType.EXCLUSIVE)
public class Cosmetic020
extends CosmeticModelRenderer {
    CosmeticModelRenderer socialmediasign;

    public Cosmetic020(ModelBase base) {
        super(base);
        this.socialmediasign = new CosmeticModelRenderer(base, 0, 0);
        this.socialmediasign.addBox(-12.0f, -14.0f, -1.0f, 24, 12, 2, 0.0f);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("twitch-sign.png");
        GL11.glPushMatrix();
        if (this.isSneaking()) {
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.0125f);
        }
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)0.015f, (float)1.6f, (float)-0.27f);
        this.socialmediasign.render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

