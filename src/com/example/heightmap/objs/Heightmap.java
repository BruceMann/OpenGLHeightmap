package com.example.heightmap.objs;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;

import com.example.heightmap.data.IndexBuffer;
import com.example.heightmap.data.VertexBuffer;
import com.example.heightmap.programs.HeightmapShaderProgram;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Heightmap {
	private static final int POSITION_COMPONENT_COUNT = 3;
	
	private final int width;
	private final int height;
	private final int numElements;
	private final VertexBuffer vertexBuffer;
	private final IndexBuffer indexBuffer;
	
	public Heightmap(Bitmap bitmap) {
		// TODO Auto-generated constructor stub
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		
		System.out.println( width*height );
		if(width*height>65536){
			throw new RuntimeException("Heightmap�߶ȳ�������������");
		}
		numElements = calculateNumElements();
		vertexBuffer = new VertexBuffer(loadBitmapData(bitmap));
		indexBuffer = new IndexBuffer(creatIndexData());
	}

	/**
	 * ������������
	 * @return
	 */
	private short[] creatIndexData() {
		// TODO Auto-generated method stub
		final short[] indexData = new short[numElements];
		int offset = 0;
		
		for(int row = 0; row < height - 1; row++){
			for(int col = 0; col < width - 1; col++){
				short topLeftIndexNum = (short)(row*width + col);
				short topRightIndexNum = (short)(row*width + col + 1);
				short bottomLeftIndexNum = (short)((row+1)*width + col);
				short bottomRightIndexNum = (short)((row+1)*width + col + 1);
				
				indexData[offset++] = topLeftIndexNum;
				indexData[offset++] = bottomLeftIndexNum;
				indexData[offset++] = topRightIndexNum;
				
				indexData[offset++] = topRightIndexNum;
				indexData[offset++] = bottomLeftIndexNum;
				indexData[offset++] = bottomRightIndexNum;
 			}
		}
		return indexData;
	}

	/**
	 * �����������������磬һ��3x3�ĸ߶�ͼ�У�3-1��x��3-1��=4���飬�Լ�ÿ������Ҫ2�������κ�ÿ����������Ҫ3��Ԫ�أ��ܹ��õ�24������
	 * @return
	 */
	private int calculateNumElements() {
		// TODO Auto-generated method stub
		return (width-1)*(height-1)*2*3;
	}

	private float[] loadBitmapData(Bitmap bitmap) {
		// TODO Auto-generated method stub
		final int[] pixels = new int[width*height];
		//��ȡͼƬ�����ص�
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		bitmap.recycle();
		
		final float[] heightmapVertices = new float[width*height*POSITION_COMPONENT_COUNT];
		int offset = 0;
		
		//��λͼ����ת��Ϊ�߶�ͼ����
		//�߶�ͼ��ÿ�������϶���1����λ��������x-zƽ���ϵ�λ�ã�0,0��Ϊ���ģ�λͼ�����Ͻǽ���ӳ�䵽��-0.5��-0.5�������½ǻᱻӳ�䵽��0.5,0.5��
		//����ƫ��ֵ = ��ǰ��*�߶� + ��ǰ��
		//һ��һ�еض�ȡλͼ��ԭ�����������ڴ��еĲ��ַ�ʽ���������ģ���CPU��˳�򻺴���ƶ�����ʱ�����Ǹ���Ч��
		for(int row = 0; row < height; row++){
			for(int col = 0; col < width; col++){
				final float xPosition = ((float)col/(float)(width-1)) - 0.5f;
				final float yPosition = (float)Color.red(pixels[(row*height)+col])/(float)255;
				final float zPosition = ((float)row/(float)(height-1))-0.5f;
				
				heightmapVertices[offset++] = xPosition;
				heightmapVertices[offset++] = yPosition;
				heightmapVertices[offset++] = zPosition;
 			}
		}
		return heightmapVertices;
	}
	
	public void bindData(HeightmapShaderProgram heightmapProgram){
		vertexBuffer.setVertexAttribPointer(0, 
				heightmapProgram.setPositionAttributeLocation(), 
				POSITION_COMPONENT_COUNT, 0);
	}
	
	public void draw(){
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
		glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
}
