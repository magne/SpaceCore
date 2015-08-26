package org.codehive.spacecore;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jbridon
 * A very simple player ship that they can move around...
 */
public class PlayerShip
{
    // Global position and local vectors
    private Vector3f Position;
    private Vector3f Forward, Up, Right;

    // Pitch and rolls
    private float Pitch, Roll;

    private float dPitch;
    private float dRoll;

    // TEST VARIABLE
    Quaternionf QResult;
    
    // Ship variable
    Model model;

    // Player ship has a current velocity and target velocity
    float RealVelocity, TargetVelocity;
    
    // Velocities
    public static final float VEL_dMAX = 0.005f;
    public static final float VEL_MAX = 0.15f;
    
    // Did we crash or bounce?
    boolean Bounced;
    boolean Crashed;
    
    // Constructor does nothing
    public PlayerShip()
    {
        // Default data
        InitShip();

        final String file = "resources/spacecore/Sample.obj";
        model = OBJLoader.load(file);
    }
    
    public void InitShip()
    {
        // Default position slight above ground
        Position = new Vector3f(0, 0.1f, 0);
        
        // Set forward to Z+
        Forward = new Vector3f(0, 0, 1);
        Up = new Vector3f(0, 1, 0);
        Right = new Vector3f(-1, 0, 0);
        
        // Blah testing...
        QResult = new Quaternionf();
        
        // Default velocities to zero
        RealVelocity = TargetVelocity = 0;
        Pitch = Roll = 0.0f;
        
        // No known states
        Bounced = false;
        Crashed = false;
    }

    public void keyboard(long window, int key, int scancode, int action, int mods)
    {
        if (action == GLFW.GLFW_PRESS) {
            // Possible angle change
            dPitch = 0;
            dRoll = 0;

            switch (key) {
                // Changing pitch and roll (Pitch is on Z axis)
                case GLFW.GLFW_KEY_W :
                case GLFW.GLFW_KEY_UP :
                    dPitch -= 0.03;
                    break;
                case GLFW.GLFW_KEY_S :
                case GLFW.GLFW_KEY_DOWN :
                    dPitch += 0.03;
                    break;
                case GLFW.GLFW_KEY_A :
                case GLFW.GLFW_KEY_LEFT :
                    dRoll += 0.05;
                    break;
                case GLFW.GLFW_KEY_D :
                case GLFW.GLFW_KEY_RIGHT :
                    dRoll -= 0.05;
                    break;

                // Update velocities
                case GLFW.GLFW_KEY_R :
                case GLFW.GLFW_KEY_PAGE_UP :
                    TargetVelocity += VEL_dMAX;
                    break;
                case GLFW.GLFW_KEY_F :
                case GLFW.GLFW_KEY_PAGE_DOWN :
                    TargetVelocity -= VEL_dMAX;
                    break;
            }

            // Save the total pitch and roll
            Pitch += dPitch;
            Roll += dRoll;

            // Bounds check the target velocity
            if(TargetVelocity > VEL_MAX)
                TargetVelocity = VEL_MAX;
            else if(TargetVelocity < 0.0f)
                TargetVelocity = 0;
        }
    }
    
    // Check for user events
    public void Update()
    {
        // If we ever crash, reset everything
        if(Crashed)
        {
            System.out.println("Crashed!");
            InitShip();
        }
        
        // Update the real velocity over time
        // NOTE: The delta has to be smaller than the target velocity
        if(TargetVelocity > RealVelocity)
            RealVelocity += VEL_dMAX * 0.5f;
        else if(TargetVelocity < RealVelocity)
            RealVelocity -= VEL_dMAX * 0.5f;
        
        /*** EULER APPROACH with pure angles (bad) ***/
        
        //forward = unit(forward *  cos(angle) + up * sin(angle));
        //up = right.cross(forward);
        Forward.mul((float)Math.cos(dPitch));
        Up.mul((float)Math.sin(dPitch));
        Forward.add(Up);
        Right.cross(Forward, Up);

        // Normalize
        Forward.normalize();
        Up.normalize();
        
        //right = unit(right * cos(angle) + up * sin(angle));
        //up = right.cross(forward);
        Right.mul((float)Math.cos(dRoll));
        Up.mul((float)Math.sin(dRoll));
        Right.add(Up);
        Right.cross(Forward, Up);

        // Normalize
        Right.normalize();
        Up.normalize();
        
        // Position changes over time based on the forward vector
        // Note we have a tiny bit of lift added
        Vector3f ForwardCopy = new Vector3f(Forward);
        ForwardCopy.mul(RealVelocity);

        // Gravity factor and normalized velocity
        float Gravity = 0.05f;
        float NVelocity = Math.min((RealVelocity / VEL_MAX) * 3, 1); // Note: 4 is to make 1/4 the "total lift" point
        
        // Computer the "up" fource that attempts to counter gravity
        Vector3f TotalUp = new Vector3f(Up);
        TotalUp.mul(NVelocity * Gravity); // Linear relationship: the faster, the more lift
        TotalUp.y -= Gravity;
        
        // Add the lift component to the forward vector
        //Vector3f.add(ForwardCopy, TotalUp, ForwardCopy);
        Position.add(ForwardCopy);

        // Build two quats, for a global roll then pitch
        Quaternionf QRoll = new Quaternionf();
        QRoll.set(new AxisAngle4f(dRoll, Forward));
        Quaternionf QPitch = new Quaternionf();
        QPitch.set(new AxisAngle4f(-dPitch, Right));
        
        // Note: we must explicitly multiply out each dQ, not just the total
        QResult.mul(QRoll);
        QResult.mul(QPitch);
        QResult.normalize();
    }
    
