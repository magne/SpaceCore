package org.codehive.spacecore;

import org.lwjgl.util.vector.Vector3f;

public class Face {
    public Vector3f vertex = new Vector3f();

    public Face(Vector3f vertex) {
        this.vertex = vertex;
    }
}
