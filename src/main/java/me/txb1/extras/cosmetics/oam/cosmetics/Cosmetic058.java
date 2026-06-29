package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelBox;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=58, height=0.0, activatergb=false, displayname="Mask", type=CosmeticType.EXCLUSIVE)
public class Cosmetic058
extends CosmeticModelRenderer {
    private final CosmeticModelRenderer mask;
    private final CosmeticModelRenderer loop4_r1;
    private final CosmeticModelRenderer loop3_r1;
    private final CosmeticModelRenderer loop2_r1;
    private final CosmeticModelRenderer loop1_r1;
    private final CosmeticModelRenderer inner4_r1;
    private final CosmeticModelRenderer inner3_r1;
    private final CosmeticModelRenderer inner2_r1;
    private final CosmeticModelRenderer inner1_r1;
    private final CosmeticModelRenderer cube5_r1;
    private final CosmeticModelRenderer cube4_r1;
    private final CosmeticModelRenderer cube3_r1;
    private final CosmeticModelRenderer cube2_r1;
    private final CosmeticModelRenderer cube1_r1;

    public Cosmetic058(ModelBase base) {
        super(base);
        this.mask = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.mask.setRotationPoint(0.0f, 24.0f, 0.0f);
        this.mask.cubeList.add(new CosmeticModelBox(this.mask, 0, 21, 8.0f, -5.5f, -3.0f, 1, 1, 6, 0.0f, false));
        this.mask.cubeList.add(new CosmeticModelBox(this.mask, 9, 0, 8.0f, -1.0f, -3.0f, 1, 1, 6, 0.0f, false));
        this.loop4_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.loop4_r1.setRotationPoint(8.0f, -4.0f, -9.0f);
        this.mask.addChild(this.loop4_r1);
        this.setRotation(this.loop4_r1, 0.0f, 0.0873f, -0.0873f);
        this.loop4_r1.cubeList.add(new CosmeticModelBox(this.loop4_r1, 18, 9, -9.0f, -1.0f, 0.2f, 9, 1, 1, 0.0f, false));
        this.loop3_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.loop3_r1.setRotationPoint(8.0f, -4.0f, 8.0f);
        this.mask.addChild(this.loop3_r1);
        this.setRotation(this.loop3_r1, 0.0f, -0.0873f, -0.0873f);
        this.loop3_r1.cubeList.add(new CosmeticModelBox(this.loop3_r1, 18, 11, -9.0f, -1.0f, -0.2f, 9, 1, 1, 0.0f, false));
        this.loop2_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.loop2_r1.setRotationPoint(7.0f, 0.0f, -8.0f);
        this.mask.addChild(this.loop2_r1);
        this.setRotation(this.loop2_r1, 0.0f, 0.0873f, 0.3491f);
        this.loop2_r1.cubeList.add(new CosmeticModelBox(this.loop2_r1, 18, 13, -8.7f, -1.1f, -0.8f, 9, 1, 1, 0.0f, false));
        this.loop1_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.loop1_r1.setRotationPoint(7.0f, 0.0f, 8.0f);
        this.mask.addChild(this.loop1_r1);
        this.setRotation(this.loop1_r1, 0.0f, -0.0873f, 0.3491f);
        this.loop1_r1.cubeList.add(new CosmeticModelBox(this.loop1_r1, 16, 19, -8.7f, -1.1f, -0.2f, 9, 1, 1, 0.0f, false));
        this.inner4_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.inner4_r1.setRotationPoint(9.0f, 0.0f, 0.0f);
        this.mask.addChild(this.inner4_r1);
        this.setRotation(this.inner4_r1, 0.0f, 0.0873f, 0.0873f);
        this.inner4_r1.cubeList.add(new CosmeticModelBox(this.inner4_r1, 0, 0, -1.2f, -3.1f, -7.0f, 1, 3, 7, 0.0f, false));
        this.inner3_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.inner3_r1.setRotationPoint(9.0f, 0.0f, 0.0f);
        this.mask.addChild(this.inner3_r1);
        this.setRotation(this.inner3_r1, 0.0f, -0.0873f, 0.0873f);
        this.inner3_r1.cubeList.add(new CosmeticModelBox(this.inner3_r1, 9, 9, -1.2f, -3.1f, 0.0f, 1, 3, 7, 0.0f, false));
        this.inner2_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.inner2_r1.setRotationPoint(9.5f, -3.5f, -4.0f);
        this.mask.addChild(this.inner2_r1);
        this.setRotation(this.inner2_r1, 0.0436f, 0.0873f, -0.1745f);
        this.inner2_r1.cubeList.add(new CosmeticModelBox(this.inner2_r1, 0, 12, -1.8f, -1.5f, -3.0f, 1, 2, 7, 0.0f, false));
        this.inner1_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.inner1_r1.setRotationPoint(9.5f, -3.5f, 3.0f);
        this.mask.addChild(this.inner1_r1);
        this.setRotation(this.inner1_r1, -0.0436f, -0.0873f, -0.1745f);
        this.inner1_r1.cubeList.add(new CosmeticModelBox(this.inner1_r1, 16, 0, -1.7f, -1.5f, -3.0f, 1, 2, 7, 0.0f, false));
        this.cube5_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.cube5_r1.setRotationPoint(8.0f, -5.0f, -9.0f);
        this.mask.addChild(this.cube5_r1);
        this.setRotation(this.cube5_r1, 0.0873f, 0.1745f, 0.0f);
        this.cube5_r1.cubeList.add(new CosmeticModelBox(this.cube5_r1, 3, 0, -1.0f, 1.0f, 0.0f, 1, -1, 6, 0.0f, false));
        this.cube4_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.cube4_r1.setRotationPoint(8.0f, 0.0f, -9.0f);
        this.mask.addChild(this.cube4_r1);
        this.setRotation(this.cube4_r1, 0.0f, 0.1309f, 0.0f);
        this.cube4_r1.cubeList.add(new CosmeticModelBox(this.cube4_r1, 14, 21, -1.0f, -1.0f, 0.0f, 1, 1, 6, 0.0f, false));
        this.cube3_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.cube3_r1.setRotationPoint(8.0f, 0.0f, -9.0f);
        this.mask.addChild(this.cube3_r1);
        this.setRotation(this.cube3_r1, 0.0f, 0.1745f, 0.0f);
        this.cube3_r1.cubeList.add(new CosmeticModelBox(this.cube3_r1, 14, 21, -1.0f, -1.0f, 0.0f, 1, 1, 6, 0.0f, false));
        this.cube2_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.cube2_r1.setRotationPoint(8.0f, 0.0f, 9.0f);
        this.mask.addChild(this.cube2_r1);
        this.setRotation(this.cube2_r1, 0.0f, -0.1745f, 0.0f);
        this.cube2_r1.cubeList.add(new CosmeticModelBox(this.cube2_r1, 22, 22, -1.0f, -1.0f, -6.0f, 1, 1, 6, 0.0f, false));
        this.cube1_r1 = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.cube1_r1.setRotationPoint(8.0f, -5.0f, 9.0f);
        this.mask.addChild(this.cube1_r1);
        this.setRotation(this.cube1_r1, 3.0543f, -0.1745f, 0.0f);
        this.cube1_r1.cubeList.add(new CosmeticModelBox(this.cube1_r1, 25, 0, -1.0f, -1.0f, 0.0f, 1, 1, 6, 0.0f, false));
    }

    @Override
    public void render(float scale) {
        super.render(scale);
        GL11.glPushMatrix();
        this.bindCosmeticTexture("mask.png");
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)-0.75f, (float)0.0f);
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        this.mask.render(scale);
        GL11.glPopMatrix();
    }
}

