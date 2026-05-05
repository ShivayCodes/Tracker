package com.game.graphics;

import com.game.ShaderProgram;
import com.game.Utils;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL30.*;

public class HudRenderer {

    private ShaderProgram shaderProgram;
    private int vaoId;
    private int posVboId;
    private int texVboId;
    private int idxVboId;

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.glsl"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionModel");
        shaderProgram.createUniform("texture_sampler");

        setupMesh();
    }

    private void setupMesh() {
        // A simple unit quad from (0,0) to (1,1)
        float[] positions = new float[]{
            0, 1, // Bottom Left
            1, 1, // Bottom Right
            1, 0, // Top Right
            0, 0  // Top Left
        };
        float[] texCoords = new float[]{
            0, 1,
            1, 1,
            1, 0,
            0, 0
        };
        int[] indices = new int[]{ 0, 1, 2, 2, 3, 0 };

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        posVboId = glGenBuffers();
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        glBindBuffer(GL_ARRAY_BUFFER, posVboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(posBuffer);

        texVboId = glGenBuffers();
        FloatBuffer texBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texBuffer.put(texCoords).flip();
        glBindBuffer(GL_ARRAY_BUFFER, texVboId);
        glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(texBuffer);

        idxVboId = glGenBuffers();
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(int windowWidth, int windowHeight, Texture hudTexture, int selectedSlot) {
        shaderProgram.bind();

        glDisable(GL_DEPTH_TEST); // HUD draws over everything
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Matrix4f ortho = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);

        shaderProgram.setUniform("texture_sampler", 0);
        glActiveTexture(GL_TEXTURE0);
        hudTexture.bind();

        glBindVertexArray(vaoId);

        // Draw Crosshair (Center screen)
        // Texture UV for Crosshair is Top-Left 16x16 (0,0 to 0.25,0.25 in 64x64)
        drawElement(ortho, windowWidth/2 - 16, windowHeight/2 - 16, 32, 32, 0.0f, 0.0f, 0.25f, 0.25f);

        // Draw Hotbar (Bottom screen)
        int hotbarWidth = 400;
        int hotbarHeight = 44;
        int startX = (windowWidth - hotbarWidth) / 2;
        int startY = windowHeight - hotbarHeight - 10;
        
        for (int i = 0; i < 9; i++) {
            float u0, v0, u1, v1;
            if (i == selectedSlot) {
                // Selected Slot: Bottom-Right 32x32 (0.5,0.25 to 1.0,0.75)
                u0 = 0.5f; v0 = 0.25f; u1 = 1.0f; v1 = 0.75f;
            } else {
                // Normal Slot: Bottom-Left 32x32 (0.0,0.25 to 0.5,0.75)
                u0 = 0.0f; v0 = 0.25f; u1 = 0.5f; v1 = 0.75f;
            }
            drawElement(ortho, startX + i * 44, startY, 44, 44, u0, v0, u1, v1);
        }

        // Draw Hearts (Above Hotbar)
        int heartY = startY - 30;
        for (int i = 0; i < 10; i++) {
            // Heart UV: Top-Right 16x16 (0.25,0.0 to 0.5,0.25)
            drawElement(ortho, startX + i * 20, heartY, 20, 20, 0.25f, 0.0f, 0.5f, 0.25f);
        }

        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        shaderProgram.unbind();
    }

    private void drawElement(Matrix4f ortho, float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
        // Adjust the VBOs for the new UV coordinates
        float[] texCoords = new float[]{
            u0, v1,
            u1, v1,
            u1, v0,
            u0, v0
        };
        glBindBuffer(GL_ARRAY_BUFFER, texVboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, texCoords);

        Matrix4f model = new Matrix4f(ortho);
        model.translate(x, y, 0).scale(w, h, 1);
        shaderProgram.setUniform("projectionModel", model);
        
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    public void cleanup() {
        if (shaderProgram != null) shaderProgram.cleanup();
        glDeleteBuffers(posVboId);
        glDeleteBuffers(texVboId);
        glDeleteBuffers(idxVboId);
        glDeleteVertexArrays(vaoId);
    }
}
