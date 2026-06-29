package me.txb1.forge;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.player.events.EventRender;
import me.txb1.player.events.EventTick;
import me.txb1.player.events.EventUpdate;
import me.txb1.forge.gui.EsdeathIngameMenu;
import me.txb1.forge.gui.EsdeathMainMenu;
import me.txb1.player.modulesystem.modules.render.crosshair.CrosshairMod;
import me.txb1.player.modulesystem.modules.render.BossbarHider;
import me.txb1.player.modulesystem.modules.player.ProjectL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

// Bridges Forge's tick/render events to the internal EventAPI events the client listens on.
// Replaces the hand-edited fire sites in vanilla:
//   - Minecraft.runTick      -> new EventTick()   (fired here on client tick END)
//   - EntityPlayerSP.onUpdate-> new EventUpdate() (fired here on client tick END)
//   - GuiIngame.renderOverlay-> new EventRender() (fired here on overlay ALL/Post)
// NOTE: EventUpdate was originally per-player-tick; ClientTickEvent is close enough for the
//   HUD/module loop. Modules needing true onUpdate timing get migrated to a Mixin later.
public class ForgeEventBridge {

   private boolean titleSet;
   private me.txb1.player.modulesystem.Module fullbrightModule;

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == TickEvent.Phase.END) {
         if (!this.titleSet) {
            this.titleSet = true;
            try {
               org.lwjgl.opengl.Display.setTitle("EsdeathClient 1.8.9");
            } catch (Throwable ignored) {
            }
         }
         EventManager.call(new EventTick());
         EventManager.call(new EventUpdate());

         // Keep the bundled io.armandukx.fullbright config in sync with the Esdeath module EVERY tick.
         // A one-shot reconcile at boot loses to the bundled mod's own config load (which defaults ON),
         // so fullbright showed until the module was toggled. The bundled EventListener restores gamma
         // when its config.enabled is false, so forcing it false here keeps fullbright off when the
         // module is off.
         if (this.fullbrightModule == null) {
            this.fullbrightModule = me.txb1.EsdeathClient.getInstance().getModuleManager().getModuleByName("Fullbright");
         }
         if (this.fullbrightModule != null) {
            me.txb1.player.modulesystem.modules.render.Fullbright.reconcileBundled(this.fullbrightModule.isEnabled());
         }

         // HardZoom: reset the scroll-adjusted zoom level back to default once the key is released.
         if (!EsdeathForgeMod.HARD_ZOOM.isKeyDown()) {
            me.txb1.player.modulesystem.modules.player.HardZoom.zoom = me.txb1.player.modulesystem.modules.player.HardZoom.DEFAULT;
         }
         // GlintCustomizer chroma: cycle the glint base colour while enabled + chroma on
         if (me.txb1.player.modulesystem.modules.render.GlintCustomizer.active
            && me.powns.glintcolorizer.asm.Colors.chroma) {
            int speed = Math.max(1, me.powns.glintcolorizer.asm.Colors.chromaSpeed);
            int rgb = java.awt.Color.HSBtoRGB(
               (float) (System.currentTimeMillis() % (10000L / speed)) / (10000.0F / speed), 0.8F, 0.8F);
            me.powns.glintcolorizer.asm.Colors.setChromaBase(rgb);
         }
      }
   }

   @SubscribeEvent
   public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
      if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
         EventManager.call(new EventRender());
      }
   }

   // CrosshairMod: suppress the vanilla crosshair when active (the standalone did this via an
   // ASM gate on GuiIngame.showCrosshair()). BossbarHider: suppress the boss health bar.
   @SubscribeEvent
   public void onPreOverlay(RenderGameOverlayEvent.Pre event) {
      if (event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && CrosshairMod.active) {
         event.setCanceled(true);
      } else if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH && BossbarHider.active) {
         event.setCanceled(true);
      }
   }

   // HardZoom: scale the raw FOV while the module is on and the Hard Zoom key is held. Fired after
   // OptiFine's own zoom has been applied to the FOV, so the two multiply and coexist cleanly.
   @SubscribeEvent
   public void onFOV(net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier event) {
      if (me.txb1.player.modulesystem.modules.player.HardZoom.active && EsdeathForgeMod.HARD_ZOOM.isKeyDown()) {
         event.setFOV(event.getFOV() * me.txb1.player.modulesystem.modules.player.HardZoom.zoom);
      }
   }

   // ProjectL: snap body/head rotation prev-values to current on mouse move (no interpolation lag).
   @SubscribeEvent
   public void onMouse(net.minecraftforge.client.event.MouseEvent event) {
      if (ProjectL.active && (event.dx != 0 || event.dy != 0)) {
         net.minecraft.entity.player.EntityPlayer p = Minecraft.getMinecraft().thePlayer;
         if (p != null) {
            p.prevRenderYawOffset = p.renderYawOffset;
            p.prevRotationYawHead = p.rotationYawHead;
            p.prevRotationYaw = p.rotationYaw;
            p.prevRotationPitch = p.rotationPitch;
         }
      }

      // HardZoom: while the module is on and the zoom key is held, the scroll wheel adjusts the zoom
      // level (up = closer, down = wider). Cancel the event so scrolling doesn't change the hotbar slot.
      if (event.dwheel != 0
         && me.txb1.player.modulesystem.modules.player.HardZoom.active
         && EsdeathForgeMod.HARD_ZOOM.isKeyDown()) {
         float step = 0.02F;
         float z = me.txb1.player.modulesystem.modules.player.HardZoom.zoom;
         z += event.dwheel > 0 ? -step : step; // smaller multiplier = more zoom
         me.txb1.player.modulesystem.modules.player.HardZoom.zoom = Math.max(0.05F, Math.min(1.0F, z));
         event.setCanceled(true);
      }
   }

   // Status: draw the local player's status above their nametag (3rd person). Specials.Post fires
   // Local status, drawn above the player's own head in 3rd person. Rendered from RenderPlayerEvent
   // (the per-player render), NOT RenderLivingEvent.Specials — so it is fully INDEPENDENT of the
   // nametag pipeline and the bundled Nametag Editor (which hooks Specials.Pre). The editor's
   // scale/offset/alpha and its canRenderName gating no longer touch the status, and the status shows
   // whether or not the nametag does. x/y/z are the entity render offsets (same space the nametag
   // label uses), GL at the camera origin.
   @SubscribeEvent
   public void onRenderPlayerPost(net.minecraftforge.client.event.RenderPlayerEvent.Post event) {
      // skip while drawing into the voice-chat glow buffer, else the status text gets outlined/glows
      if (dev.mergedvoicechat.gui.SpeakerOutline.renderingOutlinePass) {
         return;
      }
      if (event.entityPlayer == Minecraft.getMinecraft().thePlayer) {
         me.txb1.extras.status.LocalStatus.ensure();
         if (me.txb1.extras.status.LocalStatus.text != null && !me.txb1.extras.status.LocalStatus.text.isEmpty()) {
            me.txb1.extras.status.StatusRenderer.render(
               event.entityPlayer, event.x, event.y, event.z,
               me.txb1.extras.status.LocalStatus.text, me.txb1.extras.status.LocalStatus.size,
               me.txb1.extras.status.LocalStatus.y);
         }
      }
   }

   // open the in-game module menu on the keybind (Right Shift by default)
   @SubscribeEvent
   public void onKeyInput(InputEvent.KeyInputEvent event) {
      if (EsdeathForgeMod.OPEN_GUI.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
         Minecraft.getMinecraft().displayGuiScreen(new EsdeathIngameMenu());
      }
   }

   // SoundSliders: add a button to the vanilla sound-options screen that opens its per-category
   // volume slider GUI (the mod itself co-loads and scales the sounds).
   private static final int BTN_SOUNDSLIDERS = 9201;

   @SubscribeEvent
   public void onSoundOptionsInit(net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post event) {
      if (event.gui instanceof net.minecraft.client.gui.GuiScreenOptionsSounds) {
         event.buttonList.add(new GuiButton(BTN_SOUNDSLIDERS, event.gui.width / 2 - 100, event.gui.height / 6 + 168, "Sound Sliders..."));
      }
   }

   @SubscribeEvent
   public void onSoundOptionsAction(net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event) {
      if (event.gui instanceof net.minecraft.client.gui.GuiScreenOptionsSounds && event.button.id == BTN_SOUNDSLIDERS) {
         // Esdeath sound-sliders screen (Damage / Footsteps / Redstone / Voice Chat) replaces the
         // bundled two-slider GUI.
         Minecraft.getMinecraft().displayGuiScreen(new me.txb1.extras.sound.EsdeathSoundSlidersGui(event.gui));
      }
   }

   // Cape priority: add a top-left button to the gifcapes "Change Cape" screen that cycles which cape
   // source renders (Custom / Optifine / Labymod). The choice drives capemod on/off + the LabyMod hook.
   private static final int BTN_CAPE_PRIORITY = 9202;

   @SubscribeEvent
   public void onCapeGuiInit(net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post event) {
      if (event.gui instanceof me.proxycracked.capemod.gui.GuiChangeCape) {
         event.buttonList.add(new GuiButton(BTN_CAPE_PRIORITY, 8, 8, 130, 20,
            "Priority: " + me.txb1.extras.capes.CapePriority.name()));
      }
   }

   @SubscribeEvent
   public void onCapeGuiAction(net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event) {
      if (event.gui instanceof me.proxycracked.capemod.gui.GuiChangeCape && event.button.id == BTN_CAPE_PRIORITY) {
         me.txb1.extras.capes.CapePriority.cycle();
         event.button.displayString = "Priority: " + me.txb1.extras.capes.CapePriority.name();
      }
   }

   // Swap the vanilla menus for the Esdeath-themed ones (the standalone replaced GuiMainMenu /
   // GuiIngameMenu wholesale; under Forge we re-route them here). Guard against our own classes
   // so re-opening doesn't recurse.
   @SubscribeEvent
   public void onGuiOpen(GuiOpenEvent event) {
      if (event.gui != null && event.gui.getClass() == GuiMainMenu.class) {
         event.gui = new EsdeathMainMenu();
      } else if (event.gui != null && event.gui.getClass() == GuiIngameMenu.class) {
         event.gui = new EsdeathIngameMenu();
      } else if (event.gui != null && event.gui.getClass() == net.minecraft.client.gui.GuiScreenResourcePacks.class) {
         // built-in resource-pack organizer replaces the vanilla list (search/pin/nested folders).
         // currentScreen is the options screen that opened it -> use as the back target.
         event.gui = new me.txb1.forge.gui.EsdeathResourcePackGui(Minecraft.getMinecraft().currentScreen);
      } else if (event.gui != null && event.gui.getClass() == net.minecraft.client.gui.GuiMultiplayer.class) {
         // Esdeath server list (pin + drag-reorder + & colour codes) replaces vanilla multiplayer.
         event.gui = new me.txb1.forge.gui.server.EsdeathServerListGui(new EsdeathMainMenu());
      }
   }
}
