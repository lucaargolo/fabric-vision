#version 150

in vec2 texCoord0;
in vec4 vertexPos;

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform sampler2D ProjectorSampler;
uniform sampler2D ProjectorDepthSampler;

uniform mat4 MainInverseTransformMatrix;
uniform mat4 ProjectorTransformMatrix;

uniform ivec4 ViewPort;

out vec4 fragColor;

vec4 calcEyeFromWindow(in float depth, vec2 viewPortCoord, mat4 inverseTransformMatrix) {
    vec3 ndcPos;
    ndcPos.xy = ((2.0 * gl_FragCoord.xy) - (2.0 * ViewPort.xy)) / (ViewPort.zw) - 1;
    ndcPos.z = (2.0 * depth - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
    vec4 clipPos = vec4(ndcPos, 1.);
    vec4 homogeneous = inverseTransformMatrix * clipPos;
    vec4 eyePos = vec4(homogeneous.xyz / homogeneous.w, homogeneous.w);
    return eyePos;
}

vec3 calcWindowFromEye(vec4 eyePos, mat4 transformMatrix) {
    vec4 clipPos = transformMatrix * eyePos;
    vec3 ndcPos = clipPos.xyz / clipPos.w;

    float depth = ((ndcPos.z * 0.5) + 0.5) * (gl_DepthRange.far - gl_DepthRange.near) + gl_DepthRange.near;
    vec2 viewportCoord = (ndcPos.xy + 1.0) * 0.5 * ViewPort.zw + ViewPort.xy;

    return vec3(viewportCoord, depth);
}


void main() {

    vec4 mainTexture = texture(MainSampler, texCoord0);

    float mainDepth = texture(MainDepthSampler, texCoord0).x;

    vec4 eyePos = calcEyeFromWindow(mainDepth, texCoord0, MainInverseTransformMatrix);

    vec3 projectorWindow = calcWindowFromEye(eyePos, ProjectorTransformMatrix);
    vec2 projectorTexCoords = clamp(projectorWindow.xy / ViewPort.zw, 0.0, 1.0);
    vec4 projectorTexture = texture(ProjectorSampler, projectorTexCoords);

    if(projectorTexture.a > 0.0) {
        fragColor = projectorTexture;
    }else{
        fragColor = mainTexture;
    }

}
