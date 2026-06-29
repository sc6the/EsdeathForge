package me.txb1.extras.sound;

import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Scales the volume of redstone-component sounds by RedstoneVolume. Runs alongside the bundled
// SoundSliders handler (which scales damage/step); we only touch redstone sounds, so the two don't
// fight. Redstone components all play one of a small set of sound events:
//   * buttons / levers / pressure plates / repeaters / comparators / tripwire hooks / dispensers /
//     droppers -> "random.click"
//   * pistons -> "tile.piston.in" / "tile.piston.out"
//   * note blocks (redstone-activated) -> "note.*"
public final class RedstoneSoundHandler {

   @SubscribeEvent
   public void onPlaySound(PlaySoundEvent event) {
      if (event.result == null || event.name == null) {
         return;
      }
      float mult = RedstoneVolume.get();
      if (mult == 1.0F) {
         return;
      }
      String n = event.name.toLowerCase();
      if (n.contains("random.click") || n.startsWith("tile.piston.") || n.startsWith("note.")) {
         event.result = new ScaledSound(event.result, mult);
      }
   }
}
