package me.txb1.player.events;

import com.darkmagician6.eventapi.events.Cancellable;
import com.darkmagician6.eventapi.events.Event;

public class EventClickMouse implements Cancellable, Event {

   @Override
   public void setCancelled(boolean var1) {
   }

   @Override
   public boolean isCancelled() {
      return false;
   }
}
