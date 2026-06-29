package me.txb1.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

public class ColorChooserButton extends JButton {
   private List<ColorChooserButton.ColorChangedListener> listeners = new ArrayList<>();
   private Color current;

   public static ImageIcon createIcon(Color var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(var1, var2, 1);
      Graphics2D var4 = var3.createGraphics();
      var4.setColor(var0);
      var4.fillRect(0, 0, var1, var2);
      var4.setXORMode(Color.DARK_GRAY);
      var4.drawRect(0, 0, var1 - 1, var2 - 1);
      var3.flush();
      return new ImageIcon(var3);
   }

   public Color getSelectedColor() {
      return this.current;
   }

   public void setSelectedColor(Color var1) {
      this.setSelectedColor(var1, true);
   }

   public void addColorChangedListener(ColorChooserButton.ColorChangedListener var1) {
      this.listeners.add(var1);
   }

   public void setSelectedColor(Color var1, boolean var2) {
      if (!((var1) == null)) {
         this.current = var1;
         this.setIcon(createIcon(this.current, 16, 16));
         this.repaint();
         if ((var2)) {
            Iterator var3 = this.listeners.iterator();

            while ((var3.hasNext())) {
               ColorChooserButton.ColorChangedListener var4 = (ColorChooserButton.ColorChangedListener)var3.next();
               var4.colorChanged(var1);
               
            }
         }
      }
   }

   public ColorChooserButton(Color var1) {
      this.setSelectedColor(var1);
      this.addActionListener(
         new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent var1) {
               Color var2 = JColorChooser.showDialog(null, "Choose a color", ColorChooserButton.this.current);
               ColorChooserButton.this.setSelectedColor(var2);
            }

         }
      );
   }

   public interface ColorChangedListener {
      void colorChanged(Color var1);
   }
}
