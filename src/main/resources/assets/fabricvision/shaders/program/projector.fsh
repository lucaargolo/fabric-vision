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

vec2 calcViewportFromEye(vec4 eyePos, mat4 transformMatrix) {
    vec4 clipPos = transformMatrix * eyePos;
    vec3 ndcPos = clipPos.xyz * clipPos.w;

    vec2 viewportCoord = (ndcPos.xy + 1.0) * 0.5 * ViewPort.zw + ViewPort.xy;
    return viewportCoord;
}

void main() {

    vec4 mainTexture = texture(MainSampler, texCoord0);

    float mainDepth = texture(MainDepthSampler, texCoord0).r;

    vec4 mainEyePos = calcEyeFromWindow(mainDepth, MainInverseTransformMatrix);
    mainEyePos.xyz += CameraPosition;
    vec3 mainPixelPosition = mainEyePos.xyz;

    mainEyePos.xyz -= ProjectorPosition;

    vec2 projectorViewport = calcViewportFromEye(mainEyePos, ProjectorTransformMatrix);
    vec2 projectorTexCoords = projectorViewport.xy / ViewPort.zw;

    vec4 projectorTexture = texture(ProjectorSampler, projectorTexCoords);

    float projectorDepth = texture(ProjectorDepthSampler, projectorTexCoords).r;

    //Imagino q a solução aqui seja usar o calcEyeFromWindow com os parametros do projetor para pegar o projectorPixelPosition e fazer as comparações de distancia contra ProjectorPosition...
    //Entretanto se eu fizer isso o projectorPixelPosition vai ficar em um espaço diferente do mainPixelPosition... (Um ta no espaço da Camera e o outro do Projetor)
    //Tenho que fazer alguma alteração de matriz

    if(projectorTexCoords.x >= 0.0 && projectorTexCoords.x <= 1.0 && projectorTexCoords.y >= 0.0 && projectorTexCoords.y <= 1.0 && projectorTexture.a > 0.0) {
        fragColor = mix(projectorTexture, mainTexture, 0.5);
    }else{
        fragColor = mainTexture;
    }

}




