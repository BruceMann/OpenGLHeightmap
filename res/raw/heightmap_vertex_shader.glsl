//res/raw/heightmap_vertex_shader.glsl
uniform mat4 u_Matrix;
attribute vec3 a_Position;
varying vec3 v_Color;

void main()
{
	//��2����ͬ��ɫ��ƽ����ֵ���߶ȴ���0��1֮�䣬�ӽ�0Ϊ��ɫ���ӽ�1Ϊ��ɫ
	v_Color = mix(vec3(0.180, 0.467, 0.153),//��ɫ
				  vec3(0.660, 0.670, 0.680),//��ɫ
				  a_Position.y);
				  
	gl_Position = u_Matrix * vec4(a_Position, 1.0);
}