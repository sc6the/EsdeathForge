package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticManager;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=24, height=0.0, activatergb=false, displayname="Sharingan", type=CosmeticType.DEFAULT)
public class Cosmetic024
extends CosmeticModelRenderer {
    CosmeticModelRenderer sharingan;
    CosmeticModelRenderer sharingan1;

    public Cosmetic024(ModelBase model) {
        super(model);
        this.sharingan = new CosmeticModelRenderer(model, 0, 0).setTextureSize(8, 8);
        this.sharingan.addBox(0.0f, 0.0f, 0.999f, 1, 1, 1);
        this.sharingan.setRotationPoint(-1.0f, -4.0f, -5.0f);
        this.sharingan1 = new CosmeticModelRenderer(model, 0, 0).setTextureSize(8, 8);
        this.sharingan1.addBox(3.0f, 0.0f, 0.999f, 1, 1, 1);
        this.sharingan1.setRotationPoint(-3.0f, -4.0f, -5.0f);
    }

    @Override
    public void render(float p_78785_1_) {
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)(0.125f * this.cosmetic.getFloat("height")), (float)0.0f);
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        String[] sharingantex = new String[]{"sharingan", "sasuke", "itachi", "madara", "obito"};
        String type = this.cosmetic.getString("type");
        if (type.equalsIgnoreCase(sharingantex[1])) {
            this.bindCosmeticTexture("eye_sasuke_mangekyou.png");
        } else if (type.equalsIgnoreCase(sharingantex[2])) {
            this.bindCosmeticTexture("eye_itachi_mangekyou.png");
        } else if (type.equalsIgnoreCase(sharingantex[3])) {
            this.bindCosmeticTexture("eye_madara_mangekyou.png");
        } else if (type.equalsIgnoreCase(sharingantex[4])) {
            this.bindCosmeticTexture("eye_obito_mangekyou.png");
        } else {
            this.bindCosmeticTexture("eye_sharingan.png");
        }
        float distance = this.cosmetic.getFloat("distance");
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(0.125f * distance), (float)0.0f, (float)0.0f);
        if (!CosmeticManager.hasEntityCosmetic(this.entityIn, 35)) {
            this.sharingan.render(p_78785_1_);
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(-0.125f * distance), (float)0.0f, (float)0.0f);
        this.sharingan1.render(p_78785_1_);
        GL11.glPopMatrix();
        super.render(p_78785_1_);
    }
}

