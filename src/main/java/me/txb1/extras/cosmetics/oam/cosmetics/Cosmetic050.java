package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=50, height=0.0, activatergb=false, displayname="Devil Tail", type=CosmeticType.DEFAULT)
public class Cosmetic050
extends CosmeticModelRenderer {
    CosmeticModelRenderer Shape8;
    CosmeticModelRenderer Shape1;
    CosmeticModelRenderer Shape2;
    CosmeticModelRenderer Shape3;
    float ticks;

    public Cosmetic050(ModelBase base) {
        super(base);
        this.Shape8 = new CosmeticModelRenderer(base, 25, 35).setTextureSize(512, 256);
        this.Shape8.addBox(0.0f, 0.0f, 0.0f, 8, 4, 4);
        this.Shape8.setRotationPoint(-7.0f, -0.3f, -0.5f);
        this.setRotation(this.Shape8, 0.0f, 0.0f, -0.1570796f);
        this.Shape1 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(512, 256);
        this.Shape1.addBox(5.1f, 4.2f, 0.5f, 11, 3, 3);
        this.setRotation(this.Shape1, 0.0f, 0.0f, -0.5061455f);
        this.Shape8.addChild(this.Shape1);
        this.Shape2 = new CosmeticModelRenderer(base, 41, 0).setTextureSize(512, 256);
        this.Shape2.addBox(12.0f, 10.7f, 1.0f, 7, 2, 2);
        this.setRotation(this.Shape2, 0.0f, 0.0f, -0.4075712f);
        this.Shape1.addChild(this.Shape2);
        this.Shape3 = new CosmeticModelRenderer(base, 74, 0).setTextureSize(512, 256);
        this.Shape3.addBox(14.0f, 16.5f, 1.5f, 7, 1, 1);
        this.setRotation(this.Shape3, 0.0f, 0.0f, -0.312173f);
        this.Shape2.addChild(this.Shape3);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("deviltail.png");
        GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.2f, (float)0.0f);
            GL11.glRotatef((float)-24.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        }
        GL11.glTranslatef((float)0.3f, (float)0.57f, (float)-0.052f);
        GL11.glScalef((float)0.55f, (float)0.55f, (float)0.55f);
        this.Shape8.rotateAngleY = (float)Math.cos(this.ticks * 10.0f) * 0.05f;
        this.Shape1.rotateAngleY = (float)Math.cos(this.ticks * 10.0f) * 0.03f;
        this.Shape2.rotateAngleY = (float)Math.cos(this.ticks * 10.0f) * 0.02f;
        this.Shape3.rotateAngleY = (float)Math.cos(this.ticks * 10.0f) * 0.02f;
        this.Shape8.render(scale);
        super.render(scale);
    }

    @Override
    public void performAnimation() {
        if (this.entityIn.isSneaking()) {
            this.ticks += 5.0E-4f;
        } else {
            this.ticks += 0.001f;
            if (this.entityIn.isSprinting()) {
                this.ticks += 0.002f;
            }
            if (!this.entityIn.onGround) {
                this.ticks += 0.002f;
            }
        }
        super.performAnimation();
    }
}

