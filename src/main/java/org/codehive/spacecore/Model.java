package org.codehive.spacecore;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Model {
    public List<Vector3f> vertices = new ArrayList<>();
    public List<Vector3f> normals = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public Model() {
    }

    // Render the model or shape
    public void Render() {
        // Set width to a single line
        GL11.glLineWidth(1);

        // Change rendermode
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            } else {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            }

            // Randomize surface color a bit
            Random SurfaceRand = new Random(123456);

            GL11.glBegin(GL11.GL_TRIANGLES);
            for (Face face : faces) {
                // Always make black when in line mode)
                if (i == 0) {
                    GL11.glColor3f(0.8f, 0.8f, 0.5f + 0.5f * (SurfaceRand.nextFloat()));
                } else {
                    GL11.glColor3f(0.4f, 0.4f, 0.2f + 0.2f * (SurfaceRand.nextFloat()));
                }

                // Randomize the color a tiny bit
                Vector3f v1 = vertices.get((int) face.vertex.x - 1);
                GL11.glVertex3f(v1.x, v1.y, v1.z);
                Vector3f v2 = vertices.get((int) face.vertex.y - 1);
                GL11.glVertex3f(v2.x, v2.y, v2.z);
                Vector3f v3 = vertices.get((int) face.vertex.z - 1);
                GL11.glVertex3f(v3.x, v3.y, v3.z);
            }
            GL11.glEnd();
        }
    }
}
