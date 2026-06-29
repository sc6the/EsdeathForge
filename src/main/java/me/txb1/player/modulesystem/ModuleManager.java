package me.txb1.player.modulesystem;

import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.player.modulesystem.modules.player.AutoText;
import me.txb1.player.modulesystem.modules.player.Connector;
import me.txb1.player.modulesystem.modules.player.HardZoom;
import me.txb1.player.modulesystem.modules.player.Hitbox;
import me.txb1.player.modulesystem.modules.player.ItemAnimation;
import me.txb1.player.modulesystem.modules.player.MLGHelper;
import me.txb1.player.modulesystem.modules.player.MotionBlur;
import me.txb1.player.modulesystem.modules.player.PingTag;
import me.txb1.player.modulesystem.modules.player.ToggleSprint;
import me.txb1.player.modulesystem.modules.render.BlockOverlayMod;
import me.txb1.player.modulesystem.modules.render.CleanChat;
import me.txb1.player.modulesystem.modules.render.NametagEditorMod;
import me.txb1.player.modulesystem.modules.visuals.Statsify;
import me.txb1.player.modulesystem.modules.render.CleanScoreboard;
import me.txb1.player.modulesystem.modules.render.Particles;
import me.txb1.player.modulesystem.modules.render.ArmorHider;
import me.txb1.player.modulesystem.modules.render.BossbarHider;
import me.txb1.player.modulesystem.modules.render.OlyOptifineCapes;
import me.txb1.player.modulesystem.modules.render.ScoreBoard;
import me.txb1.player.modulesystem.modules.visuals.CPS;
import me.txb1.player.modulesystem.modules.visuals.FPS;
import me.txb1.player.modulesystem.modules.visuals.HUD;
import me.txb1.player.modulesystem.modules.visuals.KeyStrokes;
import me.txb1.player.modulesystem.modules.visuals.OnlinePlayers;
import me.txb1.player.modulesystem.modules.visuals.Ping;
import me.txb1.player.modulesystem.modules.visuals.Plains;
import me.txb1.player.modulesystem.modules.visuals.ReachDisplay;
import me.txb1.player.modulesystem.modules.visuals.ViewPlayer;
import me.txb1.player.modulesystem.modules.visuals.XYZ;

public class ModuleManager {
   public ArrayList<Module> modules = new ArrayList<>();

   public Module getModule(Class<? extends Module> var1) {
      Iterator var2 = this.modules.iterator();

      while ((var2.hasNext())) {
         Module var3 = (Module)var2.next();
         if (((var3.getClass()) == (var1))) {
            return var3;
         }

      }

      return null;
   }

   public ModuleManager() {
      this.addModule(new HUD());
      this.addModule(new CPS());
      this.addModule(new Plains());
      this.addModule(new XYZ());
      this.addModule(new ItemAnimation());
      this.addModule(new ToggleSprint());
      this.addModule(new Particles());
      this.addModule(new ArmorHider());
      this.addModule(new BossbarHider());
      this.addModule(new me.txb1.player.modulesystem.modules.render.InventorySnow());
      this.addModule(new me.txb1.player.modulesystem.modules.render.AdvancedCulling());
      this.addModule(new me.txb1.player.modulesystem.modules.player.ProjectL());
      this.addModule(new Hitbox());
      this.addModule(new ReachDisplay());
      this.addModule(new me.txb1.player.modulesystem.modules.visuals.PotionHUD());
      this.addModule(new HardZoom());
      this.addModule(new me.txb1.player.modulesystem.modules.player.ContainerStrafe());
      this.addModule(new me.txb1.player.modulesystem.modules.render.PerspectiveModule());
      this.addModule(new me.txb1.player.modulesystem.modules.render.GlintCustomizer());
      this.addModule(new Connector());
      this.addModule(new AutoText());
      this.addModule(new BlockOverlayMod());
      this.addModule(new PingTag());
      this.addModule(new MLGHelper());
      this.addModule(new CleanChat());
      this.addModule(new FPS());
      this.addModule(new CleanScoreboard());
      this.addModule(new KeyStrokes());
      this.addModule(new ScoreBoard());
      this.addModule(new Ping());
      this.addModule(new OnlinePlayers());
      this.addModule(new ViewPlayer());
      this.addModule(new MotionBlur());
      this.addModule(new NametagEditorMod());
      this.addModule(new Statsify());
      this.addModule(new OlyOptifineCapes());
      this.addModule(new me.txb1.player.modulesystem.modules.render.Fullbright());
      this.addModule(new me.txb1.player.modulesystem.modules.render.TimeChanger());
      this.addModule(new me.txb1.player.modulesystem.modules.player.NoHurtCam());
      this.addModule(new me.txb1.player.modulesystem.modules.render.crosshair.CrosshairMod());
      this.addModule(new me.txb1.player.modulesystem.modules.render.DamageTint());
      this.addModule(new me.txb1.player.modulesystem.modules.player.VoiceChatModule());
      System.out.println("LOADED MODULES");
   }

   public void addModule(Module var1) {
      this.modules.add(var1);
   }

   public ArrayList<Module> getModules() {
      return this.modules;
   }

   public Module getModuleByName(String var1) {
      Iterator var2 = this.modules.iterator();

      while ((var2.hasNext())) {
         Module var3 = (Module)var2.next();
         // match by internal name, display name (modules whose label differs from their name, e.g.
         // Biome/Plains, are looked up by the label the GUI shows), or toString.
         if ((var3.getName().trim().equalsIgnoreCase(var1))
            || (var3.getDisplayName() != null && var3.getDisplayName().trim().equalsIgnoreCase(var1.trim()))
            || (var3.toString().trim().equalsIgnoreCase(var1.trim()))) {
            return var3;
         }

      }

      return null;
   }
}
