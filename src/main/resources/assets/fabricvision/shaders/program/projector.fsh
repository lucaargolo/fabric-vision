#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform sampler2D ProjectorSampler;
uniform sampler2D ProjectorDepthSampler;

uniform mat4 MainInverseTransformMatrix;
uniform mat4 ProjectorTransformMatrix;

uniform vec3 CameraPosition;
uniform vec3 ProjectorPosition;

uniform float ProjectionFallout;

uniform ivec4 ViewPort;

out vec4 fragColor;

void main() {
	vec4 mainTexture = texelFetch(MainSampler, ivec2(gl_FragCoord.xy), 0);
	float mainDepth = texelFetch(MainDepthSampler, ivec2(gl_FragCoord.xy), 0).r;

	vec3 windowPos = vec3(gl_FragCoord.xy, mainDepth);
	vec3 ndc = vec3(windowPos.xy / ViewPort.zw * 2.0 - 1.0, windowPos.z * 2.0 - 1.0);
	vec4 world0 = MainInverseTransformMatrix * vec4(ndc, 1.0);
	vec3 world = world0.xyz / world0.w;

	vec3 worldProjector = world + CameraPosition - ProjectorPosition;
	vec4 ndcProjector0 = ProjectorTransformMatrix * vec4(worldProjector, 1.0);
	vec3 ndcProjector = ndcProjector0.xyz / ndcProjector0.w;

	vec2 projectorTexCoords = ndcProjector.xy * 0.5 + 0.5;

	vec4 projectorTexture = texture(ProjectorSampler, vec2(projectorTexCoords.x, 1.0 - projectorTexCoords.y));
	float projectorTextureDepth = texture(ProjectorDepthSampler, projectorTexCoords).r;

	float projectorLight = (projectorTexture.r + projectorTexture.g + projectorTexture.b)/3.0;
	float mainLight = (mainTexture.r + mainTexture.g + mainTexture.b)/3.0;

	if(projectorLight > mainLight && all(lessThanEqual(abs(ndcProjector), vec3(1.0))) && projectorTexture.a > 0.0 && ndcProjector.z * 0.5 + 0.5 - 0.00001 <= projectorTextureDepth) {
		float fallout = 1.0 - (min(ProjectionFallout, abs(distance(ndcProjector, worldProjector)))/ProjectionFallout);
		fragColor = mix(mainTexture, projectorTexture, min(0.5, fallout));
	} else {
		fragColor = mainTexture;
	}

}