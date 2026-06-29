package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=22, height=0.0, activatergb=true, displayname="Guardian Tail", type=CosmeticType.DEFAULT)
public class Cosmetic022
extends CosmeticModelRenderer {
    CosmeticModelRenderer[] guardianTail;
    HashMap<UUID, Float> actionMap = new HashMap();

    public Cosmetic022(ModelBase base) {
        super(base);
        this.guardianTail = new CosmeticModelRenderer[3];
        this.guardianTail[0] = new CosmeticModelRenderer(base, 40, 0).setTextureSize(64, 64);
        this.guardianTail[0].addBox(-2.0f, 14.0f, 7.0f, 4, 4, 8);
        this.guardianTail[1] = new CosmeticModelRenderer(base, 0, 54).setTextureSize(64, 64);
        this.guardianTail[1].addBox(0.0f, 14.0f, 0.0f, 3, 3, 7);
        this.guardianTail[2] = new CosmeticModelRenderer(base).setTextureSize(64, 64);
        this.guardianTail[2].setTextureOffset(41, 32).addBox(0.0f, 14.0f, 0.0f, 2, 2, 6);
        this.guardianTail[2].setTextureOffset(25, 19).addBox(1.0f, 10.5f, 3.0f, 1, 9, 9);
        this.guardianTail[0].addChild(this.guardianTail[1]);
        this.guardianTail[1].addChild(this.guardianTail[2]);
    }

    @Override
    public void render(float scale) {
        if (!this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            this.actionMap.put(this.entityIn.getUniqueID(), Float.valueOf(0.0f));
        }
        GL11.glPushMatrix();
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.14f, (float)0.1f);
            GL11.glRotatef((float)20.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        } else {
            GL11.glTranslatef((float)0.0f, (float)-0.02f, (float)0.1f);
            GL11.glRotatef((float)-10.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        float anim = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
        this.guardianTail[0].rotateAngleY = MathHelper.sin((float)anim) * (float)Math.PI * 0.05f;
        this.guardianTail[1].rotateAngleY = MathHelper.sin((float)anim) * (float)Math.PI * 0.1f;
        this.guardianTail[1].rotationPointX = -1.5f;
        this.guardianTail[1].rotationPointY = 0.5f;
        this.guardianTail[1].rotationPointZ = 14.0f;
        this.guardianTail[2].rotateAngleY = MathHelper.sin((float)anim) * (float)Math.PI * 0.15f;
        this.guardianTail[2].rotationPointX = 0.5f;
        this.guardianTail[2].rotationPointY = 0.5f;
        this.guardianTail[2].rotationPointZ = 6.0f;
        if (this.cosmetic.getString("style").equalsIgnoreCase("elder")) {
            this.bindCosmeticTexture("guardian_elder.png");
        } else {
            this.bindCosmeticTexture("guardian.png");
        }
        this.renderRGB("color", 100, 100, 100);
        GL11.glTranslatef((float)0.01f, (float)-0.05f, (float)-0.3f);
        GL11.glScalef((float)0.7f, (float)0.7f, (float)0.7f);
        this.guardianTail[0].render(scale);
        GL11.glPopMatrix();
        super.render(scale);
    }

    @Override
    public void performAnimation() {
        if (this.actionMap.containsKey(this.entityIn.getUniqueID())) {
            float value = this.actionMap.get(this.entityIn.getUniqueID()).floatValue();
            value = this.entityIn.isSneaking() ? (value += 0.005f) : (this.entityIn.isSprinting() ? (value += 0.025f) : (value += 0.008f));
            this.actionMap.replace(this.entityIn.getUniqueID(), Float.valueOf(value));
        }
        super.performAnimation();
    }
}

