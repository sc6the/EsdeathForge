package me.txb1.extras.cosmetics.oam;

import java.util.ArrayList;
import java.util.List;
import me.txb1.extras.cosmetics.CosmeticController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

// OAM cosmetic model renderer (clone of vanilla ModelRenderer with display-list baking and the
// OAM animation/colour helpers) — ported verbatim except:
//   * cosmeticName + alpha fields, set by OamCosmeticLayer each frame
//   * renderRGB consults CosmeticController for a user colour override and applies transparency
public class CosmeticModelRenderer {
   public float textureWidth = 64.0f;
   public float textureHeight = 32.0f;
   private int textureOffsetX;
   private int textureOffsetY;
   public float rotationPointX;
   public float rotationPointY;
   public float rotationPointZ;
   public float rotateAngleX;
   public float rotateAngleY;
   public float rotateAngleZ;
   private boolean compiled;
   private int displayList;
   public boolean mirror;
   public boolean showModel = true;
   public boolean isHidden;
   public List<CosmeticModelBox> cubeList = new ArrayList<CosmeticModelBox>();
   public List<CosmeticModelRenderer> childModels;
   public final String boxName;
   private ModelBase baseModel;
   public float offsetX;
   public float offsetY;
   public float offsetZ;
   public Entity entityIn;
   public float ticks_2;
   public float ticks_3;
   public float ticks_4;
   public float ticks_5;
   public float ticks_6;
   public AbstractClientPlayer clientPlayer;
   public float partialTicks;
   public Cosmetic cosmetic;
   // EsdeathForge additions: equip name (for the colour/alpha lookup) + current transparency
   public String cosmeticName;
   public float alpha = 1.0f;

   public CosmeticModelRenderer(ModelBase base, String boxName) {
      this.baseModel = base;
      this.boxName = boxName;
      this.setTextureSize((int) base.textureWidth, (int) base.textureHeight);
   }

   public CosmeticModelRenderer(ModelBase base) {
      this(base, null);
   }

   public CosmeticModelRenderer(ModelBase base, int texU, int texV) {
      this(base);
      this.setTextureOffset(texU, texV);
   }

   public void addChild(CosmeticModelRenderer child) {
      if (this.childModels == null) {
         this.childModels = new ArrayList<CosmeticModelRenderer>();
      }
      this.childModels.add(child);
   }

   public CosmeticModelRenderer setTextureOffset(int u, int v) {
      this.textureOffsetX = u;
      this.textureOffsetY = v;
      return this;
   }

   public CosmeticModelRenderer addBox(String name, float x, float y, float z, int dx, int dy, int dz) {
      name = this.boxName + "." + name;
      TextureOffset textureoffset = this.baseModel.getTextureOffset(name);
      this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
      this.cubeList.add(new CosmeticModelBox(this, this.textureOffsetX, this.textureOffsetY, x, y, z, dx, dy, dz, 0.0f).setBoxName(name));
      return this;
   }

   public CosmeticModelRenderer addBox(float x, float y, float z, int dx, int dy, int dz) {
      this.cubeList.add(new CosmeticModelBox(this, this.textureOffsetX, this.textureOffsetY, x, y, z, dx, dy, dz, 0.0f));
      return this;
   }

   public void addBox(float x, float y, float z, int dx, int dy, int dz, float expand) {
      this.cubeList.add(new CosmeticModelBox(this, this.textureOffsetX, this.textureOffsetY, x, y, z, dx, dy, dz, expand));
   }

   public void setRotationPoint(float x, float y, float z) {
      this.rotationPointX = x;
      this.rotationPointY = y;
      this.rotationPointZ = z;
   }

