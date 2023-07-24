#version 150

in vec2 texCoord0;

uniform sampler2D MainSampler;
uniform sampler2D ProjectorSampler;

uniform mat4 ProjectorProjMat;
uniform mat4 ProjectorViewMat;

uniform mat4 InverseViewMat;

out vec4 fragColor;

void main() {
    // Sample the main texture (your scene's rendered image)
    vec4 mainColor = texture(MainSampler, texCoord0);

    // Convert the fragment's screen-space coordinates to projector clip-space coordinates
    vec4 clipSpacePos = InverseViewMat * vec4(2.0 * texCoord0 - 1.0, 0.0, 1.0);
    vec4 projClipSpacePos = ProjectorProjMat * ProjectorViewMat * clipSpacePos;

    // Normalize the projector clip-space coordinates to get projector texture coordinates
    vec2 projTexCoord = projClipSpacePos.xy / projClipSpacePos.w * 0.5 + 0.5;

    // Sample the projected image
    vec4 projectorColor = texture(ProjectorSampler, projTexCoord);

    // Blend the projected color with the main color based on the projector opacity
    vec4 blendedColor = mix(mainColor, projectorColor, 0.5);

    fragColor = blendedColor;
}
