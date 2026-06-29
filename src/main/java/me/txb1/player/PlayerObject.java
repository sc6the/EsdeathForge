package me.txb1.player;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.utils.EsdeathUtils;
import me.txb1.utils.ImageFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

public class PlayerObject {
   private final String name;
   private final ArrayList<BufferedImage> imagesLoading;
   private int cur;
   private final String uuid;
   private String status;
   private Integer delay;
   private final ArrayList<String> cosmetics;
   private String ascii;
   private String rank;
   private final ArrayList<ResourceLocation> capes;
   private int capeDelay;
   private Integer remDelay;

   // OFFLINE: the dead backend grants every player all cosmetics + a rank symbol, so without
   // this guard everyone shows all cosmetics and the ☯ nametag icon. Only the local player is
   // a "real" Esdeath user here, so cosmetics/icon are shown for self only.
   private boolean isLocal() {
      try {
         return Minecraft.getMinecraft().thePlayer != null
            && this.uuid.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
      } catch (Exception var2) {
         return false;
      }
   }

   public String getAscii() {
      // don't show the Esdeath nametag symbol on other players
      return this.isLocal() ? this.ascii : "";
   }

   public void setStatus(String var1) {
      this.status = var1;
   }

   public PlayerObject(String var1, String var2) {
      this.ascii = "";
      this.cosmetics = new ArrayList<>();
      this.capes = new ArrayList<>();
      this.cur = 0;
      this.imagesLoading = new ArrayList<>();
      this.delay = 0;
      this.remDelay = 0;
      this.capeDelay = 0;
      this.uuid = var1;
      this.name = var2;
      System.out.println(String.valueOf(new StringBuilder().append("New Player: ").append(this.getName())));
      this.loadRank();
      this.loadStatus();
      this.loadCosmetics();
      this.loadCape();
   }

   public String getRank() {
      return this.rank;
   }

   public void loadCape() {
      // OFFLINE PATCH: cape images came from the (dead) esdeathclient.de host. No-op.
      this.delay = 0;
      this.cur = 0;
      this.getCapes().clear();
      this.getImagesLoading().clear();
   }

   public ArrayList<BufferedImage> getImagesLoading() {
      return this.imagesLoading;
   }

   public String getName() {
      return this.name;
   }

   public String getUUID() {
      return this.uuid;
   }

   public void setRank(String var1) {
      this.rank = var1;
   }

   public void loadStatus() {
      EsdeathClient.getInstance()
         .getThreadHelper()
         .getThreadpool()
         .submit(
            () -> {
               this.setStatus(EsdeathClient.getInstance().getServer().getStatus(this.getUUID()).replaceAll("&", "§"));
               System.out
                  .println(
                     String.valueOf(new StringBuilder().append("status loaded : ").append(this.getName()).append(" > ").append(this.getStatus()))
                  );
            }
         );
   }

   public void unloadTextures() {
      this.getCapes().forEach(var0 -> Minecraft.getMinecraft().getTextureManager().deleteTexture(var0));
   }

   public String getStatus() {
      return this.status;
   }

   public ResourceLocation getCurrentCape() {
      return (this.imagesLoading.isEmpty()) ? this.getCapes().get(this.cur) : this.getCapes().get(this.getCapes().size() - 1);
   }

   public void setAscii(String var1) {
      this.ascii = var1;
   }

