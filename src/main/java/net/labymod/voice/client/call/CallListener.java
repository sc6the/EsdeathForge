package net.labymod.voice.client.call;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface CallListener {
   void onCallRequest(UUID var1, Consumer<Boolean> var2);

   void onCallRequestRetracted(UUID var1);

   void onCallJoin(UUID var1);

   void onCallLeave(UUID var1);

   void onCallEnd(Set<UUID> var1);

   void onCallFeatureAvailable(UUID var1);
}
