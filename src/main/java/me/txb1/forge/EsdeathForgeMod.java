package me.txb1.forge;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.EsdeathClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

// Forge entrypoint for the Esdeath client (formerly a standalone edited-vanilla jar).
// The standalone build started the client from edited Minecraft.java:
//   EventManager.register(new EsdeathClient());  +  EsdeathClient.getInstance().onEnable();
// Here we replicate that from the @Mod lifecycle and bridge Forge ticks/renders to the
// internal DarkMagician6 EventAPI (see ForgeEventBridge).
@Mod(
   modid = EsdeathForgeMod.MODID,
   name = "Esdeath",
   version = "1.0",
   clientSideOnly = true,
   acceptedMinecraftVersions = "[1.8.9]"
)
public class EsdeathForgeMod {
   public static final String MODID = "esdeath";

   // opens the module click-GUI (default: Right Shift)
   public static final KeyBinding OPEN_GUI = new KeyBinding("Open Esdeath Menu", Keyboard.KEY_RSHIFT, "Esdeath");
   // hold to hard-zoom (default: V — kept off OptiFine's C so both zooms can coexist)
   public static final KeyBinding HARD_ZOOM = new KeyBinding("Hard Zoom", Keyboard.KEY_V, "Esdeath");

   @Mod.EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      // EsdeathClient.getInstance() forces the singleton's construction (loads ModuleManager etc.)
      EventManager.register(EsdeathClient.getInstance());
   }

   @Mod.EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ForgeEventBridge());
      // Skinchanger: the deferred Mojang upload fires on ClientDisconnectionFromServerEvent, which is
      // posted on the FML bus (not the Forge bus) — register its handler there.
      net.minecraftforge.fml.common.FMLCommonHandler.instance().bus()
         .register(new me.txb1.extras.skin.SkinDisconnectHandler());
      // Redstone volume: scales redstone-component sounds (alongside the bundled SoundSliders handler).
      MinecraftForge.EVENT_BUS.register(new me.txb1.extras.sound.RedstoneSoundHandler());
      ClientRegistry.registerKeyBinding(OPEN_GUI);
      ClientRegistry.registerKeyBinding(HARD_ZOOM);
      // bundled ParticleCustomiser (colour/scale/opacity) — create + register its mod container and
      // load its config so MixinEntityFX and the Particles editor have live settings.
      ParticleCustomizerHolder.init();
      EsdeathClient.getInstance().onEnable();
   }
}
