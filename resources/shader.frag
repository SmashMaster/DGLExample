#version 140

uniform sampler2D u_texture; //Only has red channel.

in vec3 v_color; //Supplied by vertex shader.
in vec2 v_tex_coord;

out vec3 out_color; //The color of a pixel/fragment.

void main()
{
    out_color = v_color*texture(u_texture, v_tex_coord).r;
}
