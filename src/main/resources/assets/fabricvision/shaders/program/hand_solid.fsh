#version 150

uniform sampler2D MainSampler;
uniform sampler2D HandSampler;

out vec4 fragColor;

void main() {
	vec4 mainTexture = texelFetch(MainSampler, ivec2(gl_FragCoord.xy), 0);
	vec4 handTexture = texelFetch(HandSampler, ivec2(gl_FragCoord.xy), 0);

	if(handTexture.r > 0 || handTexture.g > 0 || handTexture.b > 0 || handTexture.a > 0) {
		fragColor = handTexture;
	}else{
		fragColor = mainTexture;
	}

}