   public void onTick() {
      if (((this.getCapes().size()) > (1)) && ((this.imagesLoading.size()) == 0)) {
         this.capeDelay = this.capeDelay + 1;
         if (((this.capeDelay) > (20 / this.getCapes().size()))) {
            this.capeDelay = 0;
            this.cur = this.cur + 1;

            try {
               this.getCapes().get(this.cur);
            } catch (Exception var5) {
               this.cur = 0;
               return;
            }

         }
      } else if (((this.imagesLoading.size())) != 0) {
         if (!(EsdeathClient.getInstance().loadingcape.equalsIgnoreCase(this.getName()))) {
            if (!(EsdeathClient.getInstance().loadingcape.equalsIgnoreCase(""))) {
               return;
            }

            EsdeathClient.getInstance().loadingcape = this.getName();
            System.out.println(String.valueOf(new StringBuilder().append("Preparing ").append(this.getName())));
         }

         Integer var1 = this.delay;
         Integer var2 = this.delay = this.delay + 1;
         if (((this.delay) > (40))) {
            this.delay = 0;
                           if (!(this.imagesLoading.isEmpty())) {
                  BufferedImage var6 = this.imagesLoading.get(0);
                  this.imagesLoading.remove(0);
                  if (((this.imagesLoading.size())) != 0) {
                     this.delay = 32;
                     
                  } else {
                     EsdeathClient.getInstance().loadingcape = "";
                  }

                  final ResourceLocation var7 = new ResourceLocation(
                     String.valueOf(new StringBuilder().append("animatedcape/").append(this.getUUID()).append("/").append(this.getCapes().size()))
                  );
                  IImageBuffer var3 = new IImageBuffer() {

                     @Override
                     public BufferedImage parseUserSkin(BufferedImage var1) {
                        return var1;
                     }

                     @Override
                     public void skinAvailable() {
                        PlayerObject.this.getCapes().add(var7);
                        System.out.println("added cape");
                     }

                  };
                  ThreadDownloadImageData var4 = new ThreadDownloadImageData(null, null, null, var3);
                  Minecraft.getMinecraft().getTextureManager().loadTexture(var7, var4);
               }
            
         }
      }
   }

   public void loadCosmetics() {
      // only the local player resolves cosmetics; others wear nothing here
      if (!this.isLocal()) {
         return;
      }
      EsdeathClient.getInstance()
         .getThreadHelper()
         .getThreadpool()
         .submit(
            () -> {
               // Offline the website "owns everything" grant meant every named cosmetic was added to
               // the equipped list, so all of them rendered at once (e.g. the Esdeath Tail always
               // showed next to the LabyMod Wolf Tail). Drive the equipped list off the actual GUI
               // equip state (isActive) instead, so only equipped cosmetics render.
               CosmeticController.getCosmetics().forEach(var1 -> {
                  if (CosmeticController.isActive(var1.toLowerCase())) {
                     this.getCosmetics().add(var1.toLowerCase());
                  }
               });
               System.out
                  .println(
                     String.valueOf(
                        new StringBuilder().append("cosmetics loaded : ").append(this.getName()).append(" > ").append(this.getCosmetics().size())
                     )
                  );
            }
         );
   }

   public void loadRank() {
      try {
         EsdeathClient.getInstance()
            .getThreadHelper()
            .getThreadpool()
            .submit(
               () -> {
                  String var1 = EsdeathClient.getInstance().getServer().getRank(this.getUUID());
                  if (!(var1.equalsIgnoreCase("Chef"))
                     && !(var1.equalsIgnoreCase("Premium"))
                     && !(var1.equalsIgnoreCase("Epic"))) {
                     this.setRank("Spieler");
                  } else {
                     this.setRank(var1);
                     
                  }

                  System.out
                     .println(
                        String.valueOf(new StringBuilder().append("rank loaded : ").append(this.getName()).append(" > ").append(this.getRank()))
                     );
                  if ((this.rank.equalsIgnoreCase("Chef"))) {
                     this.setAscii("§c☯");
                     
                  } else if ((this.rank.equalsIgnoreCase("Epic"))) {
                     this.setAscii("§b☯");
                     
                  } else if ((this.rank.equalsIgnoreCase("Premium"))) {
                     this.setAscii("§6☯");
                     
                  } else {
                     this.setAscii("§f☯");
                  }
               }
            );
      } catch (Exception var2) {
         return;
      }

               ;
      
   }

   public ArrayList<ResourceLocation> getCapes() {
      return this.capes;
   }

   public ArrayList<String> getCosmetics() {
      // OFFLINE PATCH: the local player owns every cosmetic for free, but only the ones
      // toggled ON locally are "equipped" -> return the active set (CosmeticController).
      // Other players get nothing (don't render cosmetics on people who aren't really wearing them).
      if (this.isLocal()) {
         return CosmeticController.getActive();
      }

      return new ArrayList<>();
   }

}
