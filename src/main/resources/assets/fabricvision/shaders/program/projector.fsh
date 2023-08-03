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

void main() {
	vec4 mainTexture = texelFetch(MainSampler, ivec2(gl_FragCoord.xy), 0);
	float mainDepth = texelFetch(MainDepthSampler, ivec2(gl_FragCoord.xy), 0).r;

	vec3 windowPos = vec3(gl_FragCoord.xy, mainDepth);
	vec3 ndc = vec3(
		windowPos.xy / ViewPort.zw * 2.0 - 1.0,
		windowPos.z * 2.0 - 1.0
	);
	vec4 world0 = MainInverseTransformMatrix * vec4(ndc, 1.0);
	vec3 world = world0.xyz / world0.w;

	vec3 worldProjector = world + CameraPosition - ProjectorPosition;
	vec4 ndcProjector0 = ProjectorTransformMatrix * vec4(worldProjector, 1.0);
	vec3 ndcProjector = ndcProjector0.xyz / ndcProjector0.w;

	vec2 projectorTexCoords = ndcProjector.xy * 0.5 + 0.5;

	vec4 projectorTexture = texture(ProjectorSampler, vec2(projectorTexCoords.x, 1.0 - projectorTexCoords.y));
	float projectorTextureDepth = texture(ProjectorDepthSampler, projectorTexCoords).r;

	if(
		all(lessThanEqual(abs(ndcProjector), vec3(1.0))) &&
		projectorTexture.a > 0.0 &&
		ndcProjector.z * 0.5 + 0.5 - 0.000001 /* to avoid z-fighting */ <= projectorTextureDepth
	) {
		fragColor = mix(projectorTexture, mainTexture, 0.5);
	} else {
		fragColor = mainTexture;
	}

}