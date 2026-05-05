package com.game.graphics;

import com.game.Mesh;
import com.game.ShaderProgram;
import com.game.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleSystem {

    private static class Particle {
        Vector3f pos;
        Vector3f vel;
        float life; // 0 to 1
        Particle(Vector3f pos, Vector3f vel) {
            this.pos = pos;
            this.vel = vel;
            this.life = 1.0f;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private ShaderProgram shader;
    private Mesh particleMesh;

    public void init() throws Exception {
        shader = new ShaderProgram();
        shader.createVertexShader(Utils.loadResource("/shaders/hand_vertex.glsl")); // Reuse hand vertex
        shader.createFragmentShader(Utils.loadResource("/shaders/hand_fragment.glsl")); // Reuse hand fragment
        shader.link();
        shader.createUniform("projection");
        shader.createUniform("model");
        shader.createUniform("texture_sampler");

        setupMesh();
    }

    private void setupMesh() {
        // Tiny cube mesh
        float s = 0.05f;
        float[] positions = new float[]{
            -s,s,s, -s,-s,s, s,-s,s, s,s,s,
            -s,s,-s, s,s,-s, -s,-s,-s, s,-s,-s,
            -s,s,-s, -s,s,s, s,s,s, s,s,-s,
            -s,-s,-s, s,-s,-s, -s,-s,s, s,-s,s,
             s,s,s, s,-s,s, s,-s,-s, s,s,-s,
            -s,s,s, -s,s,-s, -s,-s,-s, -s,-s,s,
        };
        float[] texCoords = new float[48]; // Dummy
        float[] normals = new float[72]; // Dummy
        int[] indices = new int[]{
            0,1,3, 3,1,2, 4,5,7, 7,5,6, 8,9,11, 11,9,10, 12,13,15, 15,13,14, 16,17,19, 19,17,18, 20,21,23, 23,21,22
        };
        particleMesh = new Mesh(positions, texCoords, normals, new float[24], indices);
    }

    public void spawn(Vector3f pos) {
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            Vector3f vel = new Vector3f(
                (rand.nextFloat() - 0.5f) * 2f,
                (rand.nextFloat() + 0.5f) * 3f,
                (rand.nextFloat() - 0.5f) * 2f
            );
            particles.add(new Particle(new Vector3f(pos).add(0.5f, 0.5f, 0.5f), vel));
        }
    }

    public void update(float deltaTime) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= deltaTime * 1.5f;
            if (p.life <= 0) {
                it.remove();
                continue;
            }
            p.vel.y -= 9.8f * deltaTime; // Gravity
            p.pos.add(new Vector3f(p.vel).mul(deltaTime));
        }
    }

    public void render(Matrix4f projection, Matrix4f view, Texture atlas) {
        if (particles.isEmpty()) return;

        shader.bind();
        shader.setUniform("projection", projection);
        shader.setUniform("texture_sampler", 0);
        atlas.bind();

        for (Particle p : particles) {
            Matrix4f model = new Matrix4f().translate(p.pos).scale(p.life);
            shader.setUniform("model", model);
            particleMesh.render();
        }
        shader.unbind();
    }

    public void cleanup() {
        if (shader != null) shader.cleanup();
        if (particleMesh != null) particleMesh.cleanup();
    }
}
