package net.labymod.addons.voicechat.client;

public class Report {
   private final int count;
   private final String reason;

   public Report(int count, String reason) {
      this.count = count;
      this.reason = reason;
   }

   public int getCount() {
      return this.count;
   }

   public String getReason() {
      return this.reason;
   }
}
