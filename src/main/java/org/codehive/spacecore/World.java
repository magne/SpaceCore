package org.codehive.spacecore;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Star point
class StarPoint {
    Vector3f Pt = new Vector3f();
    Vector3f Color = new Vector3f();
    float Scale;
}

// Models to render
class Models {
    Vector3f Pt = new Vector3f();
    Model model;
    float Yaw;
}

/**
 * @author jbridon
 * A very simple world that is always rendered at Y = 0
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
        for(int i = 0; i < 1000; i++)
        {
            // New star
            StarPoint star = new StarPoint();

            // New position
            double u = 2f * Math.random() - 1f;
            double v = Math.random() * 2 * Math.PI;

            star.Pt.x = (float)(Math.sqrt(1f - Math.pow(u, 2.0)) * Math.cos(v));
            star.Pt.z = (float)(Math.sqrt(1f - Math.pow(u, 2.0)) * Math.sin(v));
            star.Pt.y = (float)Math.abs(u);
            star.Pt.scale(SkyboxSize / 2); // Scale out from the center

            // Scale up
            star.Scale = 3f * (float)Math.random();

            // Color
            float Gray = 0.5f + 0.5f * (float)Math.random();
            star.Color.x = Gray;
            star.Color.y = Gray;
            star.Color.z = Gray;

            // Push star into list
            StarList.add(star);
        }

        // Load a bunch of models
        ModelList = new ArrayList<>();

        // Load road strip
        Models model = new Models();
        model.model = OBJLoader.load("resources/spacecore/Road.obj");
        model.Yaw = 0f;
        ModelList.add(model);

        // Load a bunch of rocks...
        for (int i = 0; i < 100; i++) {
            int index = (int)(Math.random() * 5f) + 1;

            Models newModel = new Models();
            newModel.model = OBJLoader.load("resources/spacecore/Rock" + index + ".obj");
            newModel.Yaw = (float)(Math.random() * 2.0 * Math.PI);

            newModel.Pt.x = (float)(Math.random() * 2.0 - 1.0) * SkyboxSize;
            newModel.Pt.z = (float)(Math.random() * 2.0 - 1.0) * SkyboxSize;
            newModel.Pt.y = 0f;

            ModelList.add(newModel);
        }
    }

    // Render the ship
    public void Render(org.joml.Vector3f pos, float yaw) {
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
            RenderStars();
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
        for (Models model : ModelList) {
            GL11.glPushMatrix();
            {
                // Render ground and right below
                GL11.glTranslatef(model.Pt.x, model.Pt.y, model.Pt.z);
                GL11.glRotatef((float) Math.toDegrees(model.Yaw), 0, 1, 0);
                RenderModel(model.model);
            }
            GL11.glPopMatrix();
        }
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

    private void RenderStars() {
        // Render all stars
        for(StarPoint Star : StarList)
        {
            GL11.glPointSize(Star.Scale);
            GL11.glColor3f(Star.Color.x, Star.Color.y, Star.Color.z);
            GL11.glBegin(GL11.GL_POINTS);
            GL11.glVertex3f(Star.Pt.x, Star.Pt.y, Star.Pt.z);
            GL11.glEnd();
        }
    }

    // Render a model or shape
    private void RenderModel(Model model) {
        // Set width to a single line
        GL11.glLineWidth(1);

        // Change rendermode
        for(int i = 0; i < 2; i++)
        {
            if(i == 0)
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            else
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            // Randomize surface color a bit
            Random SurfaceRand = new Random(123456);

            GL11.glBegin(GL11.GL_TRIANGLES);
            for (Face face : model.faces)
            {
                // Always make black when in line mode)
                if(i == 0)
                    GL11.glColor3f(0.8f, 0.8f, 0.5f + 0.5f * (SurfaceRand.nextFloat()));
                else
                    GL11.glColor3f(0.4f, 0.4f, 0.2f + 0.2f * (SurfaceRand.nextFloat()));

                // Randomize the color a tiny bit
                Vector3f v1 = model.vertices.get((int) face.vertex.x - 1);
                GL11.glVertex3f(v1.x, v1.y, v1.z);
                Vector3f v2 = model.vertices.get((int) face.vertex.y - 1);
                GL11.glVertex3f(v2.x, v2.y, v2.z);
                Vector3f v3 = model.vertices.get((int) face.vertex.z - 1);
                GL11.glVertex3f(v3.x, v3.y, v3.z);
            }
            GL11.glEnd();
        }
    }
}
