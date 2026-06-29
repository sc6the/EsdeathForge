package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=3, height=0.0, activatergb=false, displayname="Chicken Beak", type=CosmeticType.DEFAULT)
public class Cosmetic003
extends CosmeticModelRenderer {
    CosmeticModelRenderer beak;

    public Cosmetic003(ModelBase base) {
        super(base);
        this.beak = new CosmeticModelRenderer(base, 14, 0).setTextureSize(64, 32);
        this.beak.addBox(-2.0f, -4.0f, -4.0f, 4, 2, 2, 0.0f);
        this.beak.setRotationPoint(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(float scale) {
        float height;
        this.bindEntityTexture("chicken.png");
        this.setHeadRotations();
        float f = height = this.cosmetic.getInteger("height") == 0 ? 0.35f : (float)this.cosmetic.getInteger("height") * 0.01f;
        if (height < 0.0f || height > 0.4f) {
            height = 0.35f;
        }
        GL11.glTranslatef((float)0.0f, (float)(-0.3f + height), (float)-0.156f);
        GL11.glScalef((float)0.8f, (float)0.8f, (float)0.8f);
        this.beak.render(scale);
        super.render(scale);
    }
}

