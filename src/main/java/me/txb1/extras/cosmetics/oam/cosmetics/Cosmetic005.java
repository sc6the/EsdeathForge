package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=5, height=0.0, activatergb=true, displayname="Belt", type=CosmeticType.DEFAULT)
public class Cosmetic005
extends CosmeticModelRenderer {
    CosmeticModelRenderer belt;
    CosmeticModelRenderer beltClamp;

    public Cosmetic005(ModelBase base) {
        super(base);
        this.belt = new CosmeticModelRenderer(base, 64, 64);
        this.beltClamp = new CosmeticModelRenderer(base, 64, 64);
        this.belt.addBox(3.5f, 10.0f, -2.0f, 1, 2, 4);
        this.belt.addBox(-4.5f, 10.0f, -2.0f, 1, 2, 4);
        this.belt.addBox(-4.5f, 10.0f, -2.5f, 9, 2, 1);
        this.belt.addBox(-4.5f, 10.0f, 1.5f, 9, 2, 1);
        this.beltClamp.addBox(-1.0f, 10.5f, -3.0f, 2, 1, 1);
    }

    @Override
    public void render(float p_78785_1_) {
        GL11.glPushMatrix();
        if (this.isSneaking()) {
            GL11.glRotatef((float)30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.118f);
        }
        this.renderRGB("color", 64, 32, 16);
        this.bindCosmeticTexture("resources.png");
        this.belt.render(p_78785_1_);
        this.renderRGB("colorClamp", 100, 100, 100);
        this.beltClamp.render(p_78785_1_);
        if (this.cosmetic.getString("items").equals("items")) {
            GL11.glScalef((float)0.13f, (float)0.13f, (float)0.13f);
            GL11.glTranslatef((float)0.8f, (float)5.5f, (float)-1.2f);
            GL11.glRotatef((float)180.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glPushMatrix();
            for (int i = 0; i < 3; ++i) {
                ItemStack item = new ItemStack(Items.glass_bottle);
                this.renderItem(item);
                item = new ItemStack((Item)Items.potionitem);
                this.renderItem(item);
                GL11.glTranslatef((float)0.5f, (float)0.0f, (float)0.0f);
            }
            GL11.glPopMatrix();
            GL11.glTranslatef((float)-1.6f, (float)0.0f, (float)0.0f);
            ItemStack item = new ItemStack(Items.bowl);
            this.renderItem(item);
            GL11.glTranslatef((float)-0.6f, (float)0.3f, (float)0.0f);
            ItemStack item1 = new ItemStack(Item.getItemById((int)39));
            this.renderItem(item1);
            GL11.glTranslatef((float)-0.5f, (float)0.0f, (float)0.0f);
            ItemStack item2 = new ItemStack(Item.getItemById((int)40));
            this.renderItem(item2);
        }
        GL11.glPopMatrix();
        super.render(p_78785_1_);
    }
}

