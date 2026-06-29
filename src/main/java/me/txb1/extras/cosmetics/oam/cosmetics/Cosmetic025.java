package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=25, height=0.0, activatergb=false, displayname="Angry Eyes", type=CosmeticType.EXCLUSIVE)
public class Cosmetic025
extends CosmeticModelRenderer {
    boolean start = false;
    int i = 0;
    CosmeticModelRenderer angryeyes;
    CosmeticModelRenderer angryeyes2;

    public Cosmetic025(ModelBase model) {
        super(model);
        this.angryeyes = new CosmeticModelRenderer(model, 0, 0).setTextureSize(256, 256);
        this.angryeyes.addBox(0.0f, 1.0f, 0.999f, 1, 1, 1);
        this.angryeyes.addBox(3.0f, 1.0f, 0.999f, 1, 1, 1);
        this.angryeyes.setRotationPoint(-2.0f, -4.0f, -5.0f);
        this.angryeyes2 = new CosmeticModelRenderer(model, 0, 0).setTextureSize(256, 256);
        this.angryeyes2.addBox(0.0f, 2.0f, 0.999f, 1, 1, 1);
        this.angryeyes2.addBox(3.0f, 2.0f, 0.999f, 1, 1, 1);
        this.angryeyes2.setRotationPoint(-2.0f, -4.0f, -5.0f);
    }

    @Override
    public void render(float scale) {
        this.setHeadRotations();
        this.bindCosmeticTexture("resources.png");
        int height = this.cosmetic.getInteger("height");
        boolean size = this.cosmetic.getString("size").equals("null");
        float pos = 0.0f;
        switch (height) {
            case 0: {
                pos = 0.0f;
                break;
            }
            case 1: {
                pos = -0.312f;
                break;
            }
            case 2: {
                pos = -0.25f;
                break;
            }
            case 4: {
                pos = -0.187f;
                break;
            }
            case 5: {
                pos = -0.125f;
                break;
            }
            case 6: {
                pos = -0.062f;
                break;
            }
            case 7: {
                pos = 0.0f;
                break;
            }
            case 8: {
                pos = 0.062f;
            }
        }
        GL11.glTranslatef((float)0.0f, (float)pos, (float)0.0f);
        GL11.glColor3f((float)1.1f, (float)0.0f, (float)0.0f);
        if (this.start) {
            this.angryeyes.render(scale);
            if (!size) {
                this.angryeyes2.render(scale);
            }
        }
        super.render(scale);
    }

    @Override
    public void performAnimation() {
        super.performAnimation();
        if (this.entityIn.hurtResistantTime > 0) {
            this.start = true;
            this.i = 0;
        }
        if (this.start && this.i < 1500) {
            ++this.i;
        } else {
            this.start = false;
            this.i = 0;
        }
    }
}

