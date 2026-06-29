package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=27, height=0.0, activatergb=true, displayname="Vex Wings", type=CosmeticType.DEFAULT)
public class Cosmetic027
extends CosmeticModelRenderer {
    CosmeticModelRenderer leftWing;
    CosmeticModelRenderer rightWing;

    public Cosmetic027(ModelBase model) {
        super(model);
        this.rightWing = new CosmeticModelRenderer(model, 0, 32).setTextureSize(64, 64);
        this.rightWing.addBox(-20.0f, 0.0f, 0.0f, 20, 12, 1);
        this.leftWing = new CosmeticModelRenderer(model, 0, 32).setTextureSize(64, 64);
        this.leftWing.mirror = true;
        this.leftWing.addBox(0.0f, 0.0f, 0.0f, 20, 12, 1);
    }

    @Override
    public void render(float scale) {
        if (this.isSneaking()) {
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)0.17f, (float)-0.1f);
        }
        this.renderRGB("color", 100, 100, 100);
        this.bindCosmeticTexture("vexwings.png");
        float size = this.cosmetic.getFloat("scale");
        if (size < 0.6f || size > 1.1f) {
            size = 0.8f;
        }
        GL11.glScalef((float)size, (float)size, (float)size);
        this.rightWing.rotationPointZ = 2.0f;
        this.leftWing.rotationPointZ = 2.0f;
        this.rightWing.rotationPointY = 1.0f;
        this.leftWing.rotationPointY = 1.0f;
        this.rightWing.rotateAngleY = 0.47123894f + MathHelper.cos((float)(this.ticks_4 * 0.8f)) * (float)Math.PI * 0.05f;
        this.leftWing.rotateAngleY = -this.rightWing.rotateAngleY;
        this.leftWing.rotateAngleZ = -0.47123894f;
        this.leftWing.rotateAngleX = 0.47123894f;
        this.rightWing.rotateAngleX = 0.47123894f;
        this.rightWing.rotateAngleZ = 0.47123894f;
        this.rightWing.render(scale);
        this.leftWing.render(scale);
        super.render(scale);
    }
}