    // Render the ship
    public void Render()
    {
        // Translate to position
        GL11.glPushMatrix();
        GL11.glTranslatef(Position.x, Position.y, Position.z);
        
        // Why isn't this a built-in feature of LWJGL
        Matrix4f QMatrix = QResult.get(new Matrix4f());

        FloatBuffer Buffer = BufferUtils.createFloatBuffer(16);
        QMatrix.get(Buffer);
        Buffer.position(0);
        
        GL11.glMultMatrixf(Buffer);
        
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glColor3f(1, 0.5f, 0.5f);
            GL11.glVertex3f(0, 0, 0);
            GL11.glVertex3f(1, 0, 0);

            GL11.glColor3f(0.5f, 1, 0.5f);
            GL11.glVertex3f(0, 0, 0);
            GL11.glVertex3f(0, 1, 0);

            GL11.glColor3f(0.5f, 0.5f, 1);
            GL11.glVertex3f(0, 0, 0);
            GL11.glVertex3f(0, 0, 1);
        }
        GL11.glEnd();
        
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
        
        // Reset back to regular faces
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        
        // Done
        GL11.glPopMatrix();
        
        // Render the shadow (view-volume)
        // Note: we render the shadow independent of the model's translation and rotation
        // THOUGH NOTE: we do translate the shadow up a tiny bit off the ground so it doesn't z-fight
        GL11.glPushMatrix();
        {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(-1.0f, -1.0f);

            GL11.glTranslatef(0, 0.001f, 0);
            renderShadow(Position);

            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
        GL11.glPopMatrix();
    }
    
    public void renderShadow(Vector3f Translation)
    {
        // Explicitly copy...
        List<Vector3f> vertices = new ArrayList<>();
        for(Vector3f Vertex : model.vertices)
        {
            // Apply rotation then translation
            Vector3f vt = new Vector3f(Vertex);
            vt.rotate(QResult);
            vt.add(Translation);
            vertices.add(vt);
        }
        
        // NOTE: WE DO THIS COLLISION TEST HERE SINCE WE HAVE THE
        // TRANSLATION MODEL (i.e. global data)
        
        // Make sure the model never goes below the surface, and if
        // it does, push it back up, but if it does too much, crash ship
        float MaxD = 0.0f;
        for (Vector3f Vertex : vertices)
        {
            if(Vertex.y < 0.0f && Vertex.y < MaxD)
                MaxD = Vertex.y;
        }
        
        // Physics check: did we crash, or did we bounce
        /*if(Math.abs(MaxD) > 0.02)
            Crashed = true;
        else*/ if(Math.abs(MaxD) > 0.0f)
        {
            Position.y += Math.abs(MaxD);
            Bounced = true;
        }
        
        // Assume the light source is just high above
        Vector3f LightPos = new Vector3f(0, 1000, 0);
        
        // For each triangle, project onto the plane XZ-plane
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (Face face : model.faces)
        {
            // Per-face color
            GL11.glColor3f(0.4f, 0.4f, 0.4f);

            // Draw the mode components
            Vector3f v1 = getPlaneIntersect(vertices.get((int) face.vertex.x - 1), LightPos);
            GL11.glVertex3f(v1.x, v1.y, v1.z);
            Vector3f v2 = getPlaneIntersect(vertices.get((int) face.vertex.y - 1), LightPos);
            GL11.glVertex3f(v2.x, v2.y, v2.z);
            Vector3f v3 = getPlaneIntersect(vertices.get((int) face.vertex.z - 1), LightPos);
            GL11.glVertex3f(v3.x, v3.y, v3.z);
        }
        GL11.glEnd();
    }

    // Returns the intersection point of the vector (described as two points)
    // onto the y=0 plane (or simply the XZ plane)
    public Vector3f getPlaneIntersect(Vector3f vf, Vector3f vi)
    {
        Vector3f LineDir = new Vector3f(vf).sub(vi);
        LineDir.normalize();
        
        Vector3f PlaneNormal = new Vector3f(0, 1, 0);
        Vector3f neg_Vi = new Vector3f(-vi.x, -vi.y, -vi.z);
        
        float d = neg_Vi.dot(PlaneNormal) / LineDir.dot(PlaneNormal);
        Vector3f pt = new Vector3f(LineDir);
        pt.mul(d);
        pt.add(vi);
        
        return pt;
    }

    // Get the look vectors for the camera
    public void GetCameraVectors(org.joml.Vector3f CameraPos, org.joml.Vector3f CameraTarget, org.joml.Vector3f CameraUp)
    {
        // Copy all vectors as needed for the camera
        CameraPos.set(Position.x, Position.y, Position.z);
        CameraTarget.set(Forward.x + Position.x, Forward.y + Position.y, Forward.z + Position.z);
        CameraUp.set(Up.x, Up.y, Up.z);
    }
    
    // Get yaw of ship
    public float GetYaw()
    {
        // Cast down the forward and right vectors onto the XZ plane
        Vector3f FFlat = new Vector3f(Forward.x, 0f, Forward.z);
        Vector3f RFlat = new Vector3f(1f, 0f, 0f);
        
        // Angle between
        float Ang = RFlat.angle(FFlat);
        if (RFlat.cross(FFlat, new Vector3f()).y < 0)
            Ang = (float)(Math.PI * 2.0) - Ang;
        return Ang;
    }
    
    // Get velocity
    public float GetRealVelocity()
    {
        return RealVelocity;
    }
    
    public float GetTargetVelocity()
    {
        return TargetVelocity;
    }
}