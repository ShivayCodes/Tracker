#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 projectionModelView;

void main()
{
    gl_Position = projectionModelView * vec4(aPos, 1.0);
}
