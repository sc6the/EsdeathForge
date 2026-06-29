package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import me.txb1.extras.autotext.AutoTextKeyGui;
import me.txb1.extras.autotext.AutoTextRemoveKeyGui;
import me.txb1.player.events.EventTick;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.utils.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class AutoText extends Module {
   public static Integer prepareKey = null;
   private static ArrayList<Integer> pressed = new ArrayList<>();
   public static ArrayList<Text> texts = new ArrayList<>();

   public static boolean chat(String var0) {
      if (((prepareKey) != null)) {
         System.out.println(texts.size());
         texts.add(new Text(prepareKey, var0));
         System.out.println(texts.size());
         prepareKey = null;
         MessageHelper.sendMessage("autotext.finish");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   @EventTarget
   public void OnTick(EventTick var1) {
      if (((mc.currentScreen) == null) && !((mc.thePlayer) == null)) {
         texts.forEach(var0 -> {
            if ((Keyboard.isKeyDown(var0.getKey()))) {
               if (!(pressed.contains(var0.getKey()))) {
                  pressed.add(var0.getKey());
                  mc.thePlayer.sendChatMessage(var0.getText());
                  
               }
            } else if ((pressed.contains(var0.getKey()))) {
               pressed.remove(Integer.valueOf(var0.getKey()));
            }
         });
      }
   }

   public AutoText() {
      super("AutoText", "AutoText", Category.PLAYER, true);
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      Gui.drawRect(var3 + 5, var4 + 30, var3 + 70, var4 + 45, Color.GREEN.getRGB());
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(MessageHelper.getMessage("translate.list"), var3 + 12, var4 + 30 + 4, Color.BLACK.getRGB());
      Gui.drawRect(var3 + 5, var4 + 50, var3 + 70, var4 + 65, Color.GREEN.getRGB());
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(MessageHelper.getMessage("translate.create"), var3 + 12, var4 + 30 + 4 + 20, Color.BLACK.getRGB());
      Gui.drawRect(var3 + 5, var4 + 70, var3 + 70, var4 + 85, Color.GREEN.getRGB());
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(MessageHelper.getMessage("translate.remove"), var3 + 12, var4 + 30 + 4 + 40, Color.BLACK.getRGB());
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
      Gui.drawRect(var3 + 5, var4 + 90, var3 + 70, var4 + 105, Color.GREEN.getRGB());
      String var10001 = "Clear";
      int var10002 = var3 + 12;
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(var10001, var10002, var4 + 30 + 4 + 60, Color.BLACK.getRGB());
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (((var1) > (var4 + 5)) && ((var1) < (var4 + 70)) && ((var2) > (var5 + 30)) && ((var2) < (var5 + 45))) {
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
         mc.displayGuiScreen(null);
         if ((texts.isEmpty())) {
            MessageHelper.sendMessage("autotext.no");
            return;
         }

         mc.thePlayer
            .addChatMessage(new ChatComponentText(String.valueOf(new StringBuilder().append(EsdeathClient.getInstance().getPrefix()).append(" §6AutoText"))));
         Iterator var8 = texts.iterator();

         while ((var8.hasNext())) {
            Text var9 = (Text)var8.next();
            mc.thePlayer
               .addChatMessage(
                  new ChatComponentText(
                     String.valueOf(
                        new StringBuilder().append(" §8[§7").append(Keyboard.getKeyName(var9.getKey())).append("§8] §7» §f").append(var9.getText())
                     )
                  )
               );
            
         }

      } else if (((var1) > (var4 + 5)) && ((var1) < (var4 + 70)) && ((var2) > (var5 + 50)) && ((var2) < (var5 + 65))) {
         mc.displayGuiScreen(new AutoTextKeyGui());
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
         
      } else if (((var1) > (var4 + 5)) && ((var1) < (var4 + 70)) && ((var2) > (var5 + 70)) && ((var2) < (var5 + 85))) {
         mc.displayGuiScreen(new AutoTextRemoveKeyGui());
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
         
      } else if (((var1) > (var4 + 5)) && ((var1) < (var4 + 70)) && ((var2) > (var5 + 90)) && ((var2) < (var5 + 105))) {
         texts.clear();
         mc.thePlayer
            .addChatMessage(new ChatComponentText(String.valueOf(new StringBuilder().append(EsdeathClient.getInstance().getPrefix()).append("Cleared"))));
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
      }

      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

}
