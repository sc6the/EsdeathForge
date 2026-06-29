package me.txb1.forge.coremod;

import me.powns.glintcolorizer.asm.itemrender.RenderEffectVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

// Re-gated GlintColorizer item transformer. The bundled RenderItemTransformer gates the target
// method by a NOTCH descriptor ("(Lboq;)V"), which never matches under EsdeathCorePlugin's
// post-deobf (SRG) stage. RenderEffectVisitor is constant-pattern based (only rewrites the method
// that LDCs the vanilla glint colour -8372020 / periods 3000,4873), so it's safe to run it over
// every method of RenderItem — only the glint method is actually changed.
public class GlintItemTransformer implements IClassTransformer {
   @Override
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (bytes == null || !"net.minecraft.client.renderer.entity.RenderItem".equals(transformedName)) {
         return bytes;
      }
      ClassReader reader = new ClassReader(bytes);
      ClassWriter writer = new ClassWriter(reader, 1);
      ClassVisitor visitor = new ClassVisitor(327680, writer) {
         @Override
         public MethodVisitor visitMethod(int access, String mName, String desc, String signature, String[] exceptions) {
            return new RenderEffectVisitor(super.visitMethod(access, mName, desc, signature, exceptions));
         }
      };
      reader.accept(visitor, 0);
      return writer.toByteArray();
   }
}
