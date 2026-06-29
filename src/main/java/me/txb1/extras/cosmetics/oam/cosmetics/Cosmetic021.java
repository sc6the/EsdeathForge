package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=21, height=0.0, activatergb=true, displayname="Gudoudama", type=CosmeticType.DEFAULT)
public class Cosmetic021
extends CosmeticModelRenderer {
    CosmeticModelRenderer gudoudama;

    public Cosmetic021(ModelBase base) {
        super(base);
        this.gudoudama = new CosmeticModelRenderer(base, 0, 0);
        this.gudoudama.addBox(0.0f, 0.0f, -6.0f, -6, 6, 6);
        this.gudoudama.addBox(1.0f, 1.0f, -5.0f, -8, 4, 4);
        this.gudoudama.addBox(-1.0f, -1.0f, -5.0f, -4, 8, 4);
        this.gudoudama.addBox(-1.0f, 1.0f, -7.0f, -4, 4, 8);
    }

    @Override
    public void render(float glScalef) {
        GL11.glPushMatrix();
        float i = (float)Math.cos(this.ticks_4 / 10.0f) / 5.0f;
        this.renderRGB("color", 0, 0, 0);
        this.bindCosmeticTexture("resources.png");
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.1f);
        }
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)0.2f, (float)(-1.8f + i / 1.5f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)-0.6f, (float)(-1.1f + i / 2.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)1.0f, (float)(-1.1f + i / 3.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)1.2f, (float)(-0.2f + i / 4.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)-0.8f, (float)(-0.2f + i / 3.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)-0.7f, (float)(0.7f + i / 2.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)1.1f, (float)(0.7f + i / 4.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)0.7f, (float)(1.5f + i / 2.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.4f, (float)0.4f, (float)0.4f);
        GL11.glTranslatef((float)-0.3f, (float)(1.5f + i / 3.0f), (float)1.2f);
        this.gudoudama.render(glScalef);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        super.render(glScalef);
    }
}

