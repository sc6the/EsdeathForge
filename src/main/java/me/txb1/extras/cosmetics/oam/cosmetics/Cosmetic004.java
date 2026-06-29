package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.Random;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=4, height=0.2, activatergb=true, displayname="Hairs", type=CosmeticType.DEFAULT)
public class Cosmetic004
extends CosmeticModelRenderer {
    CosmeticModelRenderer[] tentacles = new CosmeticModelRenderer[9];

    public Cosmetic004(ModelBase base) {
        super(base);
        Random random = new Random(1660L);
        for (int j = 0; j < this.tentacles.length; ++j) {
            this.tentacles[j] = new CosmeticModelRenderer(base, 0, 0);
            float f = (((float)(j % 3) - (float)(j / 3 % 2) * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float f1 = ((float)(j / 3) / 2.0f * 2.0f - 1.0f) * 5.0f;
            int k = random.nextInt(7) + 8;
            this.tentacles[j].addBox(-1.0f, 0.0f, -1.0f, 2, k, 2);
            this.tentacles[j].rotationPointX = f;
            this.tentacles[j].rotationPointZ = f1;
            this.tentacles[j].rotationPointY = 15.0f;
            this.tentacles[j].isHidden = true;
        }
    }

    @Override
    public void render(float scale) {
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i].rotateAngleX = 0.2f * MathHelper.sin((float)(this.ticks_4 * 0.3f + (float)i)) + 0.4f;
        }
        GL11.glTranslatef((float)0.0f, (float)0.03f, (float)0.0f);
        for (int j = 0; j < this.tentacles.length; ++j) {
            GL11.glPushMatrix();
            this.bindCosmeticTexture("resources.png");
            this.setHeadRotations();
            GL11.glScalef((float)0.5f, (float)-0.5f, (float)0.5f);
            this.renderRGB("color", 100, 100, 100);
            this.tentacles[j].isHidden = false;
            this.tentacles[j].render(scale);
            this.tentacles[j].isHidden = true;
            GL11.glPopMatrix();
        }
        super.render(scale);
    }
}

