package me.txb1;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class MessageHelper {
   static HashMap<String, String> eng = new HashMap<>();
   static HashMap<String, String> ger = new HashMap<>();

   public static void sendMessage(String var0) {
      try {
         if ((Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().contains("DE"))) {
            Minecraft.getMinecraft()
               .thePlayer
               .addChatMessage(
                  new ChatComponentText(String.valueOf(new StringBuilder().append(EsdeathClient.getInstance().getPrefix()).append(ger.get(var0.toLowerCase()))))
               );
            
         } else {
            Minecraft.getMinecraft()
               .thePlayer
               .addChatMessage(
                  new ChatComponentText(String.valueOf(new StringBuilder().append(EsdeathClient.getInstance().getPrefix()).append(eng.get(var0.toLowerCase()))))
               );
         }
      } catch (Exception var2) {
         return;
      }

               ;
      
   }

   static {
      ger.put("buy", "Du kannst Ränge(Status etc)/Cosmetics/Capes kaufen. Info : §ewww.Esdeath.de");
      eng.put("buy", "You can buy Ranks / Cosmetics / Capes. Info : §ewww.Esdeath.de");
      eng.put("server", "§cThe Server is currently offline :/");
      ger.put("server", "§cDer Server ist aktuell offline :/");
      eng.put("status.update", "You updated your Status");
      ger.put("status.update", "Du hast deinen Status geupdated");
      eng.put("cape.update", "You updated your Cape.");
      ger.put("cape.update", "Du hast deine Cape geupdated.");
      ger.put("autotext.create", "Erstelle einen Autotext mit\n§c > §a.autotext key Text\n§cBSP : §a.autotext f /ping");
      eng.put("autotext.create", "Create an Autotext:\n§c > §a.autotext key Text\n§cExample : §a.autotext f /ping");
      ger.put("autotext.no", "Du hast keine Autotexte");
      eng.put("autotext.no", "You dont have any autotexts");
      ger.put("autotext.write", "Schreibe nun deinen Text");
      eng.put("autotext.write", "Now write your text");
      ger.put("autotext.finish", "Du hast einen Autotext erstellt");
      eng.put("autotext.finish", "You created an autotext");
      ger.put("autotext.remove", "Du hast einen Autotext entfernt");
      eng.put("autotext.remove", "You removed an autotext");
      ger.put("mlghelper", "Du musst Sneaken um die Springen / laufen zu sehen");
      eng.put("mlghelper", "You have to sneak to see if you should run / jump");
      eng.put("answer.true", "You linked your Accounts, please refresh your Profile Page on www.EsdeathClient.de/profile");
      ger.put("answer.true", "Du hast deine Konten aktualisiert, aktualisiere bitte deine Profilseite unter www.EsdeathClient.de/profile");
      eng.put("answer.false", "Something went wrong :(");
      ger.put("answer.false", "Es ist etwas schiefgelaufen :(");
      eng.put("link.unlink", "You unlinked possible Accounts");
      ger.put("link.unlink", "Du hast alle möglichen gelinkten Accounts entfernt.");
      eng.put("capes.animated.amount", "Your Animated Capes : ");
      ger.put("capes.animated.amount", "Deine Animierten Capes : ");
      eng.put("loading", "The Client ist still loading, please wait a sec");
      ger.put("loading", "Der Client läd gerade, bitte warte einen Moment");
      eng.put("translate.list", "list");
      ger.put("translate.list", "Liste");
      eng.put("translate.create", "create");
      ger.put("translate.create", "Erstellen");
      eng.put("translate.remove", "remove");
      ger.put("translate.remove", "Entfernen");
   }

   public static String getMessage(String var0) {
      try {
         return (Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().contains("DE"))
            ? ger.get(var0)
            : eng.get(var0);
      } catch (Exception var3) {
         return "";
      }
   }

}
