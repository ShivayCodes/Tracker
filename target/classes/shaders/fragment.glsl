#version 330 core

in vec2 TexCoord;
in vec3 Normal;
in float AO;
in float vDistance;

out vec4 FragColor;

uniform sampler2D texture_sampler;
uniform vec3 sunDirection;
uniform vec3 ambientLight;
uniform vec3 skyColor;

void main()
{
    vec4 texColor = texture(texture_sampler, TexCoord);
    if(texColor.a < 0.1)
        discard;

    // Diffuse lighting
    float diffuse = max(dot(Normal, normalize(sunDirection)), 0.0);
    vec3 lighting = (ambientLight + vec3(diffuse)) * AO;

    vec3 finalColor = texColor.rgb * lighting;
    
    // Fog calculation
    float fogDensity = 0.015;
    float fogFactor = exp(-pow(vDistance * fogDensity, 2.0));
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    
    finalColor = mix(skyColor, finalColor, fogFactor);

    FragColor = vec4(finalColor, texColor.a);
}
