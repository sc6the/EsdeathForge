package me.txb1.forge.coremod;

import java.util.ListIterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

// raven.jar's transformer (which must not be touched) corrupts RenderGlobal: its <init> binds a
// texture with a null ResourceLocation, which only surfaces now that -Xverify:none lets the class
// load. The vanilla bind in <init> is the (non-essential) forcefield texture, re-bound at render
// time anyway. We replace each TextureManager.bindTexture(ResourceLocation) call inside <init> with a
// POP2 (drop the [TextureManager, ResourceLocation] operands) so the null bind is skipped and the
// constructor completes.
//
// Read with SKIP_FRAMES + write with no COMPUTE so we never analyse raven's broken renderEntities
// bytecode (ASM's verifier-grade analysis throws on it); the POP2 has the same stack effect as the
// INVOKEVIRTUAL it replaces, so nothing else shifts. Runs at runtime/SRG names (after deobf), hence
// the func_110577_a method name.
public class RenderGlobalInitFix implements IClassTransformer {
   private static final String TARGET = "net.minecraft.client.renderer.RenderGlobal";
   private static final String BIND_TEXTURE = "func_110577_a"; // TextureManager.bindTexture(ResourceLocation)
   private static final String BIND_DESC = "(Lnet/minecraft/util/ResourceLocation;)V";

   @Override
   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      if (basicClass == null || !TARGET.equals(transformedName)) {
         return basicClass;
      }
      try {
         ClassReader cr = new ClassReader(basicClass);
         ClassNode cn = new ClassNode();
         cr.accept(cn, ClassReader.SKIP_FRAMES);

         int patched = 0;
         for (MethodNode m : cn.methods) {
            if (!"<init>".equals(m.name)) {
               continue;
            }
            ListIterator<AbstractInsnNode> it = m.instructions.iterator();
            while (it.hasNext()) {
               AbstractInsnNode insn = it.next();
               if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                  MethodInsnNode min = (MethodInsnNode) insn;
                  if (BIND_TEXTURE.equals(min.name) && BIND_DESC.equals(min.desc)) {
                     it.set(new InsnNode(Opcodes.POP2));
                     patched++;
                  }
               }
            }
         }
         if (patched == 0) {
            return basicClass;
         }
         ClassWriter cw = new ClassWriter(cr, 0);
         cn.accept(cw);
         System.out.println("[Esdeath] RenderGlobal.<init>: neutralised " + patched
            + " bindTexture call(s) to avoid raven's null-texture NPE");
         return cw.toByteArray();
      } catch (Throwable t) {
         System.err.println("[Esdeath] RenderGlobal.<init> fix failed, leaving as-is: " + t);
         return basicClass;
      }
   }
}
