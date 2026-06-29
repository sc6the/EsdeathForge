package net.labymod.addons.voicechat.gui.slider;

public interface GuiResponder {
   void setEntryValue(int var1, boolean var2);

   void onTick(int var1, float var2);

   void setEntryValue(int var1, String var2);
}
