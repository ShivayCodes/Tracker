package com.game;

import com.game.graphics.HandRenderer;
import com.game.graphics.HudRenderer;
import com.game.graphics.ParticleSystem;
import com.game.graphics.Texture;
import com.game.world.World;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;
    private Renderer renderer;
    private HudRenderer hudRenderer;
    private HandRenderer handRenderer;
    private ParticleSystem particleSystem;
    private Camera camera;
    private World world;
    private Texture atlas;
    private Texture hudTexture;
    private int width = 800;
    private int height = 600;

    private boolean mouseLocked = true;
    private double lastMouseX, lastMouseY;
    private float velocityY = 0;
    private boolean isGrounded = false;
    
    private Vector3f targetBlock = null;
    private Vector3f targetAirBlock = null;
    private com.game.entity.Entity targetEntity = null;
    private int selectedSlot = 0; // 0 to 8
    
    private float time = 0.0f;
    private static final int RENDER_DISTANCE = 4;
    
    private float bobbingTime = 0;
    private boolean isMoving = false;

    public void run() {
        try {
            init();
            loop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void init() throws Exception {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !glfwInit() ) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "Minecraft Clone (LWJGL3)", NULL, NULL);
        if ( window == NULL ) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(window, (wnd, w, h) -> {
            width = w; height = h; glViewport(0, 0, width, height);
        });

        glfwSetKeyCallback(window, (wnd, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                mouseLocked = !mouseLocked;
                glfwSetInputMode(window, GLFW_CURSOR, mouseLocked ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
            }
            if ( key == GLFW_KEY_SPACE && action == GLFW_PRESS && isGrounded) {
                velocityY = 6.0f; isGrounded = false;
            }
            if (action == GLFW_PRESS && key >= GLFW_KEY_1 && key <= GLFW_KEY_9) {
                selectedSlot = key - GLFW_KEY_1;
            }
        });

        glfwSetMouseButtonCallback(window, (wnd, button, action, mods) -> {
            if (action == GLFW_PRESS && mouseLocked) {
                handRenderer.punch();
                if (button == GLFW_MOUSE_BUTTON_LEFT) { interact(true); } 
                else if (button == GLFW_MOUSE_BUTTON_RIGHT) { interact(false); }
            } else if (action == GLFW_PRESS && !mouseLocked) {
                mouseLocked = true; glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            }
        });

        glfwSetCursorPosCallback(window, (wnd, xpos, ypos) -> {
            if (mouseLocked) {
                double dx = xpos - lastMouseX; double dy = ypos - lastMouseY;
                camera.moveRotation((float) dy * 0.1f, (float) dx * 0.1f, 0);
                Vector3f rot = camera.getRotation();
                if (rot.x > 89.0f) rot.x = 89.0f; if (rot.x < -89.0f) rot.x = -89.0f;
            }
            lastMouseX = xpos; lastMouseY = ypos;
        });

        glfwMakeContextCurrent(window); glfwSwapInterval(1); glfwShowWindow(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        try ( MemoryStack stack = stackPush() ) {
            DoubleBuffer x = stack.mallocDouble(1); DoubleBuffer y = stack.mallocDouble(1);
            glfwGetCursorPos(window, x, y); lastMouseX = x.get(); lastMouseY = y.get();
        }

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST); glEnable(GL_CULL_FACE); glCullFace(GL_BACK);
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        renderer = new Renderer(); renderer.init();
        hudRenderer = new HudRenderer(); hudRenderer.init();
        handRenderer = new HandRenderer(); handRenderer.init();
        particleSystem = new ParticleSystem(); particleSystem.init();

        camera = new Camera(); camera.setPosition(0, 20, 0);
        world = new World();
        atlas = new Texture("src/main/resources/textures/atlas.png");
        hudTexture = new Texture("src/main/resources/textures/hud.png");

        float u0 = 0.0f, u1 = 1.0f/5.0f, v0 = 0.0f, v1 = 1.0f;
        float[] positions = new float[]{ -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f };
        float[] textCoords = new float[]{ u0,v0, u0,v1, u1,v1, u1,v0, u0,v0, u0,v1, u1,v1, u1,v0 };
        float[] ao = new float[8]; for(int i=0; i<8; i++) ao[i] = 1.0f;
        int[] indices = new int[]{ 0,1,3, 3,1,2, 4,0,3, 5,4,3, 3,2,7, 5,3,7, 6,1,0, 6,0,4, 2,1,6, 2,6,7, 7,6,4, 7,4,5 };
        Mesh entityMesh = new Mesh(positions, textCoords, new float[24], ao, indices);
        com.game.entity.Entity companion = new com.game.entity.Entity(entityMesh);
        companion.setPosition(0, 15, -5); world.addEntity(companion);
    }

    private void loop() {
        double lastTime = glfwGetTime();
        while ( !glfwWindowShouldClose(window) ) {
            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime); lastTime = currentTime;

            if (mouseLocked) { input(deltaTime); physics(deltaTime); updateTarget(); }
            world.update(deltaTime); world.updateChunks(camera.getPosition(), RENDER_DISTANCE);
            particleSystem.update(deltaTime);

            time += deltaTime * 0.05f; if (time > Math.PI * 2) time -= Math.PI * 2;
            Vector3f sunDir = new Vector3f((float)Math.sin(time), (float)Math.cos(time), 0.5f).normalize();
            float skyFactor = Math.max(sunDir.y, -0.2f) + 0.2f; skyFactor = Math.min(skyFactor, 1.0f);
            Vector3f skyColor = new Vector3f(0.529f, 0.808f, 0.922f).mul(skyFactor); glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
            float ambientF = Math.max(sunDir.y, 0.1f); Vector3f ambientL = new Vector3f(ambientF, ambientF, ambientF);

            if (isMoving && isGrounded) bobbingTime += deltaTime * 12.0f; else bobbingTime = 0;
            float bobO = (float) Math.sin(bobbingTime) * 0.05f; Vector3f origPos = new Vector3f(camera.getPosition()); camera.getPosition().y += bobO;

            renderer.render(world, camera, width, height, atlas, targetBlock, sunDir, ambientL, skyColor);
            particleSystem.render(camera.getProjectionMatrix((float)Math.toRadians(60), width, height, 0.01f, 1000f), camera.getViewMatrix(), atlas);
            
            camera.setPosition(origPos.x, origPos.y, origPos.z);
            handRenderer.render(width, height, atlas, selectedSlot + 1, deltaTime, isMoving && isGrounded);
            hudRenderer.render(width, height, hudTexture, selectedSlot);

            glfwSwapBuffers(window); glfwPollEvents();
        }
    }

    private void input(float deltaTime) {
        float speed = 5.0f * deltaTime; Vector3f pos = camera.getPosition(); Vector3f newPos = new Vector3f(pos); isMoving = false;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) { newPos.x += (float) Math.sin(Math.toRadians(camera.getRotation().y)) * -1.0f * -speed; newPos.z += (float) Math.cos(Math.toRadians(camera.getRotation().y)) * -speed; isMoving = true; }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) { newPos.x += (float) Math.sin(Math.toRadians(camera.getRotation().y)) * -1.0f * speed; newPos.z += (float) Math.cos(Math.toRadians(camera.getRotation().y)) * speed; isMoving = true; }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) { newPos.x += (float) Math.sin(Math.toRadians(camera.getRotation().y - 90)) * -1.0f * -speed; newPos.z += (float) Math.cos(Math.toRadians(camera.getRotation().y - 90)) * -speed; isMoving = true; }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) { newPos.x += (float) Math.sin(Math.toRadians(camera.getRotation().y - 90)) * -1.0f * speed; newPos.z += (float) Math.cos(Math.toRadians(camera.getRotation().y - 90)) * speed; isMoving = true; }
        Vector3f testPos = new Vector3f(newPos.x, pos.y, pos.z); if (!world.checkCollision(testPos)) pos.x = newPos.x;
        testPos = new Vector3f(pos.x, pos.y, newPos.z); if (!world.checkCollision(testPos)) pos.z = newPos.z;
    }

    private void physics(float deltaTime) {
        Vector3f pos = camera.getPosition(); velocityY -= 15.0f * deltaTime; Vector3f nextPos = new Vector3f(pos.x, pos.y + velocityY * deltaTime, pos.z);
        if (world.checkCollision(nextPos)) { if (velocityY < 0) isGrounded = true; velocityY = 0; } else { pos.y = nextPos.y; isGrounded = false; }
        if (pos.y < -10) { pos.y = 20; velocityY = 0; }
    }

    private void updateTarget() {
        Vector3f pos = camera.getPosition(); Vector3f dir = new Vector3f(); float yaw = (float) Math.toRadians(camera.getRotation().y), pitch = (float) Math.toRadians(camera.getRotation().x);
        dir.x = (float) (Math.sin(yaw) * Math.cos(pitch) * -1.0f); dir.y = (float) Math.sin(-pitch); dir.z = (float) (Math.cos(yaw) * Math.cos(pitch) * -1.0f); dir.normalize();
        float step = 0.1f; Vector3f currentPos = new Vector3f(pos.x, pos.y, pos.z); Vector3f lastAirPos = new Vector3f(currentPos); targetBlock = null; targetAirBlock = null; targetEntity = null;
        for (float i = 0; i < 5.0f; i += step) {
            currentPos.add(dir.x * step, dir.y * step, dir.z * step);
            for (com.game.entity.Entity e : world.getEntities()) { if (currentPos.distance(e.getPosition()) < 0.8f) { targetEntity = e; return; } }
            int bx = (int) Math.floor(currentPos.x); int by = (int) Math.floor(currentPos.y); int bz = (int) Math.floor(currentPos.z);
            if (world.getBlock(bx, by, bz) != 0) {
                targetBlock = new Vector3f(bx, by, bz); targetAirBlock = new Vector3f((int)Math.floor(lastAirPos.x), (int)Math.floor(lastAirPos.y), (int)Math.floor(lastAirPos.z)); break;
            }
            lastAirPos.set(currentPos);
        }
    }

    private void interact(boolean isBreak) {
        if (targetEntity != null && isBreak) { world.getEntities().remove(targetEntity); targetEntity = null; return; }
        if (targetBlock != null) {
            if (isBreak) {
                int bx = (int)targetBlock.x, by = (int)targetBlock.y, bz = (int)targetBlock.z;
                world.setBlock(bx, by, bz, (byte) 0);
                particleSystem.spawn(new Vector3f(bx, by, bz));
            } else if (targetAirBlock != null) {
                byte blockType = (byte) (selectedSlot + 1);
                world.setBlock((int)targetAirBlock.x, (int)targetAirBlock.y, (int)targetAirBlock.z, blockType);
            }
        }
    }

    private void cleanup() {
        if (renderer != null) renderer.cleanup(); if (hudRenderer != null) hudRenderer.cleanup(); if (handRenderer != null) handRenderer.cleanup(); if (particleSystem != null) particleSystem.cleanup();
        if (world != null) world.cleanup(); if (atlas != null) atlas.cleanup(); if (hudTexture != null) hudTexture.cleanup();
        glfwFreeCallbacks(window); glfwDestroyWindow(window); glfwTerminate(); glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) { new Main().run(); }
}
