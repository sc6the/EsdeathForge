package me.txb1.extras.cosmetics.oam.cosmetics;

import me.txb1.extras.cosmetics.oam.CosmeticInfo;
import me.txb1.extras.cosmetics.oam.CosmeticType;
import me.txb1.extras.cosmetics.oam.CosmeticModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@CosmeticInfo(id=15, height=0.2, activatergb=false, displayname="Pumpkin Head", type=CosmeticType.LIMITED)
public class Cosmetic015
extends CosmeticModelRenderer {
    public Cosmetic015(ModelBase base) {
        super(base);
    }

    @Override
    public void render(float scale) {
        GL11.glPushMatrix();
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.setHeadRotations();
        GL11.glScalef((float)0.4f, (float)-0.4f, (float)0.4f);
        GL11.glTranslatef((float)0.0f, (float)1.7f, (float)0.0f);
        ItemStack block = null;
        block = this.cosmetic.getString("style").equalsIgnoreCase("lantern") ? new ItemStack(Blocks.lit_pumpkin) : new ItemStack(Blocks.pumpkin);
        Minecraft.getMinecraft().getItemRenderer().renderItem((EntityLivingBase)this.entityIn, block, ItemCameraTransforms.TransformType.HEAD);
        GL11.glPopMatrix();
        super.render(scale);
    }
}

