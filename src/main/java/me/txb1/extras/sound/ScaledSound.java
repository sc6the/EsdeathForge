package me.txb1.extras.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;

// ISound wrapper that scales the delegate's volume by a fixed factor (same trick the bundled
// SoundSliders mod uses for its damage/step sliders). Everything else passes through unchanged.
public final class ScaledSound implements ISound {
   private final ISound delegate;
   private final float scale;

   public ScaledSound(ISound delegate, float scale) {
      this.delegate = delegate;
      this.scale = scale;
   }

   @Override
   public ResourceLocation getSoundLocation() {
      return this.delegate.getSoundLocation();
   }

   @Override
   public boolean canRepeat() {
      return this.delegate.canRepeat();
   }

   @Override
   public int getRepeatDelay() {
      return this.delegate.getRepeatDelay();
   }

   @Override
   public float getVolume() {
      return this.delegate.getVolume() * this.scale;
   }

   @Override
   public float getPitch() {
      return this.delegate.getPitch();
   }

   @Override
   public float getXPosF() {
      return this.delegate.getXPosF();
   }

   @Override
   public float getYPosF() {
      return this.delegate.getYPosF();
   }

   @Override
   public float getZPosF() {
      return this.delegate.getZPosF();
   }

   @Override
   public ISound.AttenuationType getAttenuationType() {
      return this.delegate.getAttenuationType();
   }
}
