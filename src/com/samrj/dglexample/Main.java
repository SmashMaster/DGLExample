package com.samrj.dglexample;

import com.samrj.devil.game.Game;
import com.samrj.devil.gl.DGL;
import org.lwjgl.system.APIUtil;

//Note: We import GL11C and not GL11. This is the core profile, with deprecated
//functionality removed.

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;

public class Main
{
    private static Example example;
    
    public static void main(String[] args)
    {
        try
        {
            Game.setDebug(true);
            
            //OpenGL context should be forward-compatible, i.e. one where all
            //functionality deprecated in the requested version of OpenGL is
            //removed. In the core profile, immediate mode OpenGL is deprecated.
            Game.hint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            Game.hint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            
            //OpenGL 3.2 gives good access to most modern features, and is
            //supported by most hardware.
            Game.hint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            Game.hint(GLFW_CONTEXT_VERSION_MINOR, 2);
            Game.hint(GLFW_SAMPLES, 4);
            
            Game.setFullscreen(false);
            Game.setVsync(true);
            Game.setTitle("DevilUtil - Modern OpenGL example");

            Game.onInit(() ->
            {
                Game.getMouse().setGrabbed(true);
                
                glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                glClearDepth(1.0);
                
                DGL.init();
                
                example = new Example();
                
                Game.onResize(example::resize);
                Game.onMouseMoved(example::mouseMoved);
                Game.onKey(example::key);
                Game.onStep(example::step);
                Game.onRender(example::render);
                
                glfwMaximizeWindow(Game.getWindow());
            });

            Game.onDestroy(crashed ->
            {
                example.destroy();
                example = null;
                
                //Most resources will leak in many crash situations. We should
                //see the source of the crash, not be spammed by leak messages.
                if (crashed) DGL.setDebugLeakTracking(false);

                DGL.destroy();
            });

            Game.run();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            APIUtil.DEBUG_STREAM.close(); //Prevent LWJGL leak message spam.
            System.exit(-1);
        }
    }
}
