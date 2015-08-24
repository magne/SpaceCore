package org.codehive.spacecore;

import loader.SharedLibraryLoader;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;

    // The window handle
    private long window;

    // Default settings
    public static final int DISPLAY_HEIGHT = 900;
    public static final int DISPLAY_WIDTH = 1400;

    // Renderable items
    PlayerShip TestShip;
    World TestWorld;
    UserInterface UI;

    // Debug var
    float Time;

    // Ship / camera variables
    Vector3f CameraPos = new Vector3f();
    Vector3f CameraTarget = new Vector3f();
    Vector3f CameraUp = new Vector3f();

    // Camera state
    boolean CameraType = false;

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        Main main = null;
        try {
            System.out.println("Keys:");
            System.out.println("down  - Shrink");
            System.out.println("up    - Grow");
            System.out.println("left  - Rotate left");
            System.out.println("right - Rotate right");
            System.out.println("esc   - Exit");

            main = new Main();
            main.create();
            main.run();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        } finally {
            if (main != null) {
                main.destroy();
            }
        }
    }

    public Main() {
        // Do nothing
    }

    private void create() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);     // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);    // the window will be resizable

        // Create the window
        window = glfwCreateWindow(DISPLAY_WIDTH, DISPLAY_HEIGHT, "SpaceCore", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, GL_TRUE);  // We will detect this in our rendering loop
                }
                // Did the camera change?
                if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                    CameraType = !CameraType;
                }
                TestShip.keyboard(window, key, scancode, action, mods);
            }
        });

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - DISPLAY_WIDTH) / 2, (GLFWvidmode.height(vidmode) - DISPLAY_HEIGHT) / 2);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        GLContext.createFromCurrent();

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        //OpenGL
        initGL();
        resizeGL();

        // Create our world and ships
        TestWorld = new World();
        TestShip = new PlayerShip();
        UI = new UserInterface();
    }

    private void destroy() {
        /* GLFW has to be terminated or else the application will run in background */
        // Terminate GLFW and release the GLFWerrorfun
        glfwTerminate();
        errorCallback.release();
    }

    private void run() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities(false);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(window) == GL_FALSE) {
            update();
            render();

            glfwSwapBuffers(window);    // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        // Release window and window callbacks
        glfwDestroyWindow(window);
        keyCallback.release();
    }

    public void update()
    {
        TestShip.Update();
    }

    public void render()
    {
        // Clear screen and load up the 3D matrix state
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        // 3D render
        resizeGL();

        // Move camera to right behind the ship
        //public static void gluLookAt(float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz)
        Time += 0.001f;
        float CDist = 6;

        // Set the camera on the back of the
        TestShip.GetCameraVectors(CameraPos, CameraTarget, CameraUp);

        // Tail-plane camera
        if(CameraType)
        {
            // Extend out the camera by length
            Vector3f Dir = new Vector3f();
            Vector3f.sub(CameraPos, CameraTarget, Dir);
            Dir.normalise();
            Dir.scale(4);
            Dir.y += 0.1f;
            Vector3f.add(CameraPos, Dir, CameraPos);
            CameraPos.y += 1;

            // Little error correction: always make the camera above ground
            if(CameraPos.y < 0.01f)
                CameraPos.y = 0.01f;

            GLU.gluLookAt(CameraPos.x, CameraPos.y, CameraPos.z, CameraTarget.x, CameraTarget.y, CameraTarget.z, CameraUp.x, CameraUp.y, CameraUp.z);
        }
        // Overview
        else
        {
            GLU.gluLookAt(CDist * (float)Math.cos(Time), CDist, CDist * (float)Math.sin(Time), CameraPos.x, CameraPos.y, CameraPos.z, 0, 1, 0);
        }

        // Always face forward
        float Yaw = (float)Math.toDegrees(TestShip.GetYaw());

        // Render all elements
        TestWorld.Render(CameraPos, Yaw);
        TestShip.Render();

        // 2D GUI
        resizeGL2D();
        UI.Render(TestShip.GetRealVelocity(), TestShip.GetTargetVelocity(), TestShip.VEL_MAX);
    }

    private void initGL() {
        // 2D Initialization
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black
        glDisable(GL_DEPTH_TEST);
    }

    private void resizeGL() {
        // 3D Scene
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(45.0f, ((float) DISPLAY_WIDTH / (float) DISPLAY_HEIGHT), 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);

        // Set depth buffer elements
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    private void resizeGL2D() {
        // 2D Scene
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluOrtho2D(0.0f, (float)DISPLAY_WIDTH, (float)DISPLAY_HEIGHT, 0.0f);
        glMatrixMode(GL_MODELVIEW);

        // Set depth buffer elements
        glDisable(GL_DEPTH_TEST);
    }

}
