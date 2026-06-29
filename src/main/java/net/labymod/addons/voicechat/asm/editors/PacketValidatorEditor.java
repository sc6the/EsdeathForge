package net.labymod.addons.voicechat.asm.editors;

import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.global.ClassEditor;
import net.labymod.core.asm.global.ClassEditor.ClassEditorType;
import net.labymod.main.Source;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class PacketValidatorEditor extends ClassEditor {
   public PacketValidatorEditor() {
      super(ClassEditorType.CLASS_NODE);
   }

   public void accept(String name, ClassNode node) {
      boolean mc18 = Source.ABOUT_MC_VERSION.startsWith("1.8");
      boolean obf = LabyModCoreMod.isObfuscated();

      for (MethodNode method : node.methods) {
         boolean first = true;

         for (int i = 0; i < method.instructions.size(); i++) {
            AbstractInsnNode abstractInsnNode = method.instructions.get(i);
            if (abstractInsnNode.getOpcode() == 193 && abstractInsnNode instanceof TypeInsnNode) {
               TypeInsnNode typeNode = (TypeInsnNode)abstractInsnNode;
               if (first) {
                  first = false;
                  if (obf) {
                     typeNode.desc = mc18 ? "fp" : "ic";
                  } else {
                     typeNode.desc = mc18 ? "net/minecraft/network/play/server/S0CPacketSpawnPlayer" : "net/minecraft/network/play/server/SPacketSpawnPlayer";
                  }
               } else if (obf) {
                  typeNode.desc = mc18 ? "hb" : "jt";
               } else {
                  typeNode.desc = mc18
                     ? "net/minecraft/network/play/server/S13PacketDestroyEntities"
                     : "net/minecraft/network/play/server/SPacketDestroyEntities";
               }
            }
         }
      }
   }
}
