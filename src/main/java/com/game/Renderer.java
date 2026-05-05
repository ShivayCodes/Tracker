package com.game;

import com.game.world.Chunk;
import com.game.world.World;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private ShaderProgram shaderProgram;

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("texture_sampler");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(World world, Camera camera, int width, int height, com.game.graphics.Texture texture) {
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
            if (mesh != null) {
                mesh.render();
            }
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
