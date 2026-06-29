package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=33, height=0.8, activatergb=false, displayname="Evoker Fangs", type=CosmeticType.LIMITED)
public class Cosmetic033
extends CosmeticModelRenderer {
    private final CosmeticModelRenderer base;
    private final CosmeticModelRenderer upperJaw;
    private final CosmeticModelRenderer lowerJaw;

    public Cosmetic033(ModelBase base) {
        super(base);
        this.base = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 32);
        this.base.setRotationPoint(-5.0f, 22.0f, -5.0f);
        this.base.addBox(0.0f, 0.0f, 0.0f, 10, 12, 10);
        this.upperJaw = new CosmeticModelRenderer(base, 40, 0).setTextureSize(64, 32);
        this.upperJaw.setRotationPoint(1.5f, 22.0f, -4.0f);
        this.upperJaw.addBox(0.0f, 0.0f, 0.0f, 4, 14, 8);
        this.lowerJaw = new CosmeticModelRenderer(base, 40, 0).setTextureSize(64, 32);
        this.lowerJaw.setRotationPoint(-1.5f, 22.0f, 4.0f);
        this.lowerJaw.addBox(0.0f, 0.0f, 0.0f, 4, 14, 8);
    }

    @Override
    public void render(float scale) {
        super.render(scale);
        float f = this.ticks_3 * 2.0f;
        if (f > 1.0f) {
            f = 1.0f;
        }
        f = 1.0f - f * f * f;
        this.upperJaw.rotateAngleZ = (float)Math.PI - f * 0.35f * (float)Math.PI;
        this.lowerJaw.rotateAngleZ = (float)Math.PI + f * 0.35f * (float)Math.PI;
        this.lowerJaw.rotateAngleY = (float)Math.PI;
        float f1 = (this.ticks_2 + MathHelper.sin((float)(this.ticks_2 * 2.7f))) * 0.6f * 12.0f;
        this.lowerJaw.rotationPointY = this.upperJaw.rotationPointY = 24.0f;
        this.base.rotationPointY = this.upperJaw.rotationPointY;
        this.bindCosmeticTexture("fangs.png");
        this.setHeadRotations();
        GL11.glScalef((float)0.7f, (float)0.7f, (float)0.7f);
        GL11.glTranslatef((float)0.0f, (float)-2.9f, (float)0.0f);
        this.base.render(scale);
        this.upperJaw.render(scale);
        this.lowerJaw.render(scale);
    }
}

