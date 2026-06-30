package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;
import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;

public class PacketNotAllowed extends Packet {
   private String reason;
   private long until;

   public PacketNotAllowed(String reason, long until) {
      this.reason = reason;
      this.until = until;
   }

   public PacketNotAllowed() {
   }

   @Override
   public void read(PacketBuf buf) {
      this.reason = buf.readString();
      this.until = buf.readLong();
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeString(this.reason);
      buf.writeLong(this.until);
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
      this.handle();
   }

   public String getReason() {
      return this.reason;
   }

   public long getUntil() {
      return this.until;
   }

   public void handle() {
      final String message = this.reason != null && !this.reason.isEmpty()
         ? this.reason
         : LanguageManager.translate("chat_unknown_kick_reason");
      // Original popped a disconnect/not-allowed GUI screen; Phase 1 surfaces it in chat.
      Minecraft.getMinecraft().addScheduledTask(new Runnable() {
         @Override
         public void run() {
            net.labymod.main.LabyMod.getInstance().notifyMessageRaw("LabyConnect", ModColor.createColors(message));
         }
      });
   }
}
