package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=10, height=0.0, activatergb=false, displayname="Villager Nose", type=CosmeticType.DEFAULT)
public class Cosmetic010
extends CosmeticModelRenderer {
    CosmeticModelRenderer villagerNose;

    public Cosmetic010(ModelBase base) {
        super(base);
        this.villagerNose = new CosmeticModelRenderer(base, 0, 10).setTextureSize(64, 64);
        this.villagerNose.setRotationPoint(0.0f, -2.0f, 0.0f);
        this.villagerNose.setTextureOffset(24, 0).addBox(-1.0f, -1.0f, -6.0f, 2, 4, 2, 0.0f);
        this.villagerNose.isHidden = true;
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        this.bindEntityTexture("villager/villager.png");
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.setHeadRotations();
        this.villagerNose.isHidden = false;
        this.villagerNose.render(scale);
        this.villagerNose.isHidden = true;
        GL11.glPopMatrix();
        super.render(scale);
    }
}

