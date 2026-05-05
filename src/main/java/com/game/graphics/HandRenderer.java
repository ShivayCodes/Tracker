package com.game.graphics;

import com.game.Mesh;
import com.game.ShaderProgram;
import com.game.Utils;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class HandRenderer {

    private ShaderProgram shader;
    private Mesh cubeMesh;
    private float punchTime = 0;
    private float bobbingTime = 0;

    public void init() throws Exception {
        shader = new ShaderProgram();
        shader.createVertexShader(Utils.loadResource("/shaders/hand_vertex.glsl"));
        shader.createFragmentShader(Utils.loadResource("/shaders/hand_fragment.glsl"));
        shader.link();
        shader.createUniform("projection");
        shader.createUniform("model");
        shader.createUniform("texture_sampler");

        setupMesh();
    }

    private void setupMesh() {
        float[] positions = new float[]{
            -0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f,
        };
        float[] texCoords = new float[24 * 2]; // Dummy
        float[] normals = new float[]{
             0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
             0, 0,-1,  0, 0,-1,  0, 0,-1,  0, 0,-1,
             0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
             0,-1, 0,  0,-1, 0,  0,-1, 0,  0,-1, 0,
             1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
        };
        int[] indices = new int[]{
            0,1,3, 3,1,2, 4,5,7, 7,5,6, 8,9,11, 11,9,10, 12,13,15, 15,13,14, 16,17,19, 19,17,18, 20,21,23, 23,21,22
        };
        cubeMesh = new Mesh(positions, texCoords, normals, new float[24], indices);
    }

    public void punch() {
        punchTime = 1.0f;
    }

    public void render(int width, int height, Texture atlas, int blockType, float deltaTime, boolean isMoving) {
        if (punchTime > 0) punchTime -= deltaTime * 5.0f;
        if (isMoving) bobbingTime += deltaTime * 10.0f;
        else bobbingTime = 0;

        glClear(GL_DEPTH_BUFFER_BIT); // Ensure hand is on top
        shader.bind();

        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(70), (float)width/height, 0.01f, 100.0f);
        shader.setUniform("projection", proj);

        // Hand position: bottom-right
        float bobX = (float) Math.cos(bobbingTime * 0.5f) * 0.02f;
        float bobY = (float) Math.sin(bobbingTime) * 0.02f;
        float punchZ = (float) Math.sin(punchTime * Math.PI) * 0.2f;

        Matrix4f model = new Matrix4f()
            .translate(0.5f + bobX, -0.4f + bobY, -0.8f + punchZ)
            .rotateY((float) Math.toRadians(-45))
            .rotateX((float) Math.toRadians(20))
            .scale(0.3f, 0.6f, 0.3f); // Longish arm/hand
        
        shader.setUniform("model", model);
        shader.setUniform("texture_sampler", 0);
        atlas.bind();

        // We need to update UVs for the specific block type
        // For simplicity, let's just draw the cube with the atlas
        cubeMesh.render();

        shader.unbind();
    }

    public void cleanup() {
        if (shader != null) shader.cleanup();
        if (cubeMesh != null) cubeMesh.cleanup();
    }
}
