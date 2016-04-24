package com.example.heightmap;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.heightmap.objs.Heightmap;
import com.example.heightmap.objs.ParticleShooter;
import com.example.heightmap.objs.ParticleSystem;
import com.example.heightmap.objs.Skybox;
import com.example.heightmap.programs.HeightmapShaderProgram;
import com.example.heightmap.programs.ParticleShaderProgram;
import com.example.heightmap.programs.SkyboxShaderProgram;
import com.example.heightmap.util.Geometry.Point;
import com.example.heightmap.util.Geometry.Vector;
import com.example.heightmap.util.TextureHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView.Renderer;

public class SkyboxRenderer implements Renderer{
	private Context context;
	
	//��պ�
	private SkyboxShaderProgram skyboxProgram;
	private Skybox skybox;
	private int skyboxTexture;
	
	//ͶӰ����
	private final float[] projectionMatrix = new float[16];
	//��ͼ����
	private final float[] viewMatrixForSkybox = new float[16];
	private final float[] viewMatrix = new float[16];
	//ģ�;���
	private final float[] modelMatrix = new float[16];
	//ģ����ͼͶӰ����
	private final float[] modelViewProjectionMatrix = new float[16];
	//���ɾ���
	private final float[] tempMatrix = new float[16];
	
	//����ϵͳ
	private ParticleShaderProgram particleProgram;
	private ParticleSystem particleSystem;
	private ParticleShooter redParticleShooter;
	private ParticleShooter greenParticleShooter;
	private ParticleShooter blueParticleShooter;
	private long globalStartTime;
	private int texture;
	
	//����
	private HeightmapShaderProgram heightmapProgram;
	private Heightmap heightmap;
	
	//������ת�Ƕ�
	private float xRotation, yRotation;
	
	public SkyboxRenderer(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		updateViewMatrices();
		
		drawSkybox();
		drawHeightmap();		
		drawParticles();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		glViewport(0, 0, width, height);
		
		perspectiveM(projectionMatrix, 0, 45, (float)width/(float)height, 1f, 100f);
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//������Ȳ���
		glEnable(GL_DEPTH_TEST);
		//�޳������������棨������棩����ѭ�����εľ���˳��
		glEnable(GL_CULL_FACE);
		
		skyboxProgram = new SkyboxShaderProgram(context);
		skybox = new Skybox();
		skyboxTexture = TextureHelper.loadCubeMap(context, 
				new int[]{R.drawable.bg, R.drawable.bg1,//����
						R.drawable.bg, R.drawable.bg1,//����
						R.drawable.bg, R.drawable.bg1});//ǰ��	
		
		particleProgram = new ParticleShaderProgram(context);
		particleSystem = new ParticleSystem(10000);
		globalStartTime = System.nanoTime();
		
		final Vector particleDirection = new Vector(0.0f, 0.5f, 0.0f);
		final float angleVarianceInDegrees = 5f;
		final float speedVariance = 1f;
		
		redParticleShooter = new ParticleShooter(
				new Point(-1f, 0f, 0f),
				particleDirection, 
				Color.rgb(255, 50, 5),
				angleVarianceInDegrees,
				speedVariance);
		
		greenParticleShooter = new ParticleShooter(
				new Point(0f, 0f, 0f),
				particleDirection, 
				Color.rgb(25, 255, 25),
				angleVarianceInDegrees,
				speedVariance);
		
		blueParticleShooter = new ParticleShooter(
				new Point(1f, 0f, 0f),
				particleDirection, 
				Color.rgb(5, 50, 255),
				angleVarianceInDegrees,
				speedVariance);
		
		texture = TextureHelper.loadTexture(context, R.drawable.ic_launcher);
		
		heightmapProgram = new HeightmapShaderProgram(context);
		heightmap = new Heightmap(
				((BitmapDrawable)context.getResources().getDrawable(R.drawable.heightmap))
				.getBitmap()
				);
	}

	public void handleTouchDrag(float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		xRotation += deltaX / 16f;
		yRotation += deltaY / 16f;
		
		if(yRotation < -90){
			yRotation = -90;
		} else if(yRotation > 90){
			yRotation = 90;
		}
	}
	
	private void drawHeightmap() {
		// TODO Auto-generated method stub
		setIdentityM(modelMatrix, 0);
		scaleM(modelMatrix, 0, 100f, 10f, 100f);
		updateMvpMatrix();
		heightmapProgram.useProgram();
		heightmapProgram.setUniforms(modelViewProjectionMatrix);
		heightmap.bindData(heightmapProgram);
		heightmap.draw();
	}

	private void drawParticles() {
		//��ֹ����д����Ȼ�����,�����ӽ���Ե��������Ȳ���
		glDepthMask(false);
		
		float currentTime = (System.nanoTime() - globalStartTime)/1000000000f;//1,000,000,000f
		
		redParticleShooter.addParticles(particleSystem, currentTime, 1);
		greenParticleShooter.addParticles(particleSystem, currentTime, 1);
		blueParticleShooter.addParticles(particleSystem, currentTime, 1);
		
		//ģ�;������ã�ֻ����ͼ-ͶӰ���������
		setIdentityM(modelMatrix, 0);
		updateMvpMatrix();
		
		//���ۼӻ�ϼ���������ӣ�����Խ��Խ��
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);
				
		particleProgram.useProgram();
		particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, texture);
		particleSystem.bindData(particleProgram);
		particleSystem.draw();
		
		glDisable(GL_BLEND);
		//����д����Ȼ�����
		glDepthMask(true);
	}

	private void drawSkybox() {
		//��Ƭ�α��κ��Ѿ����������Ƭ����ȱȽϽ����߶�����ͬ�Ⱦ��봦
		glDepthFunc(GL_LEQUAL);
				
		//ģ�;������ã�ֻ����ͼ-ͶӰ���������
		setIdentityM(modelMatrix, 0);
		updateMvpMatrixForSkybox();
		skyboxProgram.useProgram();
		skyboxProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture);
		skybox.bindData(skyboxProgram);
		skybox.draw();
		
		//��Ƭ�α��κ��Ѿ����������Ƭ�ν����߱�Զƽ������������Ƭ��
		glDepthFunc(GL_LESS);
	}
	
	/**
	 * ��������ͷ����ͼ����
	 */
	private void updateViewMatrices() {
		// TODO Auto-generated method stub
		setIdentityM(viewMatrix, 0);
		rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
		rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
		System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);
		
		//�൱������ͷ�ƶ�����Ϊ��0f,1.5f.5f��,����ͼ����ƽ�Ʒ����෴
		translateM(viewMatrix, 0, 0f, -1.5f, -5f);
	}

	/**
	 * �ϲ�ģ��-��ͼ�����壩-ͶӰ����
	 */
	private void updateMvpMatrix() {
		// TODO Auto-generated method stub
		multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
	}
	
	/**
	 * �ϲ�ģ��-��ͼ����պУ�-ͶӰ����
	 */
	private void updateMvpMatrixForSkybox(){
		multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
	}
}
