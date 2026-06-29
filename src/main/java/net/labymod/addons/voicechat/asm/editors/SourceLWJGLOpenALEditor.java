package net.labymod.addons.voicechat.asm.editors;

import net.labymod.core.asm.global.ClassEditor;
import net.labymod.core.asm.global.ClassEditor.ClassEditorType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class SourceLWJGLOpenALEditor extends ClassEditor {
   public SourceLWJGLOpenALEditor() {
      super(ClassEditorType.CLASS_NODE);
   }

   public void accept(String name, ClassNode node) {
      for (MethodNode method : node.methods) {
         if (method.name.equals("setPosition")) {
            InsnList list = new InsnList();
            list.add(new IntInsnNode(25, 0));
            list.add(new IntInsnNode(25, 0));
            list.add(new FieldInsnNode(180, "paulscode/sound/libraries/SourceLWJGLOpenAL", "channel", "Lpaulscode/sound/Channel;"));
            list.add(new TypeInsnNode(192, "paulscode/sound/libraries/ChannelLWJGLOpenAL"));
            list.add(new FieldInsnNode(181, "paulscode/sound/libraries/SourceLWJGLOpenAL", "channelOpenAL", "Lpaulscode/sound/libraries/ChannelLWJGLOpenAL;"));
            AbstractInsnNode firstInstruction = method.instructions.get(0);
            method.instructions.insertBefore(firstInstruction, list);
            break;
         }
      }
   }
}
