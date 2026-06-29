package me.txb1.player.events;

import com.darkmagician6.eventapi.events.Event;

public class EventUpdate implements Event {
   public boolean cancellable;

   public void setCancellable(boolean var1) {
      this.cancellable = var1;
   }

   public boolean isCancellable() {
      return this.cancellable;
   }
}
