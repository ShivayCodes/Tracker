package com.game;

import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int vaoId;
    private final int posVboId;
    private final int texVboId;
    private final int normVboId;
    private final int colorVboId;
    private final int idxVboId;
    private int vertexCount;

    public Mesh(float[] positions, float[] textCoords, float[] normals, float[] colors, int[] indices) {
        vertexCount = indices.length;

        FloatBuffer posBuffer = null;
        FloatBuffer texBuffer = null;
        FloatBuffer normBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Texture Coordinates VBO
            texVboId = glGenBuffers();
            texBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            texBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, texVboId);
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Normals VBO
            normVboId = glGenBuffers();
            normBuffer = MemoryUtil.memAllocFloat(normals.length);
            normBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normVboId);
            glBufferData(GL_ARRAY_BUFFER, normBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Colors/AO VBO
            colorVboId = glGenBuffers();
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 1, GL_FLOAT, false, 0, 0);

            // Index VBO
            idxVboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (texBuffer != null) MemoryUtil.memFree(texBuffer);
            if (normBuffer != null) MemoryUtil.memFree(normBuffer);
            if (colorBuffer != null) MemoryUtil.memFree(colorBuffer);
            if (indicesBuffer != null) MemoryUtil.memFree(indicesBuffer);
        }
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void render() {
        glBindVertexArray(getVaoId());
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(texVboId);
        glDeleteBuffers(normVboId);
        glDeleteBuffers(colorVboId);
        glDeleteBuffers(idxVboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
