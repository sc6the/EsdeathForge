package net.labymod.user.cosmetic.animation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.labymod.user.cosmetic.animation.model.Animation;
import net.labymod.user.cosmetic.animation.model.BoneAnimation;
import net.labymod.user.cosmetic.animation.model.Keyframes;

// Port of LabyMod 3's AnimationLoader (Bedrock animation.json -> Animation/BoneAnimation/Keyframes).
// load()/extractKeyframes/pushVector are verbatim; the obfuscated, random/probability-weighted
// getAnimationByTrigger is replaced by getIdleAnimation() (first IDLE-triggered animation, else the
// first one) since this debloated port just loops a cosmetic's idle pose.
public class AnimationLoader {
   private static final Gson GSON = new Gson();
   private final JsonObject tree;
   private final Map<String, Animation> animations = new HashMap<String, Animation>();

   public AnimationLoader(JsonObject tree) throws IOException {
      this.tree = tree;
   }

   public AnimationLoader(String json) throws IOException {
      this((JsonObject) GSON.fromJson(json, JsonObject.class));
   }

   public AnimationLoader load() {
      if (this.tree != null && this.tree.has("animations") && this.tree.get("animations").isJsonObject()) {
         JsonObject animations = this.tree.get("animations").getAsJsonObject();

         for (Entry<String, JsonElement> animationEntry : animations.entrySet()) {
            String animationName = animationEntry.getKey();
            if (!animationEntry.getValue().isJsonObject()) {
               continue;
            }
            JsonObject animationObject = animationEntry.getValue().getAsJsonObject();
            if (animationObject.has("bones")) {
               JsonObject bones = animationObject.get("bones").getAsJsonObject();
               Animation animation = new Animation(animationName);
               if (animationObject.has("anim_time_update")) {
                  animation.parseMeta(animationObject.get("anim_time_update").getAsString());
               } else {
                  animation.parseMeta(animationObject);
               }

               for (Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                  if (!boneEntry.getValue().isJsonObject()) {
                     continue;
                  }
                  String boneName = boneEntry.getKey();
                  JsonObject bone = boneEntry.getValue().getAsJsonObject();
                  BoneAnimation boneAnimation = animation.getBoneAnimation(boneName);
                  this.extractKeyframes(boneAnimation.rotation, bone, "rotation");
                  this.extractKeyframes(boneAnimation.position, bone, "position");
                  this.extractKeyframes(boneAnimation.scale, bone, "scale");
               }

               this.animations.put(animationName, animation);
            }
         }
      }

      return this;
   }

   private void extractKeyframes(Keyframes storage, JsonObject bone, String key) {
      if (bone.has(key)) {
         JsonElement type = bone.get(key);
         if (type.isJsonArray()) {
            this.pushVector(storage, 0L, type.getAsJsonArray(), false);
         } else if (type.isJsonObject()) {
            JsonObject object = type.getAsJsonObject();
            if (object.has("post")) {
               boolean smooth = object.has("lerp_mode") && object.get("lerp_mode").getAsString().equals("catmullrom");
               this.pushVector(storage, 0L, object.get("post").getAsJsonArray(), smooth);
            } else {
               for (Entry<String, JsonElement> entry : object.entrySet()) {
                  long offset = (long)(Double.parseDouble(entry.getKey()) * 1000.0);
                  JsonElement value = entry.getValue();
                  JsonArray array;
                  boolean smooth = false;
                  if (value.isJsonArray()) {
                     array = value.getAsJsonArray();
                  } else {
                     JsonObject entryObject = value.getAsJsonObject();
                     array = entryObject.get("post").getAsJsonArray();
                     smooth = entryObject.has("lerp_mode") && entryObject.get("lerp_mode").getAsString().equals("catmullrom");
                  }

                  this.pushVector(storage, offset, array, smooth);
               }
            }
         } else {
            float value = type.getAsFloat();
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(value));
            array.add(new JsonPrimitive(value));
            array.add(new JsonPrimitive(value));
            this.pushVector(storage, 0L, array, false);
         }
      }
   }

   private void pushVector(Keyframes storage, long offset, JsonArray arrayVector, boolean smooth) {
      float x = arrayVector.get(0).getAsFloat();
      float y = arrayVector.get(1).getAsFloat();
      float z = arrayVector.get(2).getAsFloat();
      storage.add(offset, (double)x, (double)y, (double)z, smooth);
   }

   public Animation getAnimation(String name) {
      return this.animations.get(name);
   }

   // The animation to loop for a resting cosmetic: prefer one triggered by IDLE, else the first.
   public Animation getIdleAnimation() {
      Animation first = null;
      for (Animation animation : this.animations.values()) {
         if (first == null) {
            first = animation;
         }
         if (animation.hasTrigger(EnumTrigger.IDLE)) {
            return animation;
         }
      }
      return first;
   }

   public boolean isEmpty() {
      return this.animations.isEmpty();
   }
}
