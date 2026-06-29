package net.labymod.core.asm.global;

import org.objectweb.asm.tree.ClassNode;

/**
 * Stub of LabyMod's ClassEditor base class.
 * Editors live as ASM tree-API visitors; the type tells our coremod transformer
 * whether to feed in a ClassNode or raw bytecode. We only use CLASS_NODE.
 */
public abstract class ClassEditor {

    public enum ClassEditorType {
        CLASS_NODE,
        CLASS_VISITOR
    }

    private final ClassEditorType type;

    protected ClassEditor(ClassEditorType type) {
        this.type = type;
    }

    public ClassEditorType getType() {
        return type;
    }

    public abstract void accept(String name, ClassNode node);
}
