package org.codehive.spacecore;

import org.joml.Vector3f;

import java.io.*;

public class OBJLoader {
    public static Model loadModel(Reader r) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        Model m = new Model();
        String line;
        while ((line = reader.readLine()) != null) {
            final String[] values = line.split(" ");
            if ("v".equals(values[0])) {
                float x = Float.valueOf(values[1]);
                float y = Float.valueOf(values[2]);
                float z = Float.valueOf(values[3]);
                m.vertices.add(new Vector3f(x, y, z));
            } else if ("f".equals(values[0])) {
                try {
                    Vector3f vertexIndices = new Vector3f(
                            Float.valueOf(values[1].split("/")[0]),
                            Float.valueOf(values[2].split("/")[0]),
                            Float.valueOf(values[3].split("/")[0]));
                    m.faces.add(new Face(vertexIndices));
                } catch (Exception e) {

                }
            }
        }
        reader.close();
        return m;
    }

    public static Model loadResource(String name) {
        final InputStream resource = ClassLoader.getSystemResourceAsStream(name);
        if (resource == null) {
            System.err.println(String.format("Resource %s not found", name));
            System.exit(1);
        }
        try {
            return loadModel(new InputStreamReader(resource));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
