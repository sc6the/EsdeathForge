package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=45, height=0.45, activatergb=true, displayname="Sheep", type=CosmeticType.EXCLUSIVE)
public class Cosmetic045
extends CosmeticModelRenderer {
    CosmeticModelRenderer head;
    CosmeticModelRenderer headOverlay;
    CosmeticModelRenderer body;
    CosmeticModelRenderer bodyOverlay;
    CosmeticModelRenderer leg1;
    CosmeticModelRenderer leg2;
    CosmeticModelRenderer leg3;
    CosmeticModelRenderer leg4;
    CosmeticModelRenderer leg1Overlay;
    CosmeticModelRenderer leg2Overlay;
    CosmeticModelRenderer leg3Overlay;
    CosmeticModelRenderer leg4Overlay;

    public Cosmetic045(ModelBase base) {
        super(base);
        this.head = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.head.addBox(-3.0f, -4.0f, -6.0f, 6, 6, 8, 0.0f);
        this.head.setRotationPoint(0.0f, 6.0f, -8.0f);
        this.headOverlay = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.headOverlay.addBox(-3.0f, -4.0f, -4.0f, 6, 6, 6, 0.6f);
        this.headOverlay.setRotationPoint(0.0f, 6.0f, -8.0f);
        this.body = new CosmeticModelRenderer(base, 28, 8).setTextureSize(64, 32);
        this.body.addBox(-4.0f, -10.0f, -7.0f, 8, 16, 6, 0.0f);
        this.body.setRotationPoint(0.0f, 5.0f, 2.0f);
        this.bodyOverlay = new CosmeticModelRenderer(base, 28, 8).setTextureSize(64, 32);
        this.bodyOverlay.addBox(-4.0f, -10.0f, -7.0f, 8, 16, 6, 1.75f);
        this.bodyOverlay.setRotationPoint(0.0f, 5.0f, 2.0f);
        float f = 0.5f;
        this.leg1 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        this.leg1.setRotationPoint(-3.0f, 12.0f, 7.0f);
        this.leg1Overlay = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg1Overlay.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, f);
        this.leg1Overlay.setRotationPoint(-3.0f, 12.0f, 7.0f);
        this.leg2 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        this.leg2.setRotationPoint(3.0f, 12.0f, 7.0f);
        this.leg2Overlay = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg2Overlay.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, f);
        this.leg2Overlay.setRotationPoint(3.0f, 12.0f, 7.0f);
        this.leg3 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        this.leg3.setRotationPoint(-3.0f, 12.0f, -5.0f);
        this.leg3Overlay = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg3Overlay.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, f);
        this.leg3Overlay.setRotationPoint(-3.0f, 12.0f, -5.0f);
        this.leg4 = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg4.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f);
        this.leg4.setRotationPoint(3.0f, 12.0f, -5.0f);
        this.leg4Overlay = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 32);
        this.leg4Overlay.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, f);
        this.leg4Overlay.setRotationPoint(3.0f, 12.0f, -5.0f);
    }

    @Override
    public void render(float scale) {
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)-1.16f, (float)-0.02f);
        GL11.glScalef((float)0.45f, (float)0.45f, (float)0.45f);
        this.bindEntityTexture("sheep/sheep.png");
        this.body.rotateAngleX = 1.5707964f;
        this.head.render(scale);
        this.body.render(scale);
        this.leg1.render(scale);
        this.leg2.render(scale);
        this.leg3.render(scale);
        this.leg4.render(scale);
        this.renderRGB("color", 100, 100, 100);
        this.bindEntityTexture("sheep/sheep_fur.png");
        this.bodyOverlay.rotateAngleX = 1.5707964f;
        this.headOverlay.render(scale);
        this.bodyOverlay.render(scale);
        this.leg1Overlay.render(scale);
        this.leg2Overlay.render(scale);
        this.leg3Overlay.render(scale);
        this.leg4Overlay.render(scale);
        super.render(scale);
    }
}

