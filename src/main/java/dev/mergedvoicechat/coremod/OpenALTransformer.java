package dev.mergedvoicechat.coremod;

import net.labymod.addons.voicechat.asm.editors.PacketValidatorEditor;
import net.labymod.addons.voicechat.asm.editors.SourceLWJGLOpenALEditor;
import net.labymod.core.asm.global.ClassEditor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Forge core transformer that runs the two ASM editors VoiceChat needs:
 *   - SourceLWJGLOpenALEditor patches paulscode's OpenAL source class so its
 *     surround voice playback can mix into Minecraft's audio engine.
 *   - PacketValidatorEditor rewrites references inside the embedded
 *     net.labymod.addons.voicechat.asm.validator.PacketValidator class for the
 *     current MC version + obf state.
 */
public class OpenALTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if ("paulscode.sound.libraries.SourceLWJGLOpenAL".equals(name)) {
            return apply(name, bytes, new SourceLWJGLOpenALEditor());
        }
        if ("net.labymod.addons.voicechat.asm.validator.PacketValidator".equals(name)) {
            return apply(name, bytes, new PacketValidatorEditor());
        }
        return bytes;
    }

    private static byte[] apply(String name, byte[] bytes, ClassEditor editor) {
        try {
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, 0);
            editor.accept(name, node);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return bytes;
        }
    }
}
