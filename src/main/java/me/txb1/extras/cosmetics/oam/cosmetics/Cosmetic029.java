package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=29, height=0.0, activatergb=false, displayname="Sword in the Leg", type=CosmeticType.DEFAULT)
public class Cosmetic029
extends CosmeticModelRenderer {
    public Cosmetic029(ModelBase base) {
        super(base);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        if (this.isSneaking()) {
            GL11.glTranslatef((float)0.0f, (float)0.75f, (float)0.26f);
        } else if (this.entityIn.isRiding()) {
            GL11.glTranslatef((float)0.06f, (float)0.85f, (float)0.0f);
            GL11.glRotatef((float)270.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.1f, (float)-0.7f, (float)0.0f);
        } else {
            GL11.glTranslatef((float)0.0f, (float)0.75f, (float)0.02f);
        }
        String position = this.cosmetic.getString("pos");
        float mathDirection = position.equalsIgnoreCase("right") ? -1.4f : 1.4f;
        float bus = MathHelper.cos((float)(this.ticks_2 * 0.6662f + (float)Math.PI)) * mathDirection * this.ticks_3;
        float z = position.equalsIgnoreCase("null") ? 0.0f : -0.26f;
        GL11.glTranslatef((float)z, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)((float)((double)bus * 57.29577951308232)), (float)1.0f, (float)0.0f, (float)0.0f);
        ItemStack item = null;
        String typ = this.cosmetic.getString("texture");
        item = typ.equalsIgnoreCase("wood") ? new ItemStack(Items.wooden_sword) : (typ.equalsIgnoreCase("stone") ? new ItemStack(Items.stone_sword) : (typ.equalsIgnoreCase("iron") ? new ItemStack(Items.iron_sword) : (typ.equalsIgnoreCase("gold") ? new ItemStack(Items.golden_sword) : new ItemStack(Items.diamond_sword))));
        GL11.glTranslatef((float)0.14f, (float)0.35f, (float)-0.2f);
        GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glScalef((float)0.3f, (float)0.3f, (float)0.3f);
        this.renderItem(item);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

