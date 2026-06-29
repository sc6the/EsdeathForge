package me.txb1.forge.gui.art;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

// Stores the menu-background gallery: the bundled Esdeath backgrounds plus any custom images the
// user imports. The selection + custom files are persisted on disk under
// .minecraft/EsdeathClient/ (selection in art_selected.txt, customs in backgrounds/), so nothing
// depends on FireDB. EsdeathMainMenu renders getSelectedTexture(); EsdeathArtGui edits the list.
public final class BackgroundManager {
   private static BackgroundManager INSTANCE;

   public static BackgroundManager get() {
      if (INSTANCE == null) {
         INSTANCE = new BackgroundManager();
      }
      return INSTANCE;
   }

   // a single gallery entry
   public static final class Entry {
      public final String id;           // built-in: "MainBackground.jpg"; custom: file name
      public final boolean builtin;
      public final File file;           // null for built-ins
      ResourceLocation cached;          // lazily uploaded texture

      Entry(String id, boolean builtin, File file) {
         this.id = id;
         this.builtin = builtin;
         this.file = file;
      }
   }

   // built-in backgrounds shipped in assets/minecraft/EsdeathClient/
   private static final String[] BUILTINS = { "MainBackground.jpg", "MainBackground2.jpg", "bg.gif" };
   private static final String DEFAULT = "MainBackground.jpg";

   private final File dir;
   private final File customDir;
   private final File selFile;
   private final File favFile;
   private final File randFile;
   private final Map<String, ResourceLocation> uploaded = new LinkedHashMap<String, ResourceLocation>();
   private final Set<String> favourites = new LinkedHashSet<String>();
   private String selected;
   private boolean randomMode;

   private BackgroundManager() {
      this.dir = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient");
      this.customDir = new File(this.dir, "backgrounds");
      this.selFile = new File(this.dir, "art_selected.txt");
      this.favFile = new File(this.dir, "art_favourites.txt");
      this.randFile = new File(this.dir, "art_random.txt");
      this.customDir.mkdirs();
      this.selected = readSelected();
      readFavourites();
      this.randomMode = this.randFile.exists();
   }

   // Random-background mode: when on, the home screen re-rolls a random favourite each time it opens
   // (EsdeathMainMenu.initGui calls maybeRollRandom). Persisted by the presence of art_random.txt.
   public boolean isRandomMode() {
      return this.randomMode;
   }

   public void setRandomMode(boolean on) {
      this.randomMode = on;
      try {
         if (on) {
            Files.write(this.randFile.toPath(), new byte[0]);
         } else if (this.randFile.exists()) {
            this.randFile.delete();
         }
      } catch (IOException ignored) {
      }
   }

   // Called when the home screen opens: if random mode is on and there are favourites, pick a new one.
   public void maybeRollRandom() {
      if (this.randomMode) {
         selectRandomFavourite();
      }
   }

   private String readSelected() {
      try {
         if (this.selFile.exists()) {
            String s = new String(Files.readAllBytes(this.selFile.toPath()), "UTF-8").trim();
            if (!s.isEmpty()) {
               return s;
            }
         }
      } catch (IOException ignored) {
      }
      return DEFAULT;
   }

