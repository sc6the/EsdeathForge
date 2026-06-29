package net.labymod.addons.voicechat.asm;

import net.labymod.addons.voicechat.asm.editors.PacketValidatorEditor;
import net.labymod.addons.voicechat.asm.editors.SourceLWJGLOpenALEditor;
import net.labymod.core.asm.global.ClassEditor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class VoiceChatTransformer implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (name.equals("paulscode.sound.libraries.SourceLWJGLOpenAL")) {
         return transform(name, bytes, new SourceLWJGLOpenALEditor());
      } else {
         return name.equals("net.labymod.addons.voicechat.asm.validator.PacketValidator") ? transform(name, bytes, new PacketValidatorEditor()) : bytes;
      }
   }

   private static byte[] transform(String name, byte[] bytes, ClassEditor editor) {
      try {
         ClassNode node = new ClassNode();
         ClassReader reader = new ClassReader(bytes);
         reader.accept(node, 0);
         editor.accept(name, node);
         ClassWriter writer = new ClassWriter(3);
         node.accept(writer);
         return writer.toByteArray();
      } catch (Exception var6) {
         var6.printStackTrace();
         return bytes;
      }
   }
}
