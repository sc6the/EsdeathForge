package me.txb1.forge.gui;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;

// Pinned resource packs for the Esdeath resource-pack organizer. A pinned pack floats to the top of
// the available column. Persisted to .minecraft/EsdeathClient/pinned_packs.txt (one pack name per
// line) so pins survive restarts. Names are the repository's getResourcePackName() (= the file name).
public final class PinnedPacks {
   private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/pinned_packs.txt");
   private static final Set<String> PINNED = new LinkedHashSet<String>();

   private PinnedPacks() {
   }

   static {
      try {
         if (FILE.isFile()) {
            for (String line : Files.readAllLines(FILE.toPath(), StandardCharsets.UTF_8)) {
               String t = line.trim();
               if (!t.isEmpty()) {
                  PINNED.add(t);
               }
            }
         }
      } catch (Throwable ignored) {
      }
   }

   public static boolean isPinned(String name) {
      return PINNED.contains(name);
   }

   public static void toggle(String name) {
      if (!PINNED.remove(name)) {
         PINNED.add(name);
      }
      save();
   }

   private static void save() {
      try {
         FILE.getParentFile().mkdirs();
         StringBuilder sb = new StringBuilder();
         for (String n : PINNED) {
            sb.append(n).append('\n');
         }
         Files.write(FILE.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
      } catch (Throwable ignored) {
      }
   }
}
