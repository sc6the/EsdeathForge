package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=56, height=0.0, activatergb=true, displayname="Wither", type=CosmeticType.DEFAULT)
public class Cosmetic056
extends CosmeticModelRenderer {
    private CosmeticModelRenderer[] field_82905_a = new CosmeticModelRenderer[3];
    private CosmeticModelRenderer[] field_82904_b;

    public Cosmetic056(ModelBase base) {
        super(base);
        this.field_82905_a[0] = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 64);
        this.field_82905_a[0].addBox(-10.0f, 3.9f, -0.5f, 20, 3, 3);
        this.field_82905_a[1] = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.field_82905_a[1].setRotationPoint(-2.0f, 6.9f, -0.5f);
        this.field_82905_a[1].setTextureOffset(0, 22).addBox(0.0f, 0.0f, 0.0f, 3, 10, 3);
        this.field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0f, 1.5f, 0.5f, 11, 2, 2);
        this.field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0f, 4.0f, 0.5f, 11, 2, 2);
        this.field_82905_a[1].setTextureOffset(24, 22).addBox(-4.0f, 6.5f, 0.5f, 11, 2, 2);
        this.field_82905_a[2] = new CosmeticModelRenderer(base, 12, 22).setTextureSize(64, 64);
        this.field_82905_a[2].addBox(0.0f, 0.0f, 0.0f, 3, 6, 3);
        this.field_82904_b = new CosmeticModelRenderer[3];
        this.field_82904_b[0] = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 64);
        this.field_82904_b[0].addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
        this.field_82904_b[1] = new CosmeticModelRenderer(base, 32, 0).setTextureSize(64, 64);
        this.field_82904_b[1].addBox(-4.0f, -4.0f, -4.0f, 6, 6, 6);
        this.field_82904_b[1].rotationPointX = -8.0f;
        this.field_82904_b[1].rotationPointY = 4.0f;
        this.field_82904_b[2] = new CosmeticModelRenderer(base, 32, 0).setTextureSize(64, 64);
        this.field_82904_b[2].addBox(-4.0f, -4.0f, -4.0f, 6, 6, 6);
        this.field_82904_b[2].rotationPointX = 10.0f;
        this.field_82904_b[2].rotationPointY = 4.0f;
    }

    @Override
    public void render(float scale) {
        this.bindEntityTexture("wither/wither.png");
        this.renderRGB("color", 100, 100, 100);
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        float swing = (float)Math.cos(this.ticks_4 / 15.0f) * 0.15f;
        float swing1 = (float)Math.cos(this.ticks_2 / 8.0f) * 0.15f;
        float pos = this.cosmetic.getString("pos").equals("null") ? 2.3f : -2.0f;
        GL11.glTranslatef((float)(swing1 + pos), (float)swing, (float)0.0f);
        float f6 = MathHelper.cos((float)(this.ticks_2 * 0.1f));
        this.field_82905_a[1].rotateAngleX = (0.065f + 0.05f * f6) * (float)Math.PI;
        this.field_82905_a[2].setRotationPoint(-2.0f, 6.9f + MathHelper.cos((float)this.field_82905_a[1].rotateAngleX) * 10.0f, -0.5f + MathHelper.sin((float)this.field_82905_a[1].rotateAngleX) * 10.0f);
        this.field_82905_a[2].rotateAngleX = (0.265f + 0.1f * f6) * (float)Math.PI;
        this.field_82904_b[0].rotateAngleY = this.ticks_3 / 57.295776f;
        this.field_82904_b[0].rotateAngleX = this.ticks_3 / 57.295776f;
        for (CosmeticModelRenderer modelrenderer : this.field_82904_b) {
            modelrenderer.render(scale);
        }
        for (CosmeticModelRenderer modelrenderer : this.field_82905_a) {
            modelrenderer.render(scale);
        }
        super.render(scale);
    }
}

