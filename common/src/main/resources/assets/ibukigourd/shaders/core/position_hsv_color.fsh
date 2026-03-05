#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;

out vec4 fragColor;

vec3 hsv_to_rgb(vec3 hsv){
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(hsv.xxx + K.xyz) * 6.0 - K.www);
    return hsv.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), hsv.y);
}

void main() {
    if (vertexColor.a == 0.0) {
        discard;
    }
    fragColor =  vec4(hsv_to_rgb(vertexColor.xyz), vertexColor.w) * ColorModulator;
}
