#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

vec3 hsv_to_rgb(vec3 hsv) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(hsv.xxx + K.xyz) * 6.0 - K.www);
    return hsv.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), hsv.y);
}

vec3 rgb_to_hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 rgb_to_hsl(vec3 c) {
    float mx = max(max(c.r, c.g), c.b);
    float mn = min(min(c.r, c.g), c.b);
    float l = (mx + mn) * 0.5;
    float s;
    float h;
    if (mx == mn) {
        s = 0.0;
        h = 0.0;
    } else {
        s = l < 0.5 ? (mx - mn) / (mx + mn) : (mx - mn) / (2.0 - mx - mn);
        if (mx == c.r) {
            h = (c.g - c.b) / (mx - mn);
            if (c.g < c.b) h += 6.0;
        } else if (mx == c.g) {
            h = (c.b - c.r) / (mx - mn) + 2.0;
        } else {
            h = (c.r - c.g) / (mx - mn) + 4.0;
        }
        h /= 6.0;
    }
    return vec3(h, s, l);
}

float hue_to_rgb(float p, float q, float t) {
    if (t < 0.0) t += 1.0;
    if (t > 1.0) t -= 1.0;
    if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
    if (t < 1.0 / 2.0) return q;
    if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
    return p;
}

vec3 hsl_to_rgb(vec3 hsl) {
    if (hsl.y == 0.0) {
        return vec3(hsl.z);
    }
    float q = hsl.z < 0.5 ? hsl.z * (1.0 + hsl.y) : hsl.z + hsl.y - hsl.z * hsl.y;
    float p = 2.0 * hsl.z - q;
    return vec3(
            hue_to_rgb(p, q, hsl.x + 1.0 / 3.0),
            hue_to_rgb(p, q, hsl.x),
            hue_to_rgb(p, q, hsl.x - 1.0 / 3.0)
    );
}

// C = 纹理像素色(texture)，T = 顶点色(theme tint)
// alpha = C.a × T.a（T.a 由 CPU 在 tintAlpha=false 时强制为 1，纹理 alpha 直通）
void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    float alpha = tex.a * vertexColor.a;
    if (alpha == 0.0) {
        discard;
    }

    vec3 C = tex.rgb;
    vec3 T = vertexColor.rgb;

    vec3 rgb;
    #ifdef SOKITSU_MASK
    // Mask：完全替换为 T，C 只当形状/遮罩
    rgb = T;
    #else
    #ifdef SOKITSU_TINT
    // Tint：HSV(T.H, T.S, C.V) —— 主题只改色相/饱和，明度完全由纹理灰阶决定（美术所见即所得）
    vec3 chsv = rgb_to_hsv(C);
    vec3 thsv = rgb_to_hsv(T);
    rgb = hsv_to_rgb(vec3(thsv.x, thsv.y, chsv.z));
    #else
    #ifdef SOKITSU_HUESHIFT
    // HueShift：HSL(T.H, C.S, C.L) —— 只转色相，纹理自身饱和与亮度保留
    vec3 chsl = rgb_to_hsl(C);
    vec3 thsl = rgb_to_hsl(T);
    rgb = hsl_to_rgb(vec3(thsl.x, chsl.y, chsl.z));
    #else
    // 兜底：与 Mask 一致（正常都会由 withShaderDefine 注入其一）
    rgb = T;
    #endif
    #endif
    #endif

    fragColor = vec4(rgb, alpha) * ColorModulator;
}
