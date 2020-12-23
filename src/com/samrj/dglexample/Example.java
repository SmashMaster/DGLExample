package com.samrj.dglexample;

import com.samrj.devil.game.Game;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.Texture2D;
import com.samrj.devil.gl.VertexBuffer;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.Camera3DController;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import java.io.IOException;

//Note: We import GL11C and not GL11. This is the core profile, with deprecated
//functionality removed.

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

class Example
{
    private static final float CAMERA_NEAR_Z = 0.125f;
    private static final float CAMERA_FAR_Z = 1024.0f;
    private static final float CAMERA_FOV = Util.toRadians(90.0f);
    private static final float MOVE_SPEED = 4.0f;
    
    private final Vec2i resolution;
    private final Camera3D camera;
    private final Camera3DController controller;
    private final ShaderProgram shader;
    private final VertexBuffer buffer;
    private final Texture2D texture;
    
    private float prevMouseX, prevMouseY;
    private boolean prevMouseValid = false;
    
    Example() throws IOException
    {
        resolution = Game.getResolution();
        camera = new Camera3D(CAMERA_NEAR_Z, CAMERA_FAR_Z, CAMERA_FOV, resolution);
        controller = new Camera3DController(camera);
        controller.target.set(-1.4953849f, 1.6208953f, 2.0854945f);
        controller.setAngles(-0.54916507f, -0.80380523f);
        controller.update();
        
        //This method loads shader.vert and shader.frag, as the vertex and
        //fragment shaders respectively.
        shader = DGL.loadProgram("resources/shader");
        
        //VertexBuffer is a static block of vertices, allocated once.
        //Could use VertexStream if we wanted something more dynamic.
        buffer = DGL.genVertexBuffer(6, -1);
        
        //Set up the variable names used by the vertex shader. Each vertex can
        //have multiple kinds of data: floats, vectors, or matrices.
        Vec3 vPos = buffer.vec3("in_pos");
        Vec3 vColor = buffer.vec3("in_color");
        Vec2 vTexCoord = buffer.vec2("in_tex_coord");
        
        //Build a square out of two triangles.
        buffer.begin();
        vPos.set(0.0f, 0.0f, 0.0f); vColor.set(1.0f, 0.0f, 0.0f); vTexCoord.set(0.0f, 0.0f); buffer.vertex();
        vPos.set(1.0f, 0.0f, 0.0f); vColor.set(0.0f, 1.0f, 0.0f); vTexCoord.set(1.0f, 0.0f); buffer.vertex();
        vPos.set(1.0f, 0.0f, 1.0f); vColor.set(0.0f, 0.0f, 1.0f); vTexCoord.set(1.0f, 1.0f); buffer.vertex();
        
        vPos.set(0.0f, 0.0f, 0.0f); vColor.set(1.0f, 0.0f, 0.0f); vTexCoord.set(0.0f, 0.0f); buffer.vertex();
        vPos.set(1.0f, 0.0f, 1.0f); vColor.set(0.0f, 0.0f, 1.0f); vTexCoord.set(1.0f, 1.0f); buffer.vertex();
        vPos.set(0.0f, 0.0f, 1.0f); vColor.set(1.0f, 1.0f, 1.0f); vTexCoord.set(0.0f, 1.0f); buffer.vertex();
        buffer.end();
        
        //This texture is a greyscale PNG, so it only has one channel. Its
        //format will be GL_RED.
        texture = DGL.loadTex2D("resources/texture.png");
    }
    
    void resize(int width, int height)
    {
        resolution.set(width, height);
        
        //Camera's aspect ratio may change if window is resized.
        camera.setFOV(resolution.x, resolution.y, CAMERA_FOV);
    }
    
    void mouseMoved(float x, float y)
    {
        if (prevMouseValid) //Mouse position isn't valid on first frame.
        {
            float dx = x - prevMouseX;
            float dy = y - prevMouseY;
            controller.mouseDelta(dx, dy);
            controller.update();
        }

        prevMouseX = x; prevMouseY = y;
        prevMouseValid = true;
    }
    
    void key(int key, int action, int mods)
    {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) Game.stop();
    }
    
    void step(float dt)
    {
        Vec3 localMove = new Vec3();
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_A)) localMove.x -= MOVE_SPEED;
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_D)) localMove.x += MOVE_SPEED;
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_LEFT_CONTROL)) localMove.y -= MOVE_SPEED;
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_SPACE)) localMove.y += MOVE_SPEED;
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_S)) localMove.z -= MOVE_SPEED;
        if (Game.getKeyboard().isKeyDown(GLFW_KEY_W)) localMove.z += MOVE_SPEED;
        
        Vec3 worldMove = Vec3.mult(camera.right, localMove.x);
        worldMove.madd(camera.up, localMove.y);
        worldMove.madd(camera.forward, localMove.z);
        
        controller.target.madd(worldMove, dt);
        controller.update();
    }
    
    void render()
    {
        DGL.useProgram(shader);
        shader.uniformMat4("u_projection_matrix", camera.projMat);
        shader.uniformMat4("u_view_matrix", camera.viewMat);
        
        //Assigning the texture variable to texture unit 0.
        //Don't really need to do this every frame.
        shader.uniform1i("u_texture", 0);
        
        texture.bind(GL_TEXTURE0); //Bind the image to texture unit 0.
        
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        DGL.draw(buffer, GL_TRIANGLES);
    }
    
    void destroy()
    {
        DGL.delete(shader, buffer, texture);
    }
}
