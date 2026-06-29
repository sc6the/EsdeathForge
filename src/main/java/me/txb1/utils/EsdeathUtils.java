package me.txb1.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.modules.player.AutoText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EsdeathUtils {

   public static void loadBackgroundGif() {
   }

   public static ImageFrame[] readGif(InputStream var0) throws IOException {
      ArrayList var1 = new ArrayList();
      ImageReader var2 = ImageIO.getImageReadersByFormatName("gif").next();
      var2.setInput(ImageIO.createImageInputStream(var0));
      int var3 = 0;
      int var4 = 0;
      int var5 = -1;
      int var6 = -1;
      IIOMetadata var7 = var2.getStreamMetadata();
      Color var8 = null;
      if (((var7) != null)) {
         IIOMetadataNode var9 = (IIOMetadataNode)var7.getAsTree(var7.getNativeMetadataFormatName());
         NodeList var10 = var9.getElementsByTagName("GlobalColorTable");
         NodeList var11 = var9.getElementsByTagName("LogicalScreenDescriptor");
         if (((var11) != null) && ((var11.getLength()) > 0)) {
            IIOMetadataNode var12 = (IIOMetadataNode)var11.item(0);
            if (((var12) != null)) {
               var5 = Integer.parseInt(var12.getAttribute("logicalScreenWidth"));
               var6 = Integer.parseInt(var12.getAttribute("logicalScreenHeight"));
            }
         }

         if (((var10) != null) && ((var10.getLength()) > 0)) {
            IIOMetadataNode var28 = (IIOMetadataNode)var10.item(0);
            if (((var28) != null)) {
               String var13 = var28.getAttribute("backgroundColorIndex");
               IIOMetadataNode var14 = (IIOMetadataNode)var28.getFirstChild();

               while (((var14) != null)) {
                  if ((var14.getAttribute("index").equals(var13))) {
                     int var15 = Integer.parseInt(var14.getAttribute("red"));
                     int var16 = Integer.parseInt(var14.getAttribute("green"));
                     int var17 = Integer.parseInt(var14.getAttribute("blue"));
                     var8 = new Color(var15, var16, var17);
                     
                     break;
                  }

                  var14 = (IIOMetadataNode)var14.getNextSibling();
                  
               }
            }
         }
      }

      BufferedImage var25 = null;
      int var26 = 0;
      int var27 = 0;

      do {
         BufferedImage var29;
         try {
            var29 = var2.read(var27);
         } catch (IndexOutOfBoundsException var24) {

            var2.dispose();
            return (ImageFrame[])(var1.toArray(new ImageFrame[var1.size()]));
         }

         if (!((var5) != (-1)) || ((var6) == (-1))) {
            var5 = var29.getWidth();
            var6 = var29.getHeight();
         }

         IIOMetadataNode var30 = (IIOMetadataNode)var2.getImageMetadata(var27).getAsTree("javax_imageio_gif_image_1.0");
         IIOMetadataNode var31 = (IIOMetadataNode)var30.getElementsByTagName("GraphicControlExtension").item(0);
         NodeList var32 = var30.getChildNodes();
         int var33 = Integer.valueOf(var31.getAttribute("delayTime"));
         String var34 = var31.getAttribute("disposalMethod");
         if (((var25) == null)) {
            var25 = new BufferedImage(var5, var6, 2);
            var25.createGraphics().setColor(var8);
            var25.createGraphics().fillRect(0, 0, var25.getWidth(), var25.getHeight());
            int var10000;
            if (((var29.getWidth()) == (var5)) && ((var29.getHeight()) == (var6))) {
               var10000 = 1;
               
            } else {
               var10000 = 0;
            }

            var26 = var10000;
            var25.createGraphics().drawImage(var29, 0, 0, null);
            
         } else {
            int var18 = 0;
            int var19 = 0;
            int var20 = 0;

            while (((var20) < (var32.getLength()))) {
               Node var21 = var32.item(var20);
               if ((var21.getNodeName().equals("ImageDescriptor"))) {
                  NamedNodeMap var22 = var21.getAttributes();
                  var18 = Integer.valueOf(var22.getNamedItem("imageLeftPosition").getNodeValue());
                  var19 = Integer.valueOf(var22.getNamedItem("imageTopPosition").getNodeValue());
               }

               var20++;
               
            }

            if ((var34.equals("restoreToPrevious"))) {
               BufferedImage var37 = null;
               int var39 = var27 - 1;

               while (((var39) >= 0)) {
                  if (!(((ImageFrame)var1.get(var39)).getDisposal().equals("restoreToPrevious")) || ((var27) == 0)) {
                     var37 = ((ImageFrame)var1.get(var39)).getImage();
                     
                     break;
                  }

                  var39--;
                  
               }

               ColorModel var40 = var37.getColorModel();
               boolean var42 = var37.isAlphaPremultiplied();
               WritableRaster var23 = var37.copyData(null);
               var25 = new BufferedImage(var40, var23, var42, null);
               
            } else if ((var34.equals("restoreToBackgroundColor")) && ((var8) != null) && (((var26) == 0) || ((var27) > (1)))) {
               var25.createGraphics()
                  .fillRect(var3, var4, ((ImageFrame)var1.get(var27 - 1)).getWidth(), ((ImageFrame)var1.get(var27 - 1)).getHeight());
            }

            var25.createGraphics().drawImage(var29, var18, var19, null);
            var3 = var18;
            var4 = var19;
         }

         ColorModel var36 = var25.getColorModel();
         boolean var38 = var25.isAlphaPremultiplied();
         WritableRaster var41 = var25.copyData(null);
         BufferedImage var35 = new BufferedImage(var36, var41, var38, null);
         var1.add(new ImageFrame(var35, var33, var34, var29.getWidth(), var29.getHeight()));
         var25.flush();
         var27++;
      } while (1 > -1);


   }

   public static void loadDatabase() {
      if (((EsdeathClient.getInstance().getFireDB().getDataBase().getObject("active")) != null)) {
         ((ArrayList)EsdeathClient.getInstance().getFireDB().getDataBase().getObject("active")).stream().filter(var0 -> {
            int var10000;
            if (((EsdeathClient.getInstance().getModuleManager().getModuleByName((String)var0)) != null)) {
               var10000 = 1;
               
            } else {
               var10000 = 0;
            }

            return (var10000 != 0);
         }).forEach(var0 -> {
            // Enable idempotently rather than blind toggle(): modules that wrap a bundled mod
            // (Perspective, ContainerStrafe, VoiceChat) report isEnabled() from the bundled mod's
            // own persisted config, which defaults ON — a blind toggle would flip them OFF on every
            // restart. Only toggle when actually off so the saved state is honoured for all modules.
            me.txb1.player.modulesystem.Module mod = EsdeathClient.getInstance().getModuleManager().getModuleByName((String) var0);
            if (!mod.isEnabled()) {
               mod.toggle();
            }
         });
      }

      if (((EsdeathClient.getInstance().getFireDB().getDataBase().getObject("theme")) != null)) {
         EsdeathClient.getInstance().theme = (String)EsdeathClient.getInstance().getFireDB().getDataBase().getObject("theme");
      }

      if (((EsdeathClient.getInstance().getFireDB().getDataBase().getObject("texts")) != null)) {
         AutoText.texts = (ArrayList<Text>)EsdeathClient.getInstance().getFireDB().getDataBase().getObject("texts");
      }

      if (((EsdeathClient.getInstance().getFireDB().getDataBase().getObject("art")) != null)) {
         EsdeathClient.getInstance().art = (String)EsdeathClient.getInstance().getFireDB().getDataBase().getObject("art");
      }

      if (!(EsdeathClient.getInstance().getModuleManager().getModuleByName("Connector").isEnabled())) {
         EsdeathClient.getInstance().getModuleManager().getModuleByName("Connector").toggle();
      }

      // Push the (restored) Fullbright module state into the bundled fullbright mod so it doesn't
      // force fullbright at boot from its own persisted ON config.
      me.txb1.player.modulesystem.Module fb = EsdeathClient.getInstance().getModuleManager().getModuleByName("Fullbright");
      if (fb != null) {
         me.txb1.player.modulesystem.modules.render.Fullbright.reconcileBundled(fb.isEnabled());
      }

      EsdeathClient.getInstance().getThreadHelper().getThreadpool().submit(() -> System.out.println(EsdeathClient.getInstance().getServer().getAllCapes()));
   }

   public static BufferedImage parseCape(BufferedImage var0) {
      int var1 = 64;
      int var2 = 32;
      int var3 = var0.getWidth();
      int var4 = var0.getHeight();

      while (!((var1) >= (var3)) || ((var2) < (var4))) {
         var1 *= 2;
         var2 *= 2;
         
      }

      BufferedImage var7 = new BufferedImage(var1, var2, 2);
      Graphics var5 = var7.getGraphics();
      var5.drawImage(var0, 0, 0, (ImageObserver)null);
      var5.dispose();
      return var7;
   }

   public static void drawEntityOnScreen(int var0, int var1, int var2, float var3, float var4, EntityLivingBase var5) {
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var0, (float)var1, 50.0F);
      GlStateManager.scale((float)(-var2), (float)var2, (float)var2);
      GlStateManager.color(100.0F, 100.0F, 100.0F, 100.0F);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      float var6 = var5.renderYawOffset;
      float var7 = var5.rotationYaw;
      float var8 = var5.rotationPitch;
      float var9 = var5.prevRotationYawHead;
      float var10 = var5.rotationYawHead;
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-((float)Math.atan((double)(var4 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
      var5.renderYawOffset = (float)Math.atan((double)(var3 / 40.0F)) * 20.0F;
      var5.rotationYaw = (float)Math.atan((double)(var3 / 40.0F)) * 40.0F;
      var5.rotationPitch = -((float)Math.atan((double)(var4 / 40.0F))) * 20.0F;
      var5.rotationYawHead = var5.rotationYaw;
      var5.prevRotationYawHead = var5.rotationYaw;
      GlStateManager.translate(0.0F, 0.0F, 0.0F);
      RenderManager var11 = Minecraft.getMinecraft().getRenderManager();
      var11.setPlayerViewY(180.0F);
      var11.setRenderShadow(false);
      var11.renderEntityWithPosYaw(var5, 0.0, 0.0, 0.0, 0.0F, 1.0F);
      var11.setRenderShadow(true);
      var5.renderYawOffset = var6;
      var5.rotationYaw = var7;
      var5.rotationPitch = var8;
      var5.prevRotationYawHead = var9;
      var5.rotationYawHead = var10;
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   public static void changeArt() {
      if ((EsdeathClient.getInstance().art.equalsIgnoreCase("normal"))) {
         EsdeathClient.getInstance().art = "rainbow";
         
      } else if ((EsdeathClient.getInstance().art.equalsIgnoreCase("rainbow"))) {
         EsdeathClient.getInstance().art = "absurd";
         
      } else if ((EsdeathClient.getInstance().art.equalsIgnoreCase("absurd"))) {
         EsdeathClient.getInstance().art = "normal";
      }
   }

   public static boolean isOnline(String var0) {
      // OFFLINE PATCH: treat every named player as "online" so cosmetics resolve locally.
      return var0 != null && !var0.equalsIgnoreCase("");
   }

   public static void changeTheme() {
      // Theme buttons now open the custom theme settings screen (presets + custom hex).
      Minecraft.getMinecraft().displayGuiScreen(new me.txb1.extras.settings.theme.ThemeGui(Minecraft.getMinecraft().currentScreen));
   }

   public static String getArt() {
      return EsdeathClient.getInstance().art;
   }

   public static void renderBackground() {
      Minecraft var0 = Minecraft.getMinecraft();
      ScaledResolution var1 = new ScaledResolution(var0);
      var0.getTextureManager().bindTexture(new ResourceLocation("EsdeathClient/MainBackground.jpg"));
      Gui.drawModalRectWithCustomSizedTexture(
         0, 0, 0.0F, 0.0F, var1.getScaledWidth(), var1.getScaledHeight(), (float)var1.getScaledWidth(), (float)var1.getScaledHeight()
      );
      Gui.drawRect(0, 0, var0.displayWidth, var0.displayHeight, 1073741824);
   }

   // cache for a custom hex theme: Color.decode allocates + parses, but the theme only changes when
   // the user edits it, so decode once and reuse. null verdict = not a valid hex (fall back to rainbow).
   private static String rainbowThemeKey;
   private static Integer rainbowThemeColor;

   public static int getRainbow(int var0) {
      String theme = EsdeathClient.getInstance().theme;
      if (theme.equalsIgnoreCase("rainbow")) {
         return rainbowAt(var0);
      } else if (theme.equalsIgnoreCase("grey")) {
         return 0xFFC0C0C0; // LIGHT_GRAY
      } else if (theme.equalsIgnoreCase("black")) {
         return 0xFF000000;
      } else if (theme.equalsIgnoreCase("red")) {
         return 0xFFFF0000;
      } else if (theme.equalsIgnoreCase("weiß")) {
         return 0xFFFFFFFF;
      } else {
         // custom hex — decode once, cache until the theme string changes
         if (!theme.equals(rainbowThemeKey)) {
            rainbowThemeKey = theme;
            try {
               rainbowThemeColor = Color.decode(theme).getRGB();
            } catch (Exception e) {
               rainbowThemeColor = null;
            }
         }
         return rainbowThemeColor != null ? rainbowThemeColor : rainbowAt(var0);
      }
   }

   // animated rainbow colour without allocating a Color each call (Color.HSBtoRGB returns ARGB int).
   private static int rainbowAt(int var0) {
      double h = Math.ceil((double) (System.currentTimeMillis() + (long) var0) / 20.0) % 360.0;
      return Color.HSBtoRGB((float) (h / 360.0), 0.8F, 0.7F);
   }

}
