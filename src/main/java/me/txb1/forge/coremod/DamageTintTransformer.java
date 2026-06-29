package me.txb1.forge.coremod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

// DamageTint (ported from Damage_Tint_1.2): the entity hurt-flash colour is written into
// RendererLivingEntity's brightnessBuffer as four FloatBuffer.put calls with the constants
// 1.0F, 0.0F, 0.0F, 0.3F (R,G,B,A). We swap those four constants for GETSTATIC reads of the
// DamageTint module's live fields, so the flash takes the configured colour (vanilla values while
// the module is OFF). We do NOT match by method name: OptiFine replaces RendererLivingEntity and
// refactors setBrightness, so instead we scan every method for the put(1)/put(0)/put(0)/put(0.3)
// sequence and patch wherever it is found. Constant -> GETSTATIC keeps the stack effect identical.
public class DamageTintTransformer implements IClassTransformer {
   private static final String TARGET = "net.minecraft.client.renderer.entity.RendererLivingEntity";
   private static final String DT = "me/txb1/player/modulesystem/modules/render/DamageTint";
   private static final String[] FIELDS = {"red", "green", "blue", "alpha"};
   private static final float[] WANT = {1.0F, 0.0F, 0.0F, 0.3F};

   @Override
   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      if (basicClass == null || !TARGET.equals(transformedName)) {
         return basicClass;
      }
      try {
         ClassReader cr = new ClassReader(basicClass);
         ClassNode cn = new ClassNode();
         cr.accept(cn, 0);

         boolean patched = false;
         for (MethodNode m : cn.methods) {
            // collect the constant pushed immediately before each FloatBuffer.put(F) call, in order.
            List<AbstractInsnNode> constInsns = new ArrayList<AbstractInsnNode>();
            List<Float> vals = new ArrayList<Float>();
            for (AbstractInsnNode insn = m.instructions.getFirst(); insn != null; insn = insn.getNext()) {
               if (isFloatBufferPut(insn)) {
                  AbstractInsnNode prev = insn.getPrevious();
                  Float v = floatValue(prev);
                  if (v != null) {
                     constInsns.add(prev);
                     vals.add(v);
                  }
               }
            }
            // find the 1,0,0,0.3 run among those put-constants and rewire it.
            for (int i = 0; i + 3 < vals.size(); i++) {
               if (eq(vals.get(i), WANT[0]) && eq(vals.get(i + 1), WANT[1])
                  && eq(vals.get(i + 2), WANT[2]) && eq(vals.get(i + 3), WANT[3])) {
                  for (int k = 0; k < 4; k++) {
                     m.instructions.set(constInsns.get(i + k), new FieldInsnNode(Opcodes.GETSTATIC, DT, FIELDS[k], "F"));
                  }
                  patched = true;
                  break;
               }
            }
         }
         if (!patched) {
            System.err.println("[Esdeath] DamageTint: hurt-flash put(1,0,0,0.3) sequence not found in RendererLivingEntity");
            return basicClass;
         }
         ClassWriter cw = new ClassWriter(cr, 0);
         cn.accept(cw);
         System.out.println("[Esdeath] DamageTint: rewired RendererLivingEntity hurt-flash colour to the module fields");
         return cw.toByteArray();
      } catch (Throwable t) {
         System.err.println("[Esdeath] DamageTint transform failed, leaving as-is: " + t);
         return basicClass;
      }
   }

   private static boolean isFloatBufferPut(AbstractInsnNode insn) {
      if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL || !(insn instanceof MethodInsnNode)) {
         return false;
      }
      MethodInsnNode mi = (MethodInsnNode) insn;
      return "put".equals(mi.name) && "(F)Ljava/nio/FloatBuffer;".equals(mi.desc);
   }

   // value of a float-pushing instruction (FCONST_0/1/2 or LDC <float>), or null if it isn't one.
   private static Float floatValue(AbstractInsnNode insn) {
      if (insn == null) {
         return null;
      }
      switch (insn.getOpcode()) {
         case Opcodes.FCONST_0:
            return 0.0F;
         case Opcodes.FCONST_1:
            return 1.0F;
         case Opcodes.FCONST_2:
            return 2.0F;
         case Opcodes.LDC:
            Object cst = ((LdcInsnNode) insn).cst;
            return cst instanceof Float ? (Float) cst : null;
         default:
            return null;
      }
   }

   private static boolean eq(float a, float b) {
      return Math.abs(a - b) < 1.0E-4F;
   }
}