   public void render(float scale) {
      if (!this.isHidden && this.showModel) {
         if (!this.compiled) {
            this.compileDisplayList(scale);
         }
         GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
         if (this.rotateAngleX == 0.0f && this.rotateAngleY == 0.0f && this.rotateAngleZ == 0.0f) {
            if (this.rotationPointX == 0.0f && this.rotationPointY == 0.0f && this.rotationPointZ == 0.0f) {
               GL11.glCallList(this.displayList);
               if (this.childModels != null) {
                  for (int i = 0; i < this.childModels.size(); ++i) {
                     this.childModels.get(i).render(scale);
                  }
               }
            } else {
               GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
               GL11.glCallList(this.displayList);
               if (this.childModels != null) {
                  for (int i = 0; i < this.childModels.size(); ++i) {
                     this.childModels.get(i).render(scale);
                  }
               }
               GL11.glTranslatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
            }
         } else {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            if (this.rotateAngleZ != 0.0f) {
               GL11.glRotatef(this.rotateAngleZ * 57.295776f, 0.0f, 0.0f, 1.0f);
            }
            if (this.rotateAngleY != 0.0f) {
               GL11.glRotatef(this.rotateAngleY * 57.295776f, 0.0f, 1.0f, 0.0f);
            }
            if (this.rotateAngleX != 0.0f) {
               GL11.glRotatef(this.rotateAngleX * 57.295776f, 1.0f, 0.0f, 0.0f);
            }
            GL11.glCallList(this.displayList);
            if (this.childModels != null) {
               for (int i = 0; i < this.childModels.size(); ++i) {
                  this.childModels.get(i).render(scale);
               }
            }
            GL11.glPopMatrix();
         }
         GL11.glTranslatef(-this.offsetX, -this.offsetY, -this.offsetZ);
      }
   }

   public void renderWithRotation(float scale) {
      if (!this.isHidden && this.showModel) {
         if (!this.compiled) {
            this.compileDisplayList(scale);
         }
         GL11.glPushMatrix();
         GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         if (this.rotateAngleY != 0.0f) {
            GL11.glRotatef(this.rotateAngleY * 57.295776f, 0.0f, 1.0f, 0.0f);
         }
         if (this.rotateAngleX != 0.0f) {
            GL11.glRotatef(this.rotateAngleX * 57.295776f, 1.0f, 0.0f, 0.0f);
         }
         if (this.rotateAngleZ != 0.0f) {
            GL11.glRotatef(this.rotateAngleZ * 57.295776f, 0.0f, 0.0f, 1.0f);
         }
         GL11.glCallList(this.displayList);
         GL11.glPopMatrix();
      }
   }

   public void postRender(float scale) {
      if (!this.isHidden && this.showModel) {
         if (!this.compiled) {
            this.compileDisplayList(scale);
         }
         if (this.rotateAngleX == 0.0f && this.rotateAngleY == 0.0f && this.rotateAngleZ == 0.0f) {
            if (this.rotationPointX != 0.0f || this.rotationPointY != 0.0f || this.rotationPointZ != 0.0f) {
               GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            }
         } else {
            GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            if (this.rotateAngleZ != 0.0f) {
               GL11.glRotatef(this.rotateAngleZ * 57.295776f, 0.0f, 0.0f, 1.0f);
            }
            if (this.rotateAngleY != 0.0f) {
               GL11.glRotatef(this.rotateAngleY * 57.295776f, 0.0f, 1.0f, 0.0f);
            }
            if (this.rotateAngleX != 0.0f) {
               GL11.glRotatef(this.rotateAngleX * 57.295776f, 1.0f, 0.0f, 0.0f);
            }
         }
      }
   }

   private void compileDisplayList(float scale) {
      this.displayList = GLAllocation.generateDisplayLists(1);
      GL11.glNewList(this.displayList, 4864);
      WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
      for (int i = 0; i < this.cubeList.size(); ++i) {
         this.cubeList.get(i).render(worldrenderer, scale);
      }
      GL11.glEndList();
      this.compiled = true;
   }

   public CosmeticModelRenderer setTextureSize(int w, int h) {
      this.textureWidth = w;
      this.textureHeight = h;
      return this;
   }

   public void renderCosmetic(Entity entityIn, float ticks_2, float ticks_3, float ticks_4, float ticks_5, float ticks_6, float scale, AbstractClientPlayer clientPlayer, float partialTicks, Cosmetic cosmetic) {
      this.ticks_2 = ticks_2;
      this.ticks_3 = ticks_3;
      this.ticks_4 = ticks_4;
      this.ticks_5 = ticks_5;
      this.ticks_6 = ticks_6;
      this.entityIn = entityIn;
      this.clientPlayer = clientPlayer;
      this.partialTicks = partialTicks;
      this.cosmetic = cosmetic;
      this.render(scale);
   }

   public void setRotation(CosmeticModelRenderer modelRenderer, float xRotation, float yRotation, float zRotation) {
      modelRenderer.rotateAngleX = xRotation;
      modelRenderer.rotateAngleY = yRotation;
      modelRenderer.rotateAngleZ = zRotation;
   }

