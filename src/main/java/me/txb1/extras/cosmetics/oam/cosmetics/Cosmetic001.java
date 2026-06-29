package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=1, height=0.0, activatergb=true, displayname="Wolf Tail", type=CosmeticType.DEFAULT)
public class Cosmetic001
extends CosmeticModelRenderer {
    CosmeticModelRenderer wolfTail;

    public Cosmetic001(ModelBase base) {
        super(base);
        this.wolfTail = new CosmeticModelRenderer(base, 19, 18);
        this.wolfTail.addBox(-1.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.wolfTail.setRotationPoint(-0.2f, 10.0f, 3.0f);
    }

    @Override
    public void render(float scale) {
        super.render(scale);
        this.wolfTail.rotateAngleX = this.ticks_3;
        this.wolfTail.rotateAngleY = MathHelper.cos((float)(this.ticks_2 * 0.6662f)) * 1.4f * this.ticks_3;
        GL11.glPushMatrix();
        this.bindCosmeticTexture("resources.png");
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.4f, (float)-0.25f);
            GL11.glRotatef((float)45.0f, (float)45.0f, (float)0.0f, (float)0.0f);
        } else {
            GL11.glTranslatef((float)0.0f, (float)0.1f, (float)-0.25f);
            GL11.glRotatef((float)15.0f, (float)15.0f, (float)0.0f, (float)0.0f);
        }
        this.renderRGB("color", 100, 100, 100);
        this.wolfTail.render(scale);
        GL11.glPopMatrix();
    }
}

