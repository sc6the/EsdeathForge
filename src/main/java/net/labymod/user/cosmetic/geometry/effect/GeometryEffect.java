package net.labymod.user.cosmetic.geometry.effect;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LabyMod 3's GeometryEffect base. A bone's name encodes the effect + parameters
// split on '_' (e.g. "layer_<uuid>", "color_0", "physics_50_xz_pn"); subclasses parse args[1..] and
// mutate the bone's GeometryModelRenderer each frame in apply(...).
public abstract class GeometryEffect {
   protected final GeometryModelRenderer model;
   protected final String[] args;

   public GeometryEffect(String name, GeometryModelRenderer model) {
      this.args = name.split("_");
      this.model = model;
   }

   public GeometryEffect load() {
      return this.args.length >= this.getParametersAmount() ? (this.parse() ? this : null) : null;
   }

   protected abstract boolean parse();

   protected abstract int getParametersAmount();

   protected String getParameter(int index) {
      return this.args[index + 1];
   }

   protected boolean hasParameter(int index) {
      return this.args.length > index + 1;
   }

   protected String getParameter(int index, int requiredLength) {
      String value = this.args[index + 1];
      return value.length() == requiredLength ? value : null;
   }

   public boolean onCubeAdd(GeometryModelRenderer target, float x, float y, float z, int sizeX, int sizeY, int sizeZ, float inflate, boolean mirror) {
      return true;
   }

   public abstract void apply(RemoteData var1, MetaEffectFrameParameter var2);

   public GeometryModelRenderer getModel() {
      return this.model;
   }

   public String[] getArgs() {
      return this.args;
   }
}
