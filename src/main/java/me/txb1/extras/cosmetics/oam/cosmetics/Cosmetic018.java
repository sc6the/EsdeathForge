package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=18, height=0.0, activatergb=false, displayname="Easter Eggs", type=CosmeticType.DEFAULT)
public class Cosmetic018
extends CosmeticModelRenderer {
    public Cosmetic018(ModelBase base) {
        super(base);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        float swing = this.ticks_4 * 15.0f;
        this.setHeadRotations();
        GL11.glRotatef((float)swing, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glScalef((float)0.25f, (float)-0.25f, (float)0.25f);
        GL11.glTranslatef((float)2.1f, (float)1.0f, (float)0.0f);
        ItemStack item = new ItemStack(Items.egg);
        this.renderItem(item);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)-3.8f, (float)0.0f, (float)0.0f);
        this.renderItem(item);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)-2.1f, (float)0.0f, (float)1.7f);
        this.renderItem(item);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)-2.7f, (float)0.0f, (float)-1.7f);
        this.renderItem(item);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        super.render(scale);
    }
}

