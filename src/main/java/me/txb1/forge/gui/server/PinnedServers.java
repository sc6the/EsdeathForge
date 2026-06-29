package me.txb1.forge.gui.server;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;

// Pinned servers for the Esdeath server list — pinned servers float to the top. Keyed by the
// server IP (stable across renames). Persisted to .minecraft/EsdeathClient/pinned_servers.txt.
public final class PinnedServers {
   private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/pinned_servers.txt");
   private static final Set<String> PINNED = new LinkedHashSet<String>();

   private PinnedServers() {
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

   public static boolean isPinned(String ip) {
      return ip != null && PINNED.contains(ip);
   }

   public static void toggle(String ip) {
      if (ip == null) {
         return;
      }
      if (!PINNED.remove(ip)) {
         PINNED.add(ip);
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
