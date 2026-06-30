package me.txb1.player.modulesystem.modules.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

// Generic, reusable settings screen for the Utils modules. Each module's getCustomSettingsGui() builds
// one of these fluently (toggle/slider/color/text rows bound to the module's static fields). Lives in
// me.txb1 so lambdas/inner classes are safe from the raven transformer (that package is excluded).
public class UtilSettingsGui extends GuiScreen {
   private final GuiScreen back;
   private final String title;
   private final List<Setting> settings = new ArrayList<Setting>();
   private int colX, rowW, firstY;

   public UtilSettingsGui(Module module, GuiScreen back) {
      this.back = back;
      this.title = module.getDisplayName();
   }

   public UtilSettingsGui toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
      settings.add(new ToggleSetting(label, get, set));
      return this;
   }

   public UtilSettingsGui slider(String label, int min, int max, IntSupplier get, Consumer<Integer> set) {
      settings.add(new SliderSetting(label, min, max, get, set));
      return this;
   }

   public UtilSettingsGui color(String label, IntSupplier getRgb, Consumer<Integer> setRgb) {
      settings.add(new ColorSetting(label, getRgb, setRgb));
      return this;
   }

   public UtilSettingsGui text(String label, Supplier<String> get, Consumer<String> set) {
      settings.add(new TextSetting(label, get, set));
      return this;
   }

   public UtilSettingsGui file(String label, Supplier<String> get, Consumer<String> set) {
      settings.add(new FileSetting(label, get, set));
      return this;
   }

   public UtilSettingsGui button(String label, Runnable action) {
      settings.add(new ButtonSetting(label, action));
      return this;
   }

   @Override
   public void initGui() {
      this.rowW = 220;
      this.colX = this.width / 2 - this.rowW / 2;
      this.firstY = 40;
      int y = this.firstY;
      for (Setting s : settings) {
         s.y = y;
         s.init();
         y += 24;
      }
      this.buttonList.clear();
      this.buttonList.add(new GuiButton(0, this.width / 2 - 50, y + 6, 100, 20, "Done"));
   }

   @Override
   protected void actionPerformed(GuiButton b) {
      if (b.id == 0) {
         this.mc.displayGuiScreen(this.back);
      }
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      for (Setting s : settings) {
         s.onClick(mx, my, btn);
      }
      super.mouseClicked(mx, my, btn);
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long t) {
      for (Setting s : settings) {
         s.onDrag(mx);
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      for (Setting s : settings) {
         s.dragging = false;
      }
      super.mouseReleased(mx, my, btn);
   }

   @Override
   protected void keyTyped(char c, int code) throws IOException {
      for (Setting s : settings) {
         if (s.keyTyped(c, code)) {
            return;
         }
      }
      super.keyTyped(c, code);
   }

   @Override
   public void updateScreen() {
      for (Setting s : settings) {
         s.tick();
      }
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, "§l" + this.title, this.width / 2, 18, -1);
      for (Setting s : settings) {
         s.draw(mx, my);
      }
      super.drawScreen(mx, my, pt);
   }

   // ---- settings ----

   private abstract class Setting {
      final String label;
      int y;
      boolean dragging;

      Setting(String label) {
         this.label = label;
      }

      void init() {
      }

      void tick() {
      }

      abstract void draw(int mx, int my);

      void onClick(int mx, int my, int btn) {
      }

      void onDrag(int mx) {
      }

      boolean keyTyped(char c, int code) {
         return false;
      }
   }

   private final class ToggleSetting extends Setting {
      final BooleanSupplier get;
      final Consumer<Boolean> set;

      ToggleSetting(String label, BooleanSupplier get, Consumer<Boolean> set) {
         super(label);
         this.get = get;
         this.set = set;
      }

      @Override
      void draw(int mx, int my) {
         boolean v = get.getAsBoolean();
         fontRendererObj.drawStringWithShadow("§7" + label + ": " + (v ? "§aON" : "§cOFF"), colX, y, -1);
      }

      @Override
      void onClick(int mx, int my, int btn) {
         if (mx >= colX && mx <= colX + rowW && my >= y - 2 && my <= y + 9) {
            set.accept(!get.getAsBoolean());
         }
      }
   }

   private final class SliderSetting extends Setting {
      final int min;
      final int max;
      final IntSupplier get;
      final Consumer<Integer> set;

      SliderSetting(String label, int min, int max, IntSupplier get, Consumer<Integer> set) {
         super(label);
         this.min = min;
         this.max = max;
         this.get = get;
         this.set = set;
      }

      private int trackY() {
         return y + 10;
      }

      @Override
      void draw(int mx, int my) {
         fontRendererObj.drawStringWithShadow("§7" + label + ": §f" + get.getAsInt(), colX, y, -1);
         int ty = trackY();
         Gui.drawRect(colX, ty, colX + rowW, ty + 4, 0x80000000);
         float frac = (float) (get.getAsInt() - min) / (float) Math.max(1, max - min);
         int knob = colX + (int) (frac * (rowW - 4));
         Gui.drawRect(knob, ty - 2, knob + 4, ty + 6, 0xFFFFFFFF);
      }

      private void apply(int mx) {
         float frac = (float) (mx - colX) / (float) Math.max(1, rowW - 4);
         frac = Math.max(0.0F, Math.min(1.0F, frac));
         set.accept(min + Math.round(frac * (max - min)));
      }

      @Override
      void onClick(int mx, int my, int btn) {
         int ty = trackY();
         if (mx >= colX && mx <= colX + rowW && my >= ty - 3 && my <= ty + 7) {
            dragging = true;
            apply(mx);
         }
      }

      @Override
      void onDrag(int mx) {
         if (dragging) {
            apply(mx);
         }
      }
   }

   private final class ColorSetting extends Setting {
      final IntSupplier get;
      final Consumer<Integer> set;
      GuiTextField field;

      ColorSetting(String label, IntSupplier get, Consumer<Integer> set) {
         super(label);
         this.get = get;
         this.set = set;
      }

      @Override
      void init() {
         field = new GuiTextField(0, fontRendererObj, colX + 70, y - 2, 70, 14);
         field.setMaxStringLength(6);
         field.setText(String.format("%06X", get.getAsInt() & 0xFFFFFF));
      }

      @Override
      void tick() {
         field.updateCursorCounter();
      }

      @Override
      void draw(int mx, int my) {
         fontRendererObj.drawStringWithShadow("§7" + label + ":", colX, y, -1);
         field.drawTextBox();
         Gui.drawRect(colX + 146, y - 2, colX + 164, y + 12, 0xFF000000 | (get.getAsInt() & 0xFFFFFF));
      }

      @Override
      void onClick(int mx, int my, int btn) {
         field.mouseClicked(mx, my, btn);
      }

      @Override
      boolean keyTyped(char c, int code) {
         if (field.isFocused()) {
            field.textboxKeyTyped(c, code);
            String v = field.getText().replaceAll("[^0-9A-Fa-f]", "");
            if (v.length() == 6) {
               try {
                  set.accept(Integer.parseInt(v, 16) & 0xFFFFFF);
               } catch (NumberFormatException ignored) {
               }
            }
            return true;
         }
         return false;
      }
   }

   private final class ButtonSetting extends Setting {
      final Runnable action;

      ButtonSetting(String label, Runnable action) {
         super(label);
         this.action = action;
      }

      @Override
      void draw(int mx, int my) {
         boolean hov = mx >= colX && mx <= colX + rowW && my >= y - 2 && my <= y + 11;
         Gui.drawRect(colX, y - 2, colX + rowW, y + 11, hov ? 0x55FFFFFF : 0x40000000);
         fontRendererObj.drawStringWithShadow("§b▶ " + label, colX + 5, y, -1);
      }

      @Override
      void onClick(int mx, int my, int btn) {
         if (action != null && mx >= colX && mx <= colX + rowW && my >= y - 2 && my <= y + 11) {
            action.run();
         }
      }
   }

   private final class FileSetting extends Setting {
      final Supplier<String> get;
      final Consumer<String> set;

      FileSetting(String label, Supplier<String> get, Consumer<String> set) {
         super(label);
         this.get = get;
         this.set = set;
      }

      @Override
      void draw(int mx, int my) {
         String path = get.get();
         String name = path == null || path.isEmpty() ? "§8(none)" : "§f" + new java.io.File(path).getName();
         fontRendererObj.drawStringWithShadow("§7" + label + ": " + name, colX, y, -1);
         // [Pick...] button on the right
         Gui.drawRect(colX + rowW - 56, y - 3, colX + rowW, y + 11, 0x80000000);
         fontRendererObj.drawStringWithShadow("§ePick...", colX + rowW - 50, y, -1);
      }

      @Override
      void onClick(int mx, int my, int btn) {
         if (mx >= colX + rowW - 56 && mx <= colX + rowW && my >= y - 3 && my <= y + 11) {
            pick();
         }
      }

      private void pick() {
         new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  java.awt.FileDialog fd = new java.awt.FileDialog((java.awt.Frame) null, "Select a sound (wav / ogg / mp3)", java.awt.FileDialog.LOAD);
                  fd.setFile("*.wav;*.ogg;*.mp3");
                  fd.setVisible(true);
                  String dir = fd.getDirectory();
                  String fn = fd.getFile();
                  if (dir != null && fn != null) {
                     set.accept(new java.io.File(dir, fn).getAbsolutePath());
                  }
               } catch (Throwable ignored) {
               }
            }
         }, "BedSound-Picker").start();
      }
   }

   private final class TextSetting extends Setting {
      final Supplier<String> get;
      final Consumer<String> set;
      GuiTextField field;

      TextSetting(String label, Supplier<String> get, Consumer<String> set) {
         super(label);
         this.get = get;
         this.set = set;
      }

      @Override
      void init() {
         field = new GuiTextField(0, fontRendererObj, colX + 70, y - 2, rowW - 70, 14);
         field.setMaxStringLength(64);
         field.setText(get.get());
      }

      @Override
      void tick() {
         field.updateCursorCounter();
      }

      @Override
      void draw(int mx, int my) {
         fontRendererObj.drawStringWithShadow("§7" + label + ":", colX, y, -1);
         field.drawTextBox();
      }

      @Override
      void onClick(int mx, int my, int btn) {
         field.mouseClicked(mx, my, btn);
      }

      @Override
      boolean keyTyped(char c, int code) {
         if (field.isFocused()) {
            field.textboxKeyTyped(c, code);
            set.accept(field.getText());
            return true;
         }
         return false;
      }
   }
}
