package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=26, height=0.2, activatergb=true, displayname="Luffy's Staw Hat", type=CosmeticType.EXCLUSIVE)
public class Cosmetic026
extends CosmeticModelRenderer {
    CosmeticModelRenderer straw;
    CosmeticModelRenderer red;

    public Cosmetic026(ModelBase model) {
        super(model);
        this.straw = new CosmeticModelRenderer(model);
        this.red = new CosmeticModelRenderer(model);
        this.straw.addBox(-4.0f, -8.98f, -4.0f, 8, 1, 8);
        this.straw.addBox(-4.0f, -9.98f, -4.0f, 8, 1, 8);
        this.straw.addBox(-3.0f, -10.98f, -3.0f, 6, 1, 6);
        this.straw.addBox(-4.0f, -7.98f, -5.28f, 8, 1, 2);
        this.straw.addBox(-4.0f, -7.98f, 3.28f, 8, 1, 2);
        this.straw.addBox(-5.28f, -7.98f, -4.0f, 2, 1, 8);
        this.straw.addBox(3.28f, -7.98f, -4.0f, 2, 1, 8);
        this.red.addBox(-4.0f, -8.97f, -4.1f, 8, 1, 1);
        this.red.addBox(-4.0f, -8.97f, 3.1f, 8, 1, 1);
        this.red.addBox(3.1f, -8.97f, -4.0f, 1, 1, 8);
        this.red.addBox(-4.1f, -8.97f, -4.0f, 1, 1, 8);
    }

    @Override
    public void render(float p_78785_1_) {
        this.setHeadRotations();
        this.bindCosmeticTexture("resources.png");
        if (this.isHelmetEquipped()) {
            GL11.glScalef((float)1.3f, (float)1.1f, (float)1.3f);
        }
        this.renderRGB("straw", 100, 87, 0);
        this.straw.render(p_78785_1_);
        this.renderRGB("color", 90, 1, 1);
        this.red.render(p_78785_1_);
        super.render(p_78785_1_);
    }
}