   public List<Entry> list() {
      List<Entry> out = new ArrayList<Entry>();
      for (String b : BUILTINS) {
         out.add(new Entry(b, true, null));
      }
      File[] files = this.customDir.listFiles();
      if (files != null) {
         for (File f : files) {
            String n = f.getName().toLowerCase();
            if (f.isFile() && (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif"))) {
               out.add(new Entry(f.getName(), false, f));
            }
         }
      }
      return out;
   }

   public String getSelected() {
      return this.selected;
   }

   public void setSelected(String id) {
      this.selected = id;
      try {
         Files.write(this.selFile.toPath(), id.getBytes("UTF-8"));
      } catch (IOException ignored) {
      }
   }

   // copy a user-chosen image into the custom dir; returns its entry id (file name)
   public String addCustom(File src) throws IOException {
      File dst = new File(this.customDir, src.getName());
      // avoid clobbering: suffix if needed
      int i = 1;
      String base = src.getName();
      while (dst.exists()) {
         int dot = base.lastIndexOf('.');
         String name = dot < 0 ? base + "_" + i : base.substring(0, dot) + "_" + i + base.substring(dot);
         dst = new File(this.customDir, name);
         i++;
      }
      Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return dst.getName();
   }

   // delete a custom background (built-ins can't be removed). If it was selected, fall back to default.
   public boolean delete(Entry e) {
      if (e == null || e.builtin || e.file == null) {
         return false;
      }
      boolean ok = e.file.delete();
      this.uploaded.remove(e.id);
      if (this.favourites.remove(e.id)) {
         saveFavourites();
      }
      if (e.id.equals(this.selected)) {
         setSelected(DEFAULT);
      }
      return ok;
   }

   // ---- favourites ----
   private void readFavourites() {
      try {
         if (this.favFile.exists()) {
            for (String line : Files.readAllLines(this.favFile.toPath())) {
               String s = line.trim();
               if (!s.isEmpty()) {
                  this.favourites.add(s);
               }
            }
         }
      } catch (IOException ignored) {
      }
   }

   private void saveFavourites() {
      try {
         StringBuilder sb = new StringBuilder();
         for (String s : this.favourites) {
            sb.append(s).append('\n');
         }
         Files.write(this.favFile.toPath(), sb.toString().getBytes("UTF-8"));
      } catch (IOException ignored) {
      }
   }

   public boolean isFavourite(String id) {
      return this.favourites.contains(id);
   }

   public void toggleFavourite(String id) {
      if (!this.favourites.remove(id)) {
         this.favourites.add(id);
      }
      saveFavourites();
   }

   public List<Entry> favouriteEntries() {
      List<Entry> out = new ArrayList<Entry>();
      for (Entry e : list()) {
         if (this.favourites.contains(e.id)) {
            out.add(e);
         }
      }
      return out;
   }

   // Select a random favourite (that still exists). Returns the chosen id, or null if none.
   public String selectRandomFavourite() {
      List<Entry> favs = favouriteEntries();
      if (favs.isEmpty()) {
         return null;
      }
      Entry e = favs.get((int) (Math.random() * favs.size()));
      setSelected(e.id);
      return e.id;
   }

   // ---- rename (custom only) ----
   // Renames a custom background's file on disk (extension preserved). Updates the selection and
   // favourites if they pointed at the old id. Returns the new id, or null on failure / built-in.
   public String rename(Entry e, String newBaseName) {
      if (e == null || e.builtin || e.file == null || newBaseName == null) {
         return null;
      }
      String clean = newBaseName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
      if (clean.isEmpty()) {
         return null;
      }
      String oldName = e.file.getName();
      int dot = oldName.lastIndexOf('.');
      String ext = dot < 0 ? "" : oldName.substring(dot);
      if (clean.toLowerCase().endsWith(ext.toLowerCase())) {
         ext = ""; // user already typed the extension
      }
      File dst = new File(this.customDir, clean + ext);
      int i = 1;
      while (dst.exists() && !dst.equals(e.file)) {
         dst = new File(this.customDir, clean + "_" + i + ext);
         i++;
      }
      try {
         Files.move(e.file.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ex) {
         return null;
      }
      String newId = dst.getName();
      ResourceLocation rl = this.uploaded.remove(oldName);
      if (rl != null) {
         this.uploaded.put(newId, rl);
      }
      if (this.favourites.remove(oldName)) {
         this.favourites.add(newId);
         saveFavourites();
      }
      if (oldName.equals(this.selected)) {
         setSelected(newId);
      }
      return newId;
   }

   // Save downloaded image bytes as a new custom background (e.g. a Wallhaven import). Returns its id.
   public String addDownloaded(byte[] bytes, String suggestedName) throws IOException {
      String base = (suggestedName == null || suggestedName.trim().isEmpty()) ? "wallpaper" : suggestedName.trim();
      base = base.replaceAll("[\\\\/:*?\"<>|]", "_");
      if (!base.toLowerCase().matches(".*\\.(png|jpe?g|gif)$")) {
         base = base + ".png";
      }
      File dst = new File(this.customDir, base);
      int i = 1;
      while (dst.exists()) {
         int dot = base.lastIndexOf('.');
         String name = dot < 0 ? base + "_" + i : base.substring(0, dot) + "_" + i + base.substring(dot);
         dst = new File(this.customDir, name);
         i++;
      }
      Files.write(dst.toPath(), bytes);
      return dst.getName();
   }

   // texture for a gallery entry (built-in = bundled ResourceLocation, custom = uploaded dynamic)
   public ResourceLocation texture(Entry e) {
      if (e.builtin) {
         if (e.cached == null) {
            e.cached = new ResourceLocation("EsdeathClient/" + e.id);
         }
         return e.cached;
      }
      ResourceLocation rl = this.uploaded.get(e.id);
      if (rl != null) {
         return rl;
      }
      try {
         BufferedImage img = ImageIO.read(e.file);
         if (img == null) {
            return null;
         }
         DynamicTexture tex = new DynamicTexture(img);
         rl = Minecraft.getMinecraft().getTextureManager()
            .getDynamicTextureLocation("esd_bg_" + e.id.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase(), tex);
         this.uploaded.put(e.id, rl);
         return rl;
      } catch (Throwable t) {
         return null;
      }
   }

   // texture for whatever is currently selected (falls back to the default built-in)
   public ResourceLocation getSelectedTexture() {
      for (Entry e : list()) {
         if (e.id.equals(this.selected)) {
            ResourceLocation rl = texture(e);
            if (rl != null) {
               return rl;
            }
         }
      }
      return new ResourceLocation("EsdeathClient/" + DEFAULT);
   }
}
