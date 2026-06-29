package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=17, height=0.3, activatergb=true, displayname="Snowman", type=CosmeticType.LIMITED)
public class Cosmetic017
extends CosmeticModelRenderer {
    CosmeticModelRenderer SMbody;
    CosmeticModelRenderer SMbottomBody;
    CosmeticModelRenderer SMhead;
    CosmeticModelRenderer SMrightHand;
    CosmeticModelRenderer SMleftHand;

    public Cosmetic017(ModelBase base) {
        super(base);
        float SMf = 4.0f;
        float SMf1 = 0.0f;
        this.SMhead = new CosmeticModelRenderer(base, 0, 0).setTextureSize(64, 64);
        this.SMhead.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, SMf1 - 0.5f);
        this.SMhead.setRotationPoint(0.0f, 0.0f + SMf, 0.0f);
        this.SMrightHand = new CosmeticModelRenderer(base, 32, 0).setTextureSize(64, 64);
        this.SMrightHand.addBox(-1.0f, 0.0f, -1.0f, 12, 2, 2, SMf1 - 0.5f);
        this.SMrightHand.setRotationPoint(0.0f, 0.0f + SMf + 9.0f - 7.0f, 0.0f);
        this.SMleftHand = new CosmeticModelRenderer(base, 32, 0).setTextureSize(64, 64);
        this.SMleftHand.addBox(-1.0f, 0.0f, -1.0f, 12, 2, 2, SMf1 - 0.5f);
        this.SMleftHand.setRotationPoint(0.0f, 0.0f + SMf + 9.0f - 7.0f, 0.0f);
        this.SMbody = new CosmeticModelRenderer(base, 0, 16).setTextureSize(64, 64);
        this.SMbody.addBox(-5.0f, -10.0f, -5.0f, 10, 10, 10, SMf1 - 0.5f);
        this.SMbody.setRotationPoint(0.0f, 0.0f + SMf + 9.0f, 0.0f);
        this.SMbottomBody = new CosmeticModelRenderer(base, 0, 36).setTextureSize(64, 64);
        this.SMbottomBody.addBox(-6.0f, -12.0f, -6.0f, 12, 12, 12, SMf1 - 0.5f);
        this.SMbottomBody.setRotationPoint(0.0f, 0.0f + SMf + 20.0f, 0.0f);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        float f = MathHelper.sin((float)this.SMbody.rotateAngleY);
        float f1 = MathHelper.cos((float)this.SMbody.rotateAngleY);
        this.SMrightHand.rotateAngleZ = 1.0f;
        this.SMleftHand.rotateAngleZ = -1.0f;
        this.SMrightHand.rotateAngleY = 0.0f + this.SMbody.rotateAngleY;
        this.SMleftHand.rotateAngleY = (float)Math.PI + this.SMbody.rotateAngleY;
        this.SMrightHand.rotationPointX = f1 * 5.0f;
        this.SMrightHand.rotationPointZ = -f * 5.0f;
        this.SMleftHand.rotationPointX = -f1 * 5.0f;
        this.SMleftHand.rotationPointZ = f * 5.0f;
        GL11.glEnable((int)2977);
        this.setHeadRotations();
        GL11.glScalef((float)0.25f, (float)0.25f, (float)0.25f);
        GL11.glTranslatef((float)0.0f, (float)-3.5f, (float)0.0f);
        if (this.cosmetic.getString("head").equalsIgnoreCase("null")) {
            ItemStack item = new ItemStack(Blocks.pumpkin);
            GL11.glPushMatrix();
            GL11.glScalef((float)0.6f, (float)-0.6f, (float)0.6f);
            GL11.glTranslatef((float)0.0f, (float)0.1f, (float)0.0f);
            this.renderItem(item);
            GL11.glPopMatrix();
        }
        this.bindEntityTexture("snowman.png");
        this.SMrightHand.render(scale);
        this.SMleftHand.render(scale);
        this.renderRGB("color", 100, 100, 100);
        this.SMbody.render(scale);
        this.SMbottomBody.render(scale);
        this.SMhead.render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

