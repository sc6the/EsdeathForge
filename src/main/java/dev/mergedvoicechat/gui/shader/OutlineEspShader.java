package dev.mergedvoicechat.gui.shader;

import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal GLSL 120 shader program (one passthrough vertex shader + a subclass-supplied fragment
 * shader). Ported from Raven-bS's OutlineESPShader (keystrokesmod.utility.shader) — the technique
 * behind its PlayerESP "outline" mode: render the target into an offscreen buffer with a flat glow
 * shader, then composite an edge-detect/fill shader fullscreen.
 */
public abstract class OutlineEspShader {

    private static final String VERT =
        "#version 120\n" +
        "void main() {\n" +
        "  gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
        "  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
        "}";

    protected int programId = -1;
    private final Map<String, Integer> uniforms = new HashMap<String, Integer>();

    public OutlineEspShader(String fragSrc) {
        try {
            int v = compile(VERT, GL20.GL_VERTEX_SHADER);
            int f = compile(fragSrc, GL20.GL_FRAGMENT_SHADER);
            if (v < 0 || f < 0) return;
            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, v);
            GL20.glAttachShader(programId, f);
            GL20.glLinkProgram(programId);
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                programId = -1;
                return;
            }
            onLink();
        } catch (Throwable t) {
            programId = -1;
        }
    }

    private int compile(String src, int type) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        return GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0 ? -1 : id;
    }

    protected void cacheUniform(String name) {
        if (programId >= 0) uniforms.put(name, GL20.glGetUniformLocation(programId, name));
    }

    protected int uniform(String name) {
        Integer i = uniforms.get(name);
        return i == null ? -1 : i;
    }

    public abstract void onLink();

    public abstract void onUse();

    public void use() {
        if (programId >= 0) onUse();
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public boolean isValid() {
        return programId >= 0;
    }
}
