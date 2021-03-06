precision mediump float;
varying vec3 v_Color;
varying float v_ElapsedTime;

uniform sampler2D u_TextureUnit;

void main()
{
	//使用gl_PointCoord绘制点的外观
	float xDistance = 0.5 - gl_PointCoord.x;
	float yDistance = 0.5 - gl_PointCoord.y;
	float distanceFromCenter = sqrt(xDistance*xDistance+yDistance*yDistance);
	
	if(distanceFromCenter > 0.5){
		//丢掉这片段
		discard;
	} else {
		//gl_FragColor = vec4(v_Color/v_ElapsedTime, 1.0);
		gl_FragColor = vec4(v_Color/v_ElapsedTime, 1.0) * texture2D(u_TextureUnit, gl_PointCoord);
	}	
}