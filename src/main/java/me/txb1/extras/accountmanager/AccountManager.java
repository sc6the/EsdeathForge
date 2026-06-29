package me.txb1.extras.accountmanager;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class AccountManager extends GuiScreen {
   String status;
   private GuiTextField field_password;
   private GuiTextField field_username;
   private GuiButton button_login;
   String password;

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   public AccountManager() {
      this.status = "";
      this.password = "";
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.field_password.mouseClicked(var1, var2, var3);
      this.field_username.mouseClicked(var1, var2, var3);
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   public void initGui() {
      // Account Manager now opens UniversalAccountManager (ported into the client).
      this.mc.displayGuiScreen(
         new me.proxycracked.universalaccountmanager.gui.GuiAccountManager(new net.minecraft.client.gui.GuiMainMenu())
      );
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      switch (var1.id) {
         case 0:
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
            
            break;
         case 1:
            this.status = "Logging in...";
            new Timer()
               .schedule(
                  new TimerTask() {

                     @Override
                     public void run() {
                        try {
                           Field var1 = Minecraft.class.getDeclaredField("session");
                           var1.setAccessible(true);
                           var1.set(
                              Minecraft.getMinecraft(),
                              LoginUtils.createSession(AccountManager.this.field_username.getText(), AccountManager.this.password, Proxy.NO_PROXY)
                           );
                           AccountManager.this.field_username.setText("");
                           AccountManager.this.field_password.setText("");
                           AccountManager.this.password = "";
                           if (((Minecraft.getMinecraft().getSession().getUsername()) == null)) {
                              AccountManager.this.status = "Falsche Accountdaten :(";
                              
                           } else {
                              AccountManager.this.status = String.valueOf(
                                 new StringBuilder().append("Erfolgreich als %new%").append(Minecraft.getMinecraft().getSession().getUsername()).append(" eingeloggt.")
                              );
                           }
                        } catch (Exception var3) {
                           AccountManager.this.status = "Falsche Accountdaten :(";
                           var3.printStackTrace();
                           return;
                        }

                                                   ;
                        
                     }

                  },
                  400L
               );
      }

      super.actionPerformed(var1);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      this.field_username.textboxKeyTyped(var1, var2);
      this.field_password.textboxKeyTyped(var1, var2);
      if ((this.field_password.isFocused())) {
         if ((Keyboard.isKeyDown(15)) && (Keyboard.isKeyDown(42))) {
            this.field_password.setFocused(false);
            this.field_username.setFocused(true);
         }

         if (((var2) == (14))) {
            this.password = "";
            this.field_password.setText("");
         }

         if (((this.field_password.getText().length()) == (1))) {
            this.password = this.field_password.getText();
            this.field_password.setText("*");
            
         } else {
            this.password = String.valueOf(
               new StringBuilder().append(this.password).append(this.field_password.getText().replaceAll("\\*", ""))
            );
            String var3 = "";
            int var4 = 0;

            while (((var4) < (this.field_password.getText().length()))) {
               var3 = String.valueOf(new StringBuilder().append(var3).append("*"));
               var4++;
               
            }

            this.field_password.setText(var3);
            
         }
      } else if (((var2) == (15)) && !(Keyboard.isKeyDown(42))) {
         this.field_username.setFocused(false);
         this.field_password.setFocused(true);
      }

      if (((var2) == (1))) {
         this.actionPerformed(this.buttonList.get(0));
      }

      if (((var2) == (28))) {
         this.actionPerformed(this.button_login);
      }

      super.keyTyped(var1, var2);
   }

   public static void fail(String var0) {
      FontRenderer var1 = Minecraft.getMinecraft().fontRendererObj;
      var1.drawString(var0, 30.0F, 5.0F, Color.RED.getRGB(), true);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      ScaledResolution var4 = new ScaledResolution(this.mc);
      this.mc.getTextureManager().bindTexture(new ResourceLocation("EsdeathClient/MainBackground.jpg"));
      Gui.drawModalRectWithCustomSizedTexture(
         0, 0, 0.0F, 0.0F, var4.getScaledWidth(), var4.getScaledHeight(), (float)var4.getScaledWidth(), (float)var4.getScaledHeight()
      );
      Gui.drawRect(0, 0, this.width, this.height, 1073741824);
      this.drawString(this.mc.fontRendererObj, "§fｕｓｅｒｎａｍｅ:", this.field_username.xPosition, this.field_username.yPosition - 10, Color.WHITE.getRGB());
      this.drawString(this.mc.fontRendererObj, "§fｐａｓｓｗｏｒｄ:", this.field_password.xPosition, this.field_password.yPosition - 10, Color.WHITE.getRGB());
      if ((this.status.contains("%new%"))) {
         int var5 = 0;

         while (((var5) < (this.status.split("%new%").length))) {
            this.drawString(
               this.mc.fontRendererObj,
               this.status.split("%new%")[var5],
               this.field_password.xPosition + 230,
               this.field_username.yPosition + var5 * 12,
               Color.WHITE.getRGB()
            );
            var5++;
            
         }

      } else {
         this.drawString(this.mc.fontRendererObj, this.status, this.field_password.xPosition + 230, this.field_username.yPosition, Color.WHITE.getRGB());
      }

      this.field_username.drawTextBox();
      this.field_password.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

}
