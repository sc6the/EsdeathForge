package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=9, height=0.0, activatergb=true, displayname="Sunglasses", type=CosmeticType.DEFAULT)
public class Cosmetic009
extends CosmeticModelRenderer {
    CosmeticModelRenderer sunglasses;

    public Cosmetic009(ModelBase base) {
        super(base);
        this.sunglasses = new CosmeticModelRenderer(base);
        this.sunglasses.addBox(-4.0001f, -5.0f, -4.495f, 8, 1, 1);
        this.sunglasses.addBox(-3.2001f, -4.65f, -4.4951f, 2, 1, 1);
        this.sunglasses.addBox(-2.8001f, -4.65f, -4.4952f, 2, 1, 1);
        this.sunglasses.addBox(-3.0001f, -4.35f, -4.4953f, 1, 1, 1);
        this.sunglasses.addBox(-2.0001f, -4.35f, -4.4954f, 1, 1, 1);
        this.sunglasses.addBox(-2.8001f, -4.0f, -4.4955f, 1, 1, 1);
        this.sunglasses.addBox(-2.2001f, -4.0f, -4.4956f, 1, 1, 1);
        this.sunglasses.addBox(0.8001f, -4.65f, -4.4951f, 2, 1, 1);
        this.sunglasses.addBox(1.2001f, -4.65f, -4.4952f, 2, 1, 1);
        this.sunglasses.addBox(2.0001f, -4.35f, -4.4953f, 1, 1, 1);
        this.sunglasses.addBox(1.0001f, -4.35f, -4.4954f, 1, 1, 1);
        this.sunglasses.addBox(1.8001f, -4.0f, -4.4955f, 1, 1, 1);
        this.sunglasses.addBox(1.2001f, -4.0f, -4.4956f, 1, 1, 1);
        this.sunglasses.addBox(3.501f, -5.0f, -4.495f, 1, 1, 5);
        this.sunglasses.addBox(3.501f, -4.5f, 0.495f, 1, 1, 1);
        this.sunglasses.addBox(3.502f, -3.7f, 0.795f, 1, 1, 1);
        this.sunglasses.addBox(-4.501f, -5.0f, -4.495f, 1, 1, 5);
        this.sunglasses.addBox(-4.501f, -4.5f, 0.495f, 1, 1, 1);
        this.sunglasses.addBox(-4.502f, -3.7f, 0.795f, 1, 1, 1);
    }

    @Override
    public void render(float scale) {
        this.bindCosmeticTexture("resources.png");
        this.renderRGB("color", 0, 0, 0);
        float base = 0.125f;
        this.setHeadRotations();
        GL11.glTranslatef((float)0.0f, (float)(base * this.cosmetic.getFloat("height")), (float)0.0f);
        this.sunglasses.render(scale);
        super.render(scale);
    }
}

