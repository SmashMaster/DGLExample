#version 140

uniform mat4 u_projection_matrix; //Supplied once per frame.
uniform mat4 u_view_matrix;

in vec3 in_pos; //Supplied by the vertex buffer.
in vec3 in_color;
in vec2 in_tex_coord;

out vec3 v_color; //Passed to fragment shader.
out vec2 v_tex_coord;

void main()
{
    v_color = in_color;
    v_tex_coord = in_tex_coord;
    
    //Position should be a vec4 because matrix transformations require homogeneous coordinates.
    //So this ends up being vec4(x, y, z, 1.0)
    vec4 pos = vec4(in_pos, 1.0);
    
    //gl_Position is one of the few built-in variables in GLSL.
    gl_Position = u_projection_matrix*u_view_matrix*pos;
}
