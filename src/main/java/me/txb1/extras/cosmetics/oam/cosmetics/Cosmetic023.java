package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=23, height=0.0, activatergb=true, displayname="Konoha Headband", type=CosmeticType.DEFAULT)
public class Cosmetic023
extends CosmeticModelRenderer {
    CosmeticModelRenderer headband;
    CosmeticModelRenderer headbandOL;

    public Cosmetic023(ModelBase base) {
        super(base);
        this.headband = new CosmeticModelRenderer(base, 0, 0);
        this.headbandOL = new CosmeticModelRenderer(base, 0, 0);
        this.headband.addBox(3.501f, -7.0f, -4.501f, -7, 2, 1);
        this.headband.addBox(4.501f, -7.0f, -4.501f, -1, 2, 9);
        this.headband.addBox(3.501f, -7.0f, 3.501f, -1, 2, 1);
        this.headband.addBox(-2.501f, -7.0f, 3.501f, -1, 2, 1);
        this.headband.addBox(-1.501f, -6.5f, 3.501f, 3, 1, 1);
        this.headband.addBox(-2.501f, -6.8f, 3.501f, 1, 1, 1);
        this.headband.addBox(-2.501f, -6.2f, 3.501f, 1, 1, 1);
        this.headband.addBox(1.501f, -6.8f, 3.501f, 1, 1, 1);
        this.headband.addBox(1.501f, -6.2f, 3.501f, 1, 1, 1);
        this.headband.addBox(-3.501f, -7.0f, -4.501f, -1, 2, 9);
        this.headband.addBox(-0.501f, -6.5f, 3.001f, 1, 1, 2);
        this.headband.addBox(-1.001f, -6.0f, 4.501f, 1, 1, 1);
        this.headband.addBox(-1.501f, -5.0f, 5.001f, 1, 2, 1);
        this.headband.addBox(-2.001f, -3.0f, 5.501f, 1, 2, 1);
        this.headband.addBox(0.001f, -6.0f, 4.501f, 1, 1, 1);
        this.headband.addBox(0.501f, -5.0f, 5.001f, 1, 1, 1);
        this.headband.addBox(1.001f, -4.0f, 5.501f, 1, 2, 1);
        this.headband.addBox(1.501f, -2.0f, 5.501f, 1, 2, 1);
        this.headbandOL.addBox(-12.0f, -14.0f, -1.0f, 24, 12, 2, 0.0f);
    }

    @Override
    public void render(float scale) {
        // The cloth band is built from negative-dimension boxes (e.g. addBox(..,-7,2,1)), which invert
        // the quad winding. With the layer's forced GL_CULL_FACE(BACK) their outward faces get culled,
        // so the side straps render see-through/washed out. Draw both faces for this cosmetic. (The
        // metal plate is a normal box, so rendering its back faces too is harmless.) The layer restores
        // the cull state after us; we also restore it here so nothing leaks within this render.
        GL11.glDisable((int)2884); // GL_CULL_FACE
        GL11.glPushMatrix();
        GL11.glDisable((int)2896);
        this.renderRGB("color", 0, 0, 60);
        this.bindCosmeticTexture("resources.png");
        this.setHeadRotations();
        if (this.isHelmetEquipped()) {
            GL11.glScalef((float)1.12f, (float)1.0f, (float)1.12f);
        }
        this.headband.render(scale);
        GL11.glEnable((int)2896);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.setHeadRotations();
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        if (this.isHelmetEquipped()) {
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.03099f);
        }
        GL11.glTranslatef((float)0.076f, (float)-0.204f, (float)-0.266f);
        GL11.glScalef((float)0.35f, (float)0.29f, (float)0.29f);
        if (this.cosmetic.getString("style").equalsIgnoreCase("renegade")) {
            this.bindCosmeticTexture("konoha2.png");
        } else {
            this.bindCosmeticTexture("konoha1.png");
        }
        this.headbandOL.render(scale);
        GL11.glPopMatrix();
        GL11.glEnable((int)2884); // restore GL_CULL_FACE
        super.render(scale);
    }
}

