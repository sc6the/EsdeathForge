package net.labymod.addons.voicechat.client;

import net.labymod.main.lang.LanguageManager;

public class Mute {
   private final String reason;
   private final long muteUntil;
   private final String mutedBy;

   public Mute(String reason, long muteUntil, String mutedBy) {
      this.reason = reason;
      this.muteUntil = muteUntil;
      this.mutedBy = mutedBy;
   }

   public String getReason() {
      return this.reason;
   }

   public long getMuteUntil() {
      return this.muteUntil;
   }

   public String getMutedBy() {
      return this.mutedBy;
   }

   public boolean isExpired() {
      return System.currentTimeMillis() > this.muteUntil;
   }

   public String getTimeLeft() {
      long time = this.muteUntil - System.currentTimeMillis();
      long secs = time / 1000L;
      long mins = secs / 60L;
      long hours = mins / 60L;
      long days = hours / 24L;
      long months = days / 31L;
      long years = months / 12L;
      String timeLeft = null;
      if (months >= 12L) {
         timeLeft = years + " " + LanguageManager.translate("time_" + (years == 1L ? "year" : "years"));
      } else if (days >= 31L) {
         timeLeft = months + " " + LanguageManager.translate("time_" + (months == 1L ? "month" : "months"));
      } else if (hours >= 24L) {
         timeLeft = days + " " + LanguageManager.translate("time_" + (days == 1L ? "day" : "days"));
      } else if (mins >= 60L) {
         timeLeft = hours + " " + LanguageManager.translate("time_" + (hours == 1L ? "hour" : "hours"));
      } else if (secs >= 60L) {
         timeLeft = mins + " " + LanguageManager.translate("time_" + (mins == 1L ? "minute" : "minutes"));
      } else {
         timeLeft = secs + " " + LanguageManager.translate("time_" + (secs == 1L ? "second" : "seconds"));
      }

      return timeLeft;
   }
}
