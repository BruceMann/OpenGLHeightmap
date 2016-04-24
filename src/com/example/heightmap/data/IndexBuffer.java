package com.example.heightmap.data;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class IndexBuffer {
	private final int bufferId;
	
	public IndexBuffer(short[] indexData) {
		// TODO Auto-generated constructor stub
		final int buffers[] = new int[1];
		glGenBuffers(buffers.length, buffers, 0);
		if(buffers[0] == 0){
			throw new RuntimeException("���۴���VertexBuffer����");
		}
		bufferId = buffers[0];
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
		
		ShortBuffer indexArray = ByteBuffer
		            .allocateDirect(indexData.length * Constants.BYTES_PER_SHORT)
		            .order(ByteOrder.nativeOrder())
		            .asShortBuffer()
		            .put(indexData);
		indexArray.position(0);
		
		//���ݴ��뻺����
		//GL_ARRAY_BUFFER�����㻺����
		//GL_STATIC_DRAW��������󽫱��޸�һ�Σ����ǻᾭ��ʹ�ã�OpenGL���Ը�����Ҫ���Ż���
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray.capacity()*Constants.BYTES_PER_SHORT,
				indexArray, GL_STATIC_DRAW);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public int getBufferId(){
		return bufferId;
	}
}
