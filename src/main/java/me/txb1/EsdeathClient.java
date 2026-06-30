package me.txb1;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import eu.firedata.firedb.FireDB;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.guisettings.GuiSettings;
import me.txb1.player.PlayerObject;
import me.txb1.player.events.EventTick;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.player.modulesystem.ModuleManager;
import me.txb1.player.modulesystem.modules.player.AutoText;
import me.txb1.server.Server;
import me.txb1.utils.EsdeathUtils;
import org.lwjgl.opengl.Display;

public class EsdeathClient {
   private static EsdeathClient instance = new EsdeathClient();
   private Server server;
   private FireDB fireDB;
   public String art;
   private ArrayList<String> players;
   private ModuleManager moduleManager = null;
   private HashMap<String, Integer> valuesInteger;
   public String theme;
   public String ip;
   private HashMap<String, String> valuesString;
   private HashMap<String, PlayerObject> player;
   public String loadingcape;
   public String version;
   private ThreadHelper threadHelper;
   public Integer cat;
   private ArrayList<Category> categories;
   private boolean connected;

   public void onShutdown() {
      this.saveAll();
   }

   // AUTO-SAVE: persist all settings/state now. Called on shutdown AND periodically from
   // onTick so a force-close / crash (e.g. the not-responding window) doesn't lose settings.
   private int saveTick = 0;

   public void saveAll() {
      try {
         if (this.getFireDB() == null) {
            return;
         }
         AnzeigeSettings.save();
         GuiSettings.save();
         this.getFireDB().getDataBase().saveObject("art", this.art);
         ArrayList var1 = new ArrayList();
         this.getModuleManager().getModules().stream().filter(Module::isEnabled).forEach(var1x -> {
            var1.add(var1x.getName());
         });
         this.getFireDB().getDataBase().saveObject("active", var1);
         this.getFireDB().getDataBase().saveObject("theme", this.theme);
         this.getFireDB().getDataBase().saveObject("texts", AutoText.texts);
         this.getFireDB().getDataBase().push();
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public HashMap<String, String> getValuesString() {
      return this.valuesString;
   }

   @EventTarget
   public void onTick(EventTick var1) {
      this.getPlayerMapList().forEach(PlayerObject::onTick);
      if ((++this.saveTick >= 100)) {   // ~5s at 20 tps
         this.saveTick = 0;
         this.saveAll();
      }
   }

   public ThreadHelper getThreadHelper() {
      return this.threadHelper;
   }

   public EsdeathClient() {
      this.version = "4.0.3";
      this.ip = "esdeath.de";
      this.theme = "rainbow";
      this.art = "normal";
      this.connected = false;
      this.valuesString = new HashMap<>();
      this.valuesInteger = new HashMap<>();
      this.players = new ArrayList<>();
      this.categories = new ArrayList<>();
      this.cat = 0;
      this.player = new HashMap<>();
      this.loadingcape = "";
   }

   public void onEnable() {
      EventManager.register(this);

      label56: {
         try {
            Files.createDirectory(Paths.get("esdeath"));
         } catch (IOException var5) {
            System.out.println("Folder already existing");
            break label56;
         }

      }

      this.categories.add(Category.RENDER);
      this.categories.add(Category.PLAYER);
      this.categories.add(Category.VISUAL);
      this.categories.add(Category.UTILS);
      this.moduleManager = new ModuleManager();
      this.threadHelper = new ThreadHelper();

      label51: {
         try {
            this.fireDB = new FireDB("esdeath/", "storage");
         } catch (Exception var4) {
            break label51;
         }

      }

      Display.setTitle("Loading Configuration...");
      AnzeigeSettings.load();
      GuiSettings.load();
      EsdeathUtils.loadDatabase();
      me.txb1.extras.cosmetics.CosmeticController.load();
      me.proxycracked.universalaccountmanager.UniversalAccountManager.init();
      Display.setTitle("Loaded Configuration - Starting up...");
      // OFFLINE PATCH: no DNS probe / version check. Run fully local.
      this.server = new Server(this.ip, 2239);
      this.connected = true;
      this.getValuesString().put("Info", "§7Running offline (local mode).");

      EsdeathUtils.loadBackgroundGif();
   }

   public HashMap<String, PlayerObject> getPlayerMap() {
      return this.player;
   }

   public ArrayList<PlayerObject> getPlayerMapList() {
      ArrayList var1 = new ArrayList();
      Iterator var2 = this.player.entrySet().iterator();

      while ((var2.hasNext())) {
         Entry var3 = (Entry)var2.next();
         var1.add((PlayerObject)var3.getValue());
         
      }

      return var1;
   }

   public ArrayList<String> getPlayers() {
      return this.players;
   }

   public int rainbow(int var1) {
      return EsdeathUtils.getRainbow(var1);
   }

   public String getPrefix() {
      return "§8[§eEsdeathClient§8] §7» §f";
   }

   public Server getServer() {
      return this.server;
   }

   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   public static EsdeathClient getInstance() {
      return instance;
   }

   public FireDB getFireDB() {
      return this.fireDB;
   }

   public PlayerObject getPlayer(String var1) {
      // OFFLINE PATCH: never return null -> create+cache on demand so cosmetics always resolve.
      PlayerObject var2 = this.getPlayerMap().get(var1);
      if (var2 == null) {
         var2 = new PlayerObject(var1, var1);
         this.getPlayerMap().put(var1, var2);
      }
      return var2;
   }

   public HashMap<String, Integer> getValuesInteger() {
      return this.valuesInteger;
   }

   public boolean isConnected() {
      return true;   // OFFLINE PATCH: always "connected" (local mode)
   }

   public ArrayList<Category> getCategories() {
      return this.categories;
   }
}
