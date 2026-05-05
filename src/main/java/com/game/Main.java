package com.game;

import org.lwjgl.*;
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
    private Camera camera;
    private Mesh mesh;
    private int width = 800;
    private int height = 600;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

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

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Minecraft Clone (LWJGL3)", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(window, (window, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, width, height);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // v-sync
        glfwShowWindow(window);

        GL.createCapabilities();
        
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.529f, 0.808f, 0.922f, 0.0f);

        renderer = new Renderer();
        renderer.init();

        camera = new Camera();
        camera.setPosition(0, 0, 5); // move back a bit to see the cube

        // Define a cube
        float[] positions = new float[]{
            // V0
            -0.5f,  0.5f,  0.5f,
            // V1
            -0.5f, -0.5f,  0.5f,
            // V2
             0.5f, -0.5f,  0.5f,
            // V3
             0.5f,  0.5f,  0.5f,
            // V4
            -0.5f,  0.5f, -0.5f,
            // V5
             0.5f,  0.5f, -0.5f,
            // V6
            -0.5f, -0.5f, -0.5f,
            // V7
             0.5f, -0.5f, -0.5f,
        };
        float[] colors = new float[]{
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top Face
            4, 0, 3, 5, 4, 3,
            // Right face
            3, 2, 7, 5, 3, 7,
            // Left face
            6, 1, 0, 6, 0, 4,
            // Bottom face
            2, 1, 6, 2, 6, 7,
            // Back face
            7, 6, 4, 7, 4, 5,
        };
        mesh = new Mesh(positions, colors, indices);
    }

    private void loop() {
        double lastTime = glfwGetTime();

        while ( !glfwWindowShouldClose(window) ) {
            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            input(deltaTime);
            renderer.render(mesh, camera, width, height);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void input(float deltaTime) {
        float speed = 2.0f * deltaTime;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.movePosition(0, 0, -speed);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.movePosition(0, 0, speed);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.movePosition(-speed, 0, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.movePosition(speed, 0, 0);
        }
        // Basic rotation for demo
        camera.moveRotation(0, 50.0f * deltaTime, 0);
    }

    private void cleanup() {
        if (renderer != null) renderer.cleanup();
        if (mesh != null) mesh.cleanup();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
