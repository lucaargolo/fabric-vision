#version 150

uniform sampler2D MainSampler;
uniform sampler2D HandSampler;

out vec4 fragColor;

void main() {
	vec4 mainTexture = texelFetch(MainSampler, ivec2(gl_FragCoord.xy), 0);
	vec4 handTexture = texelFetch(HandSampler, ivec2(gl_FragCoord.xy), 0);

	fragColor = vec4(
		mix(mainTexture.r, handTexture.r, handTexture.a),
		mix(mainTexture.g, handTexture.g, handTexture.a),
		mix(mainTexture.b, handTexture.b, handTexture.a),
	1.0);
}