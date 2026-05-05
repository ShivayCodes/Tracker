#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

out vec2 TexCoord;
out float Brightness;

uniform mat4 model;
uniform mat4 projection;

void main()
{
    gl_Position = projection * model * vec4(aPos, 1.0);
    TexCoord = aTexCoord;
    
    // Simple lighting for the hand
    Brightness = max(dot(aNormal, normalize(vec3(0.5, 1.0, 0.5))), 0.4);
}
