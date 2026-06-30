package net.labymod.user.cosmetic.geometry.effect.effects;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.effect.util.PhysicMapping;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryPhysic: movement-driven sway. Bone "physics_<strength>_<map>_<mirror>"
// rotates from the player's forward/gravity/strafe motion (meta.*).
public class GeometryPhysic extends GeometryEffect {
   private int version;
   private PhysicMapping mappingX = PhysicMapping.F;
   private PhysicMapping mappingY = PhysicMapping.G;
   private PhysicMapping mappingZ = PhysicMapping.S;
   private double strength = -1.0;
   private boolean mirrorX = false;
   private boolean mirrorY = false;
   private boolean mirrorZ = false;

   public GeometryPhysic(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      this.strength = (double)((float)Integer.parseInt(this.getParameter(0)) / 50.0F);
      String mappingV2 = this.getParameter(1, 3);
      if (mappingV2 != null) {
         this.version = 2;
         if (mappingV2.isEmpty()) {
            return false;
         }

         this.mappingX = PhysicMapping.get(mappingV2.charAt(0));
         this.mappingY = PhysicMapping.get(mappingV2.charAt(1));
         this.mappingZ = PhysicMapping.get(mappingV2.charAt(2));
         String mirror = this.getParameter(2, 3);
         if (mirror == null || mirror.isEmpty()) {
            return false;
         }

         this.mirrorX = mirror.charAt(0) == 'n';
         this.mirrorY = mirror.charAt(1) == 'n';
         this.mirrorZ = mirror.charAt(2) == 'n';
      } else {
         this.version = 1;
         String mapping = this.getParameter(1, 2);
         String mirror = this.getParameter(2, 2);
         if (mapping == null || mapping.isEmpty()) {
            return false;
         }

         this.mappingX = PhysicMapping.get(mapping.charAt(0));
         this.mappingZ = PhysicMapping.get(mapping.charAt(1));
         if (mirror == null || mirror.isEmpty()) {
            return false;
         }

         this.mirrorX = mirror.charAt(0) == 'n';
         this.mirrorZ = mirror.charAt(1) == 'n';
      }

      return true;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      float strength = (float)this.strength;
      float forward = meta.forward * (float)(this.mirrorX ? -1 : 1) * strength;
      float gravity = meta.gravity * (float)(this.mirrorY ? -1 : 1) * strength;
      float strafe = meta.strafe * (float)(this.mirrorZ ? -1 : 1) * strength;
      if (this.version == 1) {
         switch (this.mappingX) {
            case X:
               this.model.rotateAngleX = forward;
               break;
            case Y:
               this.model.rotateAngleY = forward;
               break;
            case Z:
               this.model.rotateAngleZ = forward;
            default:
         }

         switch (this.mappingZ) {
            case X:
               this.model.rotateAngleX = strafe;
               break;
            case Y:
               this.model.rotateAngleY = strafe;
               break;
            case Z:
               this.model.rotateAngleZ = strafe;
            default:
         }
      } else if (this.version == 2) {
         this.model.rotateAngleX = this.mappingX == PhysicMapping.F
            ? forward
            : (this.mappingX == PhysicMapping.G ? gravity : (this.mappingX == PhysicMapping.S ? strafe : 0.0F));
         this.model.rotateAngleY = this.mappingY == PhysicMapping.F
            ? forward
            : (this.mappingY == PhysicMapping.G ? gravity : (this.mappingY == PhysicMapping.S ? strafe : 0.0F));
         this.model.rotateAngleZ = this.mappingZ == PhysicMapping.F
            ? forward
            : (this.mappingZ == PhysicMapping.G ? gravity : (this.mappingZ == PhysicMapping.S ? strafe : 0.0F));
      }
   }

   @Override
   protected int getParametersAmount() {
      return 5;
   }
}
