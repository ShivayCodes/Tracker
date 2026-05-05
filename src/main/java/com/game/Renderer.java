package com.game;

import com.game.world.Chunk;
import com.game.world.World;
import com.game.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private ShaderProgram shaderProgram;
    private ShaderProgram wireframeShader;
    
    private int wireframeVaoId;
    private int wireframeVboId;

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("texture_sampler");

        wireframeShader = new ShaderProgram();
        wireframeShader.createVertexShader(Utils.loadResource("/shaders/wireframe_vertex.glsl"));
        wireframeShader.createFragmentShader(Utils.loadResource("/shaders/wireframe_fragment.glsl"));
        wireframeShader.link();
        wireframeShader.createUniform("projectionModelView");

        setupWireframeMesh();
    }

    private void setupWireframeMesh() {
        // A simple wireframe cube from (0,0,0) to (1,1,1)
        float[] positions = new float[]{
            0,0,0, 1,0,0,  1,0,0, 1,0,1,  1,0,1, 0,0,1,  0,0,1, 0,0,0, // Bottom
            0,1,0, 1,1,0,  1,1,0, 1,1,1,  1,1,1, 0,1,1,  0,1,1, 0,1,0, // Top
            0,0,0, 0,1,0,  1,0,0, 1,1,0,  1,0,1, 1,1,1,  0,0,1, 0,1,1  // Vertical
        };
        wireframeVaoId = glGenVertexArrays();
        glBindVertexArray(wireframeVaoId);
        wireframeVboId = glGenBuffers();
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        glBindBuffer(GL_ARRAY_BUFFER, wireframeVboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(posBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(World world, Camera camera, int width, int height, com.game.graphics.Texture texture, Vector3f targetBlock) {
        clear();

        if (width == 0 || height == 0) return;

        shaderProgram.bind();

        Matrix4f projectionMatrix = camera.getProjectionMatrix(FOV, width, height, Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projection", projectionMatrix);

        Matrix4f viewMatrix = camera.getViewMatrix();
        shaderProgram.setUniform("view", viewMatrix);

        shaderProgram.setUniform("texture_sampler", 0);
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        Matrix4f modelMatrix = new Matrix4f().identity();
        shaderProgram.setUniform("model", modelMatrix);

        for (Chunk chunk : world.getChunks()) {
            Mesh mesh = chunk.getMesh();
            if (mesh != null) mesh.render();
        }

        for (Entity entity : world.getEntities()) {
            Mesh mesh = entity.getMesh();
            if (mesh != null) {
                modelMatrix.identity()
                    .translate(entity.getPosition())
                    .rotateX((float) Math.toRadians(entity.getRotation().x))
                    .rotateY((float) Math.toRadians(entity.getRotation().y))
                    .rotateZ((float) Math.toRadians(entity.getRotation().z))
                    .scale(entity.getScale());
                shaderProgram.setUniform("model", modelMatrix);
                mesh.render();
            }
        }

        shaderProgram.unbind();

        if (targetBlock != null) {
            glLineWidth(2.0f);
            wireframeShader.bind();
            Matrix4f pmv = new Matrix4f(projectionMatrix).mul(viewMatrix).translate(targetBlock).scale(1.002f).translate(-0.001f, -0.001f, -0.001f);
            wireframeShader.setUniform("projectionModelView", pmv);
            glBindVertexArray(wireframeVaoId);
            glDrawArrays(GL_LINES, 0, 24);
            glBindVertexArray(0);
            wireframeShader.unbind();
        }
    }

    public void cleanup() {
        if (shaderProgram != null) shaderProgram.cleanup();
        if (wireframeShader != null) wireframeShader.cleanup();
        glDeleteBuffers(wireframeVboId);
        glDeleteVertexArrays(wireframeVaoId);
    }
}
