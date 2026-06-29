package net.labymod.core.asm;

/**
 * Stub. Original LabyMod core mod indicator.
 * VoiceChat's PacketValidatorEditor calls isObfuscated() to pick raw Notch
 * descriptors vs MCP descriptors when patching its PacketValidator class.
 *
 * In a Forge dev workspace this returns false (MCP names). In production
 * (deobf=false), Forge uses SRG/notch-mapped runtime — return true.
 *
 * The simplest correct approach: detect at runtime whether we're in dev or prod.
 * Forge sets the system property "fml.deobfuscatedEnvironment" — if false (or unset),
 * we're in prod with obfuscated/SRG names.
 */
public final class LabyModCoreMod {
    private LabyModCoreMod() {}

    public static boolean isObfuscated() {
        try {
            Class<?> launchClassLoader = Class.forName("net.minecraft.launchwrapper.Launch");
            Object blackboard = launchClassLoader.getField("blackboard").get(null);
            if (blackboard instanceof java.util.Map) {
                Object deobf = ((java.util.Map<?, ?>) blackboard).get("fml.deobfuscatedEnvironment");
                if (deobf instanceof Boolean) {
                    return !(Boolean) deobf;
                }
            }
        } catch (Throwable ignored) {}
        return true;
    }
}
