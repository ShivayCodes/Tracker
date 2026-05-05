#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in float aAO;

out vec2 TexCoord;
out vec3 Normal;
out float AO;
out float vDistance;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    vec4 worldPos = model * vec4(aPos, 1.0);
    vec4 viewPos = view * worldPos;
    gl_Position = projection * viewPos;
    
    TexCoord = aTexCoord;
    Normal = normalize(mat3(model) * aNormal);
    AO = aAO;
    vDistance = length(viewPos.xyz);
}
