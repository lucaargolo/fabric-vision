#version 150

in vec2 texCoord0;
in vec4 vertexPos;

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform sampler2D ProjectorSampler;
uniform sampler2D ProjectorDepthSampler;

uniform mat4 MainTransformMatrix;
uniform mat4 MainInverseTransformMatrix;
uniform mat4 ProjectorTransformMatrix;
uniform mat4 ProjectorInverseTransformMatrix;

uniform mat4 InvProjMat;

uniform vec3 CameraPosition;
uniform vec3 ProjectorPosition;

uniform float NearPlane;
uniform float FarPlane;

uniform ivec4 ViewPort;

out vec4 fragColor;

vec4 calcEyeFromWindow(in float depth, mat4 inverseTransformMatrix) {
    vec3 ndcPos;
    ndcPos.xy = ((2.0 * gl_FragCoord.xy) - (2.0 * ViewPort.xy)) / (ViewPort.zw) - 1;
    ndcPos.z = (2.0 * depth - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
    vec4 clipPos = vec4(ndcPos, 1.0);
    vec4 homogeneous = inverseTransformMatrix * clipPos;
    vec4 eyePos = vec4(homogeneous.xyz / homogeneous.w, homogeneous.w);
    return eyePos;
}

vec3 calcViewportFromEye(vec4 eyePos, mat4 transformMatrix) {
    vec4 clipPos = transformMatrix * eyePos;
    vec3 ndcPos = clipPos.xyz / clipPos.w;

    vec3 viewportCoord = vec3((ndcPos.xy + 1.0) * 0.5 * ViewPort.zw + ViewPort.xy, (ndcPos.z + 1.0) * 0.5);
    return viewportCoord;
}

void main() {

    vec4 mainTexture = texture(MainSampler, texCoord0);

    float mainDepth = texture(MainDepthSampler, texCoord0).r;

    vec4 mainEyePos = calcEyeFromWindow(mainDepth, MainInverseTransformMatrix);
    vec3 mainPixelPosition = mainEyePos.xyz + CameraPosition - ProjectorPosition;


    vec3 projectorViewport = calcViewportFromEye(vec4(mainPixelPosition, mainEyePos.w), ProjectorTransformMatrix);
    vec2 projectorTexCoords = projectorViewport.xy / ViewPort.zw;

    projectorTexCoords.y = 1 - projectorTexCoords.y;
    vec4 projectorTexture = texture(ProjectorSampler, projectorTexCoords);

    float projectorTextureDepth = texture(ProjectorDepthSampler, projectorTexCoords).r;

    if(projectorTexCoords.x >= 0.0 && projectorTexCoords.x <= 1.0 && projectorTexCoords.y >= 0.0 && projectorTexCoords.y <= 1.0 && projectorTexture.a > 0.0) {
        if(projectorViewport.z <= projectorTextureDepth) {
            fragColor = mix(projectorTexture, mainTexture, 0.5);
        }else{
            fragColor = mainTexture;
        }

    }else{
        fragColor = mainTexture;
    }

}




