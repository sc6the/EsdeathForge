package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=28, height=0.4, activatergb=false, displayname="Llama", type=CosmeticType.EXCLUSIVE)
public class Cosmetic028
extends CosmeticModelRenderer {
    public CosmeticModelRenderer head;
    public CosmeticModelRenderer body;
    public CosmeticModelRenderer leg1;
    public CosmeticModelRenderer leg2;
    public CosmeticModelRenderer leg3;
    public CosmeticModelRenderer leg4;
    protected float childYOffset = 8.0f;
    protected float childZOffset = 4.0f;
    private final CosmeticModelRenderer chest1;
    private final CosmeticModelRenderer chest2;

    public Cosmetic028(ModelBase base) {
        super(base);
        this.head = new CosmeticModelRenderer(base, 0, 0).setTextureSize(128, 64);
        this.head.addBox(-4.0f, -4.0f, -8.0f, 8, 8, 8, 0.7f);
        this.head.setRotationPoint(0.0f, 18.0f, -6.0f);
        this.body = new CosmeticModelRenderer(base, 28, 8).setTextureSize(128, 64);
        this.body.addBox(-5.0f, -10.0f, -7.0f, 10, 16, 8, 0.7f);
        this.body.setRotationPoint(0.0f, 17.0f, 2.0f);
        this.leg1 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(128, 64);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4, 0, 4, 0.7f);
        this.leg1.setRotationPoint(-3.0f, 24.0f, 7.0f);
        this.leg2 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(128, 64);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4, 0, 4, 0.7f);
        this.leg2.setRotationPoint(3.0f, 24.0f, 7.0f);
        this.leg3 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(128, 64);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4, 0, 4, 0.7f);
        this.leg3.setRotationPoint(-3.0f, 24.0f, -5.0f);
        this.leg4 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(128, 64);
        this.leg4.addBox(-2.0f, 0.0f, -2.0f, 4, 0, 4, 0.7f);
        this.leg4.setRotationPoint(3.0f, 24.0f, -5.0f);
        this.head = new CosmeticModelRenderer(base, 0, 0).setTextureSize(128, 64);
        this.head.addBox(-2.0f, -14.0f, -10.0f, 4, 4, 9, 0.0f);
        this.head.setRotationPoint(0.0f, 7.0f, -6.0f);
        this.head.setTextureOffset(0, 14).addBox(-4.0f, -16.0f, -6.0f, 8, 18, 6, 0.0f);
        this.head.setTextureOffset(17, 0).addBox(-4.0f, -19.0f, -4.0f, 3, 3, 2, 0.0f);
        this.head.setTextureOffset(17, 0).addBox(1.0f, -19.0f, -4.0f, 3, 3, 2, 0.0f);
        this.body = new CosmeticModelRenderer(base, 29, 0).setTextureSize(128, 64);
        this.body.addBox(-6.0f, -10.0f, -7.0f, 12, 18, 10, 0.0f);
        this.body.setRotationPoint(0.0f, 5.0f, 2.0f);
        this.chest1 = new CosmeticModelRenderer(base, 45, 28).setTextureSize(128, 64);
        this.chest1.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3, 0.0f);
        this.chest1.setRotationPoint(-8.5f, 3.0f, 3.0f);
        this.chest1.rotateAngleY = 1.5707964f;
        this.chest2 = new CosmeticModelRenderer(base, 45, 41).setTextureSize(128, 64);
        this.chest2.addBox(-3.0f, 0.0f, 0.0f, 8, 8, 3, 0.0f);
        this.chest2.setRotationPoint(5.5f, 3.0f, 3.0f);
        this.chest2.rotateAngleY = 1.5707964f;
        int i = 4;
        int j = 14;
        this.leg1 = new CosmeticModelRenderer(base, 29, 29).setTextureSize(128, 64);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, 0.0f);
        this.leg1.setRotationPoint(-2.5f, 10.0f, 6.0f);
        this.leg2 = new CosmeticModelRenderer(base, 29, 29).setTextureSize(128, 64);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, 0.0f);
        this.leg2.setRotationPoint(2.5f, 10.0f, 6.0f);
        this.leg3 = new CosmeticModelRenderer(base, 29, 29).setTextureSize(128, 64);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, 0.0f);
        this.leg3.setRotationPoint(-2.5f, 10.0f, -4.0f);
        this.leg4 = new CosmeticModelRenderer(base, 29, 29).setTextureSize(128, 64);
        this.leg4.addBox(-2.0f, 0.0f, -2.0f, 4, 14, 4, 0.0f);
        this.leg4.setRotationPoint(2.5f, 10.0f, -4.0f);
        this.leg1.rotationPointX -= 1.0f;
        this.leg2.rotationPointX += 1.0f;
        this.leg1.rotationPointZ += 0.0f;
        this.leg2.rotationPointZ += 0.0f;
        this.leg3.rotationPointX -= 1.0f;
        this.leg4.rotationPointX += 1.0f;
        this.leg3.rotationPointZ -= 1.0f;
        this.leg4.rotationPointZ -= 1.0f;
        this.childZOffset += 2.0f;
    }

    @Override
    public void render(float scale) {
        this.body.rotateAngleX = 1.5707964f;
        String style = this.cosmetic.getString("style");
        if (style.equalsIgnoreCase("brown")) {
            this.bindCosmeticTexture("llama_brown.png");
        } else if (style.equalsIgnoreCase("creamy")) {
            this.bindCosmeticTexture("llama_creamy.png");
        } else if (style.equalsIgnoreCase("gray")) {
            this.bindCosmeticTexture("llama_gray.png");
        } else if (style.equalsIgnoreCase("white")) {
            this.bindCosmeticTexture("llama_white.png");
        } else {
            this.bindCosmeticTexture("llama.png");
        }
        this.setHeadRotations();
        GL11.glScalef((float)0.3f, (float)0.3f, (float)0.3f);
        GL11.glTranslatef((float)0.0f, (float)-3.1f, (float)0.0f);
        this.head.render(scale);
        this.body.render(scale);
        this.leg1.render(scale);
        this.leg2.render(scale);
        this.leg3.render(scale);
        this.leg4.render(scale);
        super.render(scale);
    }
}

