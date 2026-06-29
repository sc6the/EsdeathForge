package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelBox;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=57, height=0.35, activatergb=false, displayname="Santa Hat", type=CosmeticType.LIMITED)
public class Cosmetic057
extends CosmeticModelRenderer {
    private final CosmeticModelRenderer santahat;
    private final CosmeticModelRenderer ball4_r1;
    private final CosmeticModelRenderer layer6_r1;
    private final CosmeticModelRenderer layer5_r1;
    private final CosmeticModelRenderer layer4_r1;
    private final CosmeticModelRenderer layer3_r1;
    private final CosmeticModelRenderer layer2_r1;
    private final CosmeticModelRenderer layer1_r1;
    private final CosmeticModelRenderer layer0_r1;

    public Cosmetic057(ModelBase base) {
        super(base);
        this.santahat = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.santahat.setRotationPoint(0.0f, 24.0f, 0.0f);
        this.santahat.cubeList.add(new CosmeticModelBox(this.santahat, 0, 32, -9.0f, -17.0f, -9.0f, 18, 3, 18, 0.0f, false));
        this.ball4_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.ball4_r1.setRotationPoint(15.0f, -27.0f, 1.0f);
        this.santahat.addChild(this.ball4_r1);
        this.setRotation(this.ball4_r1, 0.0f, 0.0f, 0.8727f);
        this.ball4_r1.cubeList.add(new CosmeticModelBox(this.ball4_r1, 20, 72, -5.5f, -4.5f, 1.0f, 6, 4, 4, 0.0f, false));
        this.ball4_r1.cubeList.add(new CosmeticModelBox(this.ball4_r1, 0, 0, -4.5f, -5.5f, 1.0f, 4, 6, 4, 0.0f, false));
        this.ball4_r1.cubeList.add(new CosmeticModelBox(this.ball4_r1, 58, 69, -4.5f, -4.5f, 0.0f, 4, 4, 6, 0.0f, false));
        this.ball4_r1.cubeList.add(new CosmeticModelBox(this.ball4_r1, 0, 72, -5.0f, -5.0f, 0.5f, 5, 5, 5, 0.0f, false));
        this.layer6_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer6_r1.setRotationPoint(8.0f, -33.0f, 3.0f);
        this.santahat.addChild(this.layer6_r1);
        this.setRotation(this.layer6_r1, 0.0f, 0.0f, 1.789f);
        this.layer6_r1.cubeList.add(new CosmeticModelBox(this.layer6_r1, 0, 42, -2.0f, -6.0f, 0.0f, 2, 6, 2, 0.0f, false));
        this.layer5_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer5_r1.setRotationPoint(31.0f, -45.0f, 5.0f);
        this.santahat.addChild(this.layer5_r1);
        this.setRotation(this.layer5_r1, -0.1309f, 0.0f, 1.0036f);
        this.layer5_r1.cubeList.add(new CosmeticModelBox(this.layer5_r1, 0, 32, -4.0f, 24.0f, 0.0f, 4, 6, 4, 0.0f, false));
        this.layer4_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer4_r1.setRotationPoint(4.0f, -24.0f, -2.0f);
        this.santahat.addChild(this.layer4_r1);
        this.setRotation(this.layer4_r1, -0.2182f, 0.0f, 0.6109f);
        this.layer4_r1.cubeList.add(new CosmeticModelBox(this.layer4_r1, 54, 35, -6.0f, -8.0f, 0.0f, 6, 8, 6, 0.0f, false));
        this.layer3_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer3_r1.setRotationPoint(5.0f, -21.0f, -5.0f);
        this.santahat.addChild(this.layer3_r1);
        this.setRotation(this.layer3_r1, -0.1745f, 0.0f, 0.2618f);
        this.layer3_r1.cubeList.add(new CosmeticModelBox(this.layer3_r1, 48, 53, -9.0f, -6.0f, 0.0f, 9, 6, 10, 0.0f, false));
        this.layer2_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer2_r1.setRotationPoint(6.0f, -20.0f, -6.0f);
        this.santahat.addChild(this.layer2_r1);
        this.setRotation(this.layer2_r1, -0.1309f, 0.0f, 0.1309f);
        this.layer2_r1.cubeList.add(new CosmeticModelBox(this.layer2_r1, 48, 0, -12.0f, -4.0f, 0.0f, 12, 4, 12, 0.0f, false));
        this.layer1_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer1_r1.setRotationPoint(7.0f, -18.0f, -7.0f);
        this.santahat.addChild(this.layer1_r1);
        this.setRotation(this.layer1_r1, -0.0873f, 0.0f, 0.0873f);
        this.layer1_r1.cubeList.add(new CosmeticModelBox(this.layer1_r1, 54, 18, -14.0f, -3.0f, 0.0f, 14, 3, 14, 0.0f, false));
        this.layer0_r1 = new CosmeticModelRenderer(base).setTextureSize(128, 128);
        this.layer0_r1.setRotationPoint(8.0f, -17.0f, -8.0f);
        this.santahat.addChild(this.layer0_r1);
        this.setRotation(this.layer0_r1, -0.0436f, 0.0f, 0.0436f);
        this.layer0_r1.cubeList.add(new CosmeticModelBox(this.layer0_r1, 0, 53, -16.0f, -2.0f, 0.0f, 16, 3, 16, 0.0f, false));
    }

    @Override
    public void render(float scale) {
        super.render(scale);
        GL11.glPushMatrix();
        this.bindCosmeticTexture("santa_hat.png");
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)-0.75f, (float)0.0f);
        GL11.glScalef((float)0.51f, (float)0.51f, (float)0.51f);
        this.santahat.render(scale);
        GL11.glPopMatrix();
    }
}

