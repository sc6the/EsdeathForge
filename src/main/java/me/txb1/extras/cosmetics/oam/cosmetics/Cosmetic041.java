package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=41, height=0.0, activatergb=false, displayname="Pikachu Tail", type=CosmeticType.DEFAULT)
public class Cosmetic041
extends CosmeticModelRenderer {
    CosmeticModelRenderer Shape1;
    CosmeticModelRenderer Shape2;
    CosmeticModelRenderer Shape3;
    CosmeticModelRenderer Shape4;
    CosmeticModelRenderer Shape5;

    public Cosmetic041(ModelBase base) {
        super(base);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.Shape1.addBox(0.0f, 0.0f, 0.0f, 1, 2, 7);
        this.Shape1.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.setRotation(this.Shape1, 0.0f, 0.0f, 0.0f);
        this.Shape2 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.Shape2.addBox(0.0f, 0.0f, 0.0f, 1, 8, 3);
        this.Shape2.setRotationPoint(0.0f, 2.0f, 7.0f);
        this.setRotation(this.Shape2, 2.96706f, 0.0f, 0.0f);
        this.Shape3 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.Shape3.addBox(0.0f, 0.0f, 0.0f, 1, 4, 9);
        this.Shape3.setRotationPoint(0.0f, -6.4f, 5.6f);
        this.setRotation(this.Shape3, 0.1570796f, 0.0f, 0.0f);
        this.Shape4 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.Shape4.addBox(0.0f, 0.0f, 0.0f, 1, 9, 5);
        this.Shape4.setRotationPoint(0.0f, -3.8f, 15.1f);
        this.setRotation(this.Shape4, 2.86234f, 0.0f, 0.0f);
        this.Shape5 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.Shape5.addBox(0.0f, 0.0f, 0.0f, 1, 5, 10);
        this.Shape5.setRotationPoint(0.0f, -13.9f, 13.0f);
        this.setRotation(this.Shape5, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(float scale) {
        float swing = (float)Math.cos(this.ticks_2) * 6.0f;
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.23f, (float)0.0f);
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        this.bindCosmeticTexture("pikachu_tail.png");
        GL11.glTranslatef((float)-0.024f, (float)0.6f, (float)0.12f);
        GL11.glScalef((float)0.55f, (float)0.55f, (float)0.55f);
        GL11.glRotatef((float)swing, (float)0.0f, (float)0.0f, (float)1.0f);
        this.Shape1.render(scale);
        this.Shape2.render(scale);
        this.Shape3.render(scale);
        this.Shape4.render(scale);
        this.Shape5.render(scale);
        super.render(scale);
    }
}

