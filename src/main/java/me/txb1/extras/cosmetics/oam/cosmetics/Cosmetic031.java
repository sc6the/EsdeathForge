package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=31, height=0.0, activatergb=false, displayname="Fish", type=CosmeticType.LIMITED)
public class Cosmetic031
extends CosmeticModelRenderer {
    public Cosmetic031(ModelBase base) {
        super(base);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        float k = this.ticks_4 / 10.0f;
        float i = (float)(Math.cos(k) * Math.PI);
        float f = i * 0.05f;
        float l1 = (float)Math.cos(this.ticks_4 / 20.0f);
        float l = l1 * 0.25f;
        ItemStack fish1 = this.cosmetic.getString("type").equalsIgnoreCase("cook") ? new ItemStack(Items.cooked_fish) : new ItemStack(Items.fish);
        GL11.glTranslatef((float)l, (float)f, (float)(1.0f + f));
        GL11.glRotatef((float)-40.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)270.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glScaled((double)0.5, (double)0.5, (double)0.5);
        this.renderItem(fish1);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

