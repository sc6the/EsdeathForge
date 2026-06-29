package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=34, height=0.0, activatergb=false, displayname="Double Sword", type=CosmeticType.EXCLUSIVE)
public class Cosmetic034
extends CosmeticModelRenderer {
    public Cosmetic034(ModelBase base) {
        super(base);
    }

    @Override
    public void render(float glScalef) {
        ItemStack sword = new ItemStack(Items.iron_sword);
        if (this.isSneaking()) {
            GL11.glRotatef((float)20.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        }
        GL11.glPushMatrix();
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glTranslatef((float)0.53f, (float)1.5f, (float)-0.2f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        this.renderItem(sword);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glTranslatef((float)-0.53f, (float)1.5f, (float)-0.2f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)90.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        this.renderItem(sword);
        GL11.glPopMatrix();
        super.render(glScalef);
    }
}

