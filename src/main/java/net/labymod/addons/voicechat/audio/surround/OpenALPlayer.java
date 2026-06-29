package net.labymod.addons.voicechat.audio.surround;

import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OpenALPlayer {
   private final SurroundManager surroundManager;

   public OpenALPlayer(SurroundManager surroundManager) {
      this.surroundManager = surroundManager;
      ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
      executorService.scheduleAtFixedRate(() -> {
         for (Entry<UUID, UserStream> entry : surroundManager.getUserStreams().entrySet()) {
            entry.getValue().play();
         }
      }, 0L, 5L, TimeUnit.MILLISECONDS);
   }
}
