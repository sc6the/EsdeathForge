package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticManager;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=35, height=0.0, activatergb=false, displayname="Rinnegan", type=CosmeticType.DEFAULT)
public class Cosmetic035
extends CosmeticModelRenderer {
    CosmeticModelRenderer rinnegan;
    CosmeticModelRenderer rinnegan1;

    public Cosmetic035(ModelBase base) {
        super(base);
        this.rinnegan = new CosmeticModelRenderer(base, 0, 0).setTextureSize(8, 8);
        this.rinnegan.addBox(0.0f, 0.0f, 0.999f, 1, 1, 1);
        this.rinnegan.setRotationPoint(-1.0f, -4.0f, -5.0f);
        this.rinnegan1 = new CosmeticModelRenderer(base, 0, 0).setTextureSize(8, 8);
        this.rinnegan1.addBox(3.0f, 0.0f, 0.999f, 1, 1, 1);
        this.rinnegan1.setRotationPoint(-3.0f, -4.0f, -5.0f);
    }

    @Override
    public void render(float scale) {
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)(0.125f * this.cosmetic.getFloat("height")), (float)0.0f);
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.bindCosmeticTexture("eye_rinnegan.png");
        float distance = this.cosmetic.getFloat("distance");
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(0.125f * distance), (float)0.0f, (float)0.0f);
        this.rinnegan.render(scale);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(-0.125f * distance), (float)0.0f, (float)0.0f);
        if (!CosmeticManager.hasEntityCosmetic(this.entityIn, 24)) {
            this.rinnegan1.render(scale);
        }
        GL11.glPopMatrix();
        super.render(scale);
    }
}

