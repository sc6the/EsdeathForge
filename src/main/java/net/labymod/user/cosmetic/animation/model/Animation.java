package net.labymod.user.cosmetic.animation.model;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.labymod.user.cosmetic.animation.EnumAnimationMetaType;
import net.labymod.user.cosmetic.animation.EnumCondition;
import net.labymod.user.cosmetic.animation.EnumTrigger;

// Port of LabyMod 3's Animation. Meta/trigger/probability parsing is verbatim; the obfuscated
// per-entity condition evaluation (meetsConditions) is simplified to "always met" for this debloated
// port (idle cosmetic animations generally declare no conditions).
public class Animation {
   private final String name;
   private final Map<String, BoneAnimation> boneAnimation = new HashMap<String, BoneAnimation>();
   private final Map<EnumAnimationMetaType, String> meta = new HashMap<EnumAnimationMetaType, String>();
   private final List<EnumTrigger> triggers = new ArrayList<EnumTrigger>();
   private Integer probability = null;
   private final List<EnumCondition> conditions = new ArrayList<EnumCondition>();

   public Animation(String name) {
      this.name = name;
   }

   public void parseMeta(JsonObject object) {
      for (EnumAnimationMetaType type : EnumAnimationMetaType.values()) {
         if (object.has(type.getKey())) {
            this.meta.put(type, object.get(type.getKey()).getAsString());
         }
      }
   }

   public void parseMeta(String command) {
      if (!command.isEmpty()) {
         String[] args = command.split(" ");
         EnumAnimationMetaType lastMetaType = null;

         for (String argument : args) {
            if (lastMetaType != null) {
               this.meta.put(lastMetaType, argument);
               lastMetaType = null;
            } else if ((lastMetaType = EnumAnimationMetaType.get(argument.replace("-", ""))) == null) {
               return;
            }
         }

         this.parseMetaTrigger();
         this.parseMetaProbability();
         this.parseMetaConditions();
      }
   }

   private void parseMetaTrigger() {
      String value = this.getMetaValue(EnumAnimationMetaType.TRIGGER);
      if (value != null) {
         if (value.contains(",")) {
            for (String triggerEntry : value.split(",")) {
               EnumTrigger trigger = EnumTrigger.getById(triggerEntry.toUpperCase());
               if (trigger != null) {
                  this.triggers.add(trigger);
               }
            }
         } else if (value.equals("*")) {
            Collections.addAll(this.triggers, EnumTrigger.values());
         } else {
            EnumTrigger trigger = EnumTrigger.getById(value.toUpperCase());
            if (trigger != null) {
               this.triggers.add(trigger);
            }
         }
      }
   }

   private void parseMetaProbability() {
      String probability = this.getMetaValue(EnumAnimationMetaType.PROBABILITY);
      if (probability != null) {
         try {
            this.probability = Integer.parseInt(probability);
         } catch (Exception ignored) {
         }
      }
   }

   private void parseMetaConditions() {
      String conditions = this.getMetaValue(EnumAnimationMetaType.CONDITION);
      if (conditions != null) {
         try {
            for (String condition : conditions.split(",")) {
               this.conditions.add(EnumCondition.valueOf(condition.toUpperCase(Locale.ROOT)));
            }
         } catch (Exception ignored) {
         }
      }
   }

   public BoneAnimation getBoneAnimation(String boneName) {
      BoneAnimation boneAnimation = this.boneAnimation.get(boneName);
      if (boneAnimation == null) {
         this.boneAnimation.put(boneName, boneAnimation = new BoneAnimation());
      }

      return boneAnimation;
   }

   public long getLength() {
      long maxLength = 0L;

      for (BoneAnimation boneAnimation : this.boneAnimation.values()) {
         maxLength = Math.max(boneAnimation.getLength(), maxLength);
      }

      return maxLength;
   }

   public String getMetaValue(EnumAnimationMetaType type) {
      return this.meta.get(type);
   }

   public List<EnumCondition> getConditions() {
      return this.conditions;
   }

   public boolean hasTrigger(EnumTrigger trigger) {
      return this.triggers.contains(trigger);
   }

   public Integer getProbability() {
      return this.probability;
   }

   public Map<String, BoneAnimation> getBoneAnimations() {
      return this.boneAnimation;
   }

   public String getName() {
      return this.name;
   }
}
