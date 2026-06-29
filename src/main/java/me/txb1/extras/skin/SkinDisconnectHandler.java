package me.txb1.extras.skin;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

// Flushes a deferred Mojang skin upload (queued by the in-game Skinchanger) the moment the client
// leaves a server. ClientDisconnectionFromServerEvent is posted on the FML event bus, so this is
// registered there (separate from ForgeEventBridge, which lives on the Forge bus and would
// double-fire ticks if it also joined the FML bus).
public final class SkinDisconnectHandler {

   @SubscribeEvent
   public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
      if (PendingSkin.hasPending()) {
         PendingSkin.flushAsync();
      }
   }
}
