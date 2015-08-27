package org.codehive.spacecore;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

// Star point
class StarPoint {
    Vector3f Pt = new Vector3f();
    Vector3f Color = new Vector3f();
    float Scale;

    public void Render() {
        GL11.glPointSize(Scale);
        GL11.glColor3f(Color.x, Color.y, Color.z);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex3f(Pt.x, Pt.y, Pt.z);
        GL11.glEnd();
    }
}

// Models to render
class Models {
    private final Model model;
    private final Vector3f point;
    private final float yaw;

    public Models(String resource) {
        this(resource, new Vector3f(), 0.0f);
    }

    public Models(String resource, Vector3f point, float yaw) {
        this(OBJLoader.loadResource(resource), point, yaw);
    }

    public Models(Model model, Vector3f point, float yaw) {
        this.model = model;
        this.point = point;
        this.yaw = yaw;
    }

    public void Render() {
        GL11.glPushMatrix();
        {
            // Render ground and right below
            GL11.glTranslatef(point.x, point.y, point.z);
            GL11.glRotatef((float) Math.toDegrees(yaw), 0, 1, 0);
            model.Render();
        }
        GL11.glPopMatrix();
    }
}

/**
 * @author jbridon
 *         A very simple world that is always rendered at Y = 0
 */
public class World {
    // Box size
    public static float SkyboxSize = 32.0f;
    public static float WorldSize = 1024.0f;

    // List of stars (list of Vector3f)
    List<StarPoint> StarList;

    // Load a bunch of objects
    List<Models> ModelList;

    public World() {
        // Create a list and fill with a bunch of stars
        StarList = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            // New star
            StarPoint star = new StarPoint();

            // New position
            double u = 2f * Math.random() - 1f;
            double v = Math.random() * 2 * Math.PI;

            star.Pt.x = (float) (Math.sqrt(1f - Math.pow(u, 2.0)) * Math.cos(v));
            star.Pt.z = (float) (Math.sqrt(1f - Math.pow(u, 2.0)) * Math.sin(v));
            star.Pt.y = (float) Math.abs(u);
            star.Pt.mul(SkyboxSize / 2); // Scale out from the center

            // Scale up
            star.Scale = 3f * (float) Math.random();

            // Color
            float Gray = 0.5f + 0.5f * (float) Math.random();
            star.Color.x = Gray;
            star.Color.y = Gray;
            star.Color.z = Gray;

            // Push star into list
            StarList.add(star);
        }

        // Load a bunch of models
        ModelList = new ArrayList<>();

        // Load road strip
        ModelList.add(new Models("spacecore/Road.obj"));

        // Load a bunch of rocks...
        for (int i = 0; i < 100; i++) {
            int index = (int) (Math.random() * 5f) + 1;

            String resource = String.format("spacecore/Rock%s.obj", index);
            Vector3f point = new Vector3f(
                    (float) (Math.random() * 2.0 - 1.0) * SkyboxSize,
                    0.0f,
                    (float) (Math.random() * 2.0 - 1.0) * SkyboxSize
            );
            float yaw = (float) (Math.random() * 2.0 * Math.PI);
            ModelList.add(new Models(resource, point, yaw));
        }
    }

    // Render the ship
    public void Render(Vector3f pos, float yaw) {
        // Rotate (yaw) as needed so the player always faces non-corners
        GL11.glPushMatrix();
        {
            // Rotate and translate
            GL11.glTranslatef(pos.x, pos.y, pos.z);
            GL11.glRotatef(yaw, 0f, 1f, 0f);

            // Render the skybox and stars
            RenderSkybox();
        }
        // Be done
        GL11.glPopMatrix();

        // Render out the stars
        GL11.glPushMatrix();
        {
            // Show stars
            GL11.glTranslatef(pos.x, pos.y * 0.99f, pos.z);
            StarList.forEach(StarPoint::Render);
        }
        // Be done
        GL11.glPopMatrix();

        // Draw stars
        GL11.glPushMatrix();
        {
            // Render ground and right below
            GL11.glTranslatef(pos.x, 0, pos.z);

            Vector3f color = new Vector3f(236.0f / 255.0f, 200.0f / 255.0f, 122.0f / 255.0f);
            RenderGround(WorldSize, color);
        }
        GL11.glPopMatrix();

        // Render all the objects
        ModelList.forEach(Models::Render);
    }

    // Draw the bottom level
    private void RenderGround(float worldSize, Vector3f color) {
        // Translate to position
        GL11.glPushMatrix();

        // Set the ship color to red for now
        GL11.glColor3f(color.x, color.y, color.z);
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex3f(-worldSize, 0, -worldSize);
            GL11.glVertex3f(worldSize, 0, -worldSize);
            GL11.glVertex3f(worldSize, 0, worldSize);
            GL11.glVertex3f(-worldSize, 0, worldSize);
        }
        GL11.glEnd();

        // Done
        GL11.glPopMatrix();
    }

    private void RenderSkybox() {
        // Define the top and bottom color
        Vector3f topColor = new Vector3f(204f / 255f, 255f / 255f, 255f / 255f);
        Vector3f bottomColor = new Vector3f(207f / 255f, 179f / 255f, 52f / 255f);

        // Save matrix
        GL11.glPushMatrix();

        // Draw out top side
        GL11.glBegin(GL11.GL_QUADS);
        {
            // Polygon and texture map
            // Top has one constant color
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(SkyboxSize, SkyboxSize, -SkyboxSize);
            GL11.glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
            GL11.glVertex3f(SkyboxSize, -SkyboxSize, -SkyboxSize);
            GL11.glVertex3f(SkyboxSize, -SkyboxSize, SkyboxSize);
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(SkyboxSize, SkyboxSize, SkyboxSize);
        }
        GL11.glEnd();

        // Draw out the right side
        GL11.glBegin(GL11.GL_QUADS);
        {
            // Polygon & texture map
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(-SkyboxSize, SkyboxSize, SkyboxSize);
            GL11.glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
            GL11.glVertex3f(-SkyboxSize, -SkyboxSize, SkyboxSize);
            GL11.glVertex3f(-SkyboxSize, -SkyboxSize, -SkyboxSize);
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(-SkyboxSize, SkyboxSize, -SkyboxSize);
        }
        GL11.glEnd();

        // Draw out the front side
        GL11.glBegin(GL11.GL_QUADS);
        {
            // Polygon & texture map
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(SkyboxSize, SkyboxSize, SkyboxSize);
            GL11.glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
            GL11.glVertex3f(SkyboxSize, -SkyboxSize, SkyboxSize);
            GL11.glVertex3f(-SkyboxSize, -SkyboxSize, SkyboxSize);
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(-SkyboxSize, SkyboxSize, SkyboxSize);
        }
        GL11.glEnd();

        // Draw out the back side
        GL11.glBegin(GL11.GL_QUADS);
        {
            // Polygon & texture map
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(-SkyboxSize, SkyboxSize, -SkyboxSize);
            GL11.glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
            GL11.glVertex3f(-SkyboxSize, -SkyboxSize, -SkyboxSize);
            GL11.glVertex3f(SkyboxSize, -SkyboxSize, -SkyboxSize);
            GL11.glColor3f(topColor.x, topColor.y, topColor.z);
            GL11.glVertex3f(SkyboxSize, SkyboxSize, -SkyboxSize);
        }
        GL11.glEnd();

        // Place back matrix
        GL11.glPopMatrix();
    }
}