   public void renderMultiColor() {
      int ixx = this.entityIn.ticksExisted / 25 + this.entityIn.getEntityId();
      int axx = EnumDyeColor.values().length;
      int kxx = ixx % axx;
      int lxx = (ixx + 1) % axx;
      float f1xx = ((float) (this.entityIn.ticksExisted % 25) + 0.7f) / 25.0f;
      float[] afloat1xx = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(kxx));
      float[] afloat2xx = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(lxx));
      float multicolor1 = afloat1xx[0] * (1.0f - f1xx) + afloat2xx[0] * f1xx;
      float multicolor2 = afloat1xx[1] * (1.0f - f1xx) + afloat2xx[1] * f1xx;
      float multicolor3 = afloat1xx[2] * (1.0f - f1xx) + afloat2xx[2] * f1xx;
      if (this.alpha < 1.0f) {
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
      }
      GL11.glColor4f(multicolor1, multicolor2, multicolor3, this.alpha);
   }

   public void setHeadRotations() {
      if (this.entityIn.isSneaking()) {
         GL11.glTranslatef(0.0f, 0.2625f, 0.0f);
      }
      GL11.glRotatef(this.ticks_5, 0.0f, 1.0f, 0.0f);
      GL11.glRotatef(this.ticks_6, 1.0f, 0.0f, 0.0f);
   }

   // ResourceLocation cache — these binds run per cosmetic per frame; building a new ResourceLocation
   // (string parse + validation) each time churned the GC. Reuse one instance per texture name.
   private static final java.util.HashMap<String, ResourceLocation> RL_CACHE = new java.util.HashMap<String, ResourceLocation>();

   private static ResourceLocation rl(String path) {
      ResourceLocation r = RL_CACHE.get(path);
      if (r == null) {
         r = new ResourceLocation(path);
         RL_CACHE.put(path, r);
      }
      return r;
   }

   public void bindCosmeticTexture(String name) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(rl("oldanimations/cosmetics/" + name));
   }

   public void bindEntityTexture(String name) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(rl("textures/entity/" + name));
   }

   public void bindSkinTexture() {
      Minecraft.getMinecraft().getTextureManager().bindTexture(((AbstractClientPlayer) this.entityIn).getLocationSkin());
   }

   // honor a user colour override (CosmeticController, 0-255) and the cosmetic's transparency;
   // otherwise use the cosmetic's built-in OAM default (which is expressed over /100).
   public void renderRGB(String key, int defaultRed, int defaultGreen, int defaultBlue) {
      if (this.cosmeticName != null && CosmeticController.isMulticolorCosmetic(this.cosmeticName)) {
         this.renderMultiColor();
      } else if (this.cosmeticName != null && CosmeticController.hasColor(this.cosmeticName)) {
         int[] c = CosmeticController.getColor(this.cosmeticName);
         this.applyColor(c[0] / 255.0f, c[1] / 255.0f, c[2] / 255.0f);
      } else if (this.cosmetic.isMulticolor(key)) {
         this.renderMultiColor();
      } else if (this.cosmetic.getRGB(key).length == 3) {
         int[] color = this.cosmetic.getRGB(key);
         this.applyColor(color[0] / 100.0f, color[1] / 100.0f, color[2] / 100.0f);
      } else {
         this.applyColor(defaultRed / 100.0f, defaultGreen / 100.0f, defaultBlue / 100.0f);
      }
   }

   private void applyColor(float r, float g, float b) {
      if (this.alpha < 1.0f) {
         GL11.glEnable(3042); // GL_BLEND
         GL11.glBlendFunc(770, 771); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA
      }
      GL11.glColor4f(r, g, b, this.alpha);
   }

   public void runAnimationProcess() {
      if (this.entityIn != null) {
         this.performAnimation();
      }
   }

   public void performAnimation() {
   }

   public CosmeticInfo getInformation() {
      return this.getClass().getAnnotation(CosmeticInfo.class);
   }

   public void renderItem(ItemStack item) {
      Minecraft.getMinecraft().getItemRenderer().renderItem((EntityLivingBase) this.entityIn, item, ItemCameraTransforms.TransformType.NONE);
   }

   public void renderText(String text, int x, int y, int color) {
      Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
   }

   public int getStringWidth(String text) {
      return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
   }

   public boolean isHelmetEquipped() {
      EntityPlayer player = (EntityPlayer) this.entityIn;
      return player.getCurrentArmor(3) != null && player.getCurrentArmor(3).getUnlocalizedName().contains("helmet");
   }

   public boolean isSneaking() {
      return this.entityIn.isSneaking();
   }
}
