package me.txb1.extras.cosmetics.oam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Registry of the data-driven variant options each OAM cosmetic supports (e.g. Konoha "renegade",
// Nine Tails count, Llama colour). The cosmetic editor renders one cycle-button per option; the
// chosen value is stored in CosmeticController and read back by the Cosmetic shim at render time.
public final class CosmeticOptions {

   public static final class Opt {
      public final String key;        // data key the cosmetic reads (getString/getInteger/getFloat)
      public final String label;      // UI label
      public final String[] values;   // stored values ("null" = the cosmetic's default branch)
      public final String[] labels;   // display label per value

      Opt(String key, String label, String[] values, String[] labels) {
         this.key = key;
         this.label = label;
         this.values = values;
         this.labels = labels;
      }
   }

   private static final Map<String, List<Opt>> REG = new HashMap<String, List<Opt>>();

   private CosmeticOptions() {
   }

   private static void add(String cosmetic, Opt opt) {
      List<Opt> l = REG.get(cosmetic.toLowerCase());
      if (l == null) {
         l = new ArrayList<Opt>();
         REG.put(cosmetic.toLowerCase(), l);
      }
      l.add(opt);
   }

   // empty list if the cosmetic has no options
   public static List<Opt> get(String cosmetic) {
      List<Opt> l = REG.get(cosmetic == null ? "" : cosmetic.toLowerCase());
      return l == null ? java.util.Collections.<Opt>emptyList() : l;
   }

   private static Opt toggle(String key, String label, String offVal, String onVal, String offLbl, String onLbl) {
      return new Opt(key, label, new String[]{offVal, onVal}, new String[]{offLbl, onLbl});
   }

   static {
      add("Belt", toggle("items", "Items", "null", "items", "Hidden", "Shown"));
      add("Headset", toggle("mic", "Mic", "null", "mic", "No Mic", "Mic"));
      add("Pumpkin Head", toggle("style", "Style", "null", "lantern", "Pumpkin", "Lantern"));
      add("Snowman", toggle("head", "Head Block", "null", "none", "Pumpkin", "None"));
      add("Guardian Tail", toggle("style", "Style", "null", "elder", "Normal", "Elder"));
      add("Konoha Headband", toggle("style", "Style", "null", "renegade", "Normal", "Renegade"));
      add("Sharingan", new Opt("type", "Eye",
         new String[]{"sharingan", "sasuke", "itachi", "madara", "obito"},
         new String[]{"Sharingan", "Sasuke", "Itachi", "Madara", "Obito"}));
      add("Angry Eyes", toggle("size", "Size", "null", "big", "Small", "Big"));
      add("Llama", new Opt("style", "Colour",
         new String[]{"null", "brown", "creamy", "gray", "white"},
         new String[]{"Default", "Brown", "Creamy", "Gray", "White"}));
      add("Sword in the Leg", toggle("pos", "Side", "null", "right", "Left", "Right"));
      add("Sword in the Leg", new Opt("texture", "Sword",
         new String[]{"diamond", "wood", "stone", "iron", "gold"},
         new String[]{"Diamond", "Wood", "Stone", "Iron", "Gold"}));
      add("Fish", toggle("type", "Type", "null", "cook", "Raw", "Cooked"));
      add("Ender Dragon", toggle("pos", "Side", "null", "right", "Left", "Right"));
      add("Diadem", toggle("skin", "Skin", "null", "overlay", "Normal", "Overlay"));
      add("Wither", toggle("pos", "Side", "null", "right", "Left", "Right"));
      add("Mini Me", toggle("pos", "Side", "null", "right", "Left", "Right"));
      add("Nine Tails", new Opt("tails", "Tails",
         new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
         new String[]{"Auto", "1", "2", "3", "4", "5", "6", "7", "8", "9"}));
   }
}
