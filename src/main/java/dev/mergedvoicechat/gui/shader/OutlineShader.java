package dev.mergedvoicechat.gui.shader;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

/**
 * Composite pass over the glow buffer. The buffer's RGB is intentionally ignored — only its alpha
 * (coverage, which also carries the per-player fade) is read — and every output pixel is painted
 * with the uniform {@code tint} colour. This is what fixes the "white patches" bug: cosmetic /
 * custom layer renderers reset the shader program and write fixed-function (white) colour into the
 * glow buffer, but since we recolour from the uniform, those pixels still come out the right colour.
 *
 * <p>Interior pixels (coverage &gt; 0) get the tint at {@code fill * coverage} opacity; an empty
 * pixel within {@code kernel} of a covered one gets the tint at the neighbour's coverage -&gt; a glow
 * rim. Based on Raven-bS OutlineShader, extended with the tint + fill uniforms.
 */
public final class OutlineShader extends OutlineEspShader {

    private static final String FRAG =
        "#version 120\n" +
        "uniform sampler2D tex;\n" +
        "uniform vec2 texelSize;\n" +
        "uniform float kernel;\n" +
        "uniform float fill;\n" +
        "uniform vec3 tint;\n" +
        "void main() {\n" +
        "  float ca = texture2D(tex, gl_TexCoord[0].xy).a;\n" +
        "  if (ca > 0.0) { gl_FragColor = vec4(tint, ca * fill); return; }\n" +
        "  float na = 0.0;\n" +
        "  for (float dx = -kernel; dx <= kernel; dx += 1.0)\n" +
        "    for (float dy = -kernel; dy <= kernel; dy += 1.0) {\n" +
        "      float s = texture2D(tex, gl_TexCoord[0].xy + vec2(dx, dy) * texelSize).a;\n" +
        "      if (s > na) na = s;\n" +
        "    }\n" +
        "  gl_FragColor = vec4(tint, na);\n" +
        "}";

    private float currentFill = 0.0f;
    private float tintR = 1.0f, tintG = 1.0f, tintB = 1.0f;

    public OutlineShader() {
        super(FRAG);
    }

    public void setFill(float fill) {
        this.currentFill = fill;
    }

    public void setTint(int r, int g, int b) {
        this.tintR = r / 255f;
        this.tintG = g / 255f;
        this.tintB = b / 255f;
    }

    @Override
    public void onLink() {
        cacheUniform("tex");
        cacheUniform("texelSize");
        cacheUniform("kernel");
        cacheUniform("fill");
        cacheUniform("tint");
    }

    @Override
    public void onUse() {
        GL20.glUseProgram(programId);
        int u = uniform("tex");
        if (u >= 0) GL20.glUniform1i(u, 0);
        u = uniform("texelSize");
        if (u >= 0) {
            Minecraft mc = Minecraft.getMinecraft();
            GL20.glUniform2f(u, 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        }
        u = uniform("kernel");
        if (u >= 0) GL20.glUniform1f(u, 2.0f);
        u = uniform("fill");
        if (u >= 0) GL20.glUniform1f(u, this.currentFill);
        u = uniform("tint");
        if (u >= 0) GL20.glUniform3f(u, this.tintR, this.tintG, this.tintB);
    }
}
