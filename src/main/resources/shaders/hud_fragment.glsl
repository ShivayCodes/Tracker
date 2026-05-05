#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D texture_sampler;

void main()
{
    vec4 texColor = texture(texture_sampler, TexCoord);
    if(texColor.a < 0.1)
        discard;
    FragColor = texColor;
}
