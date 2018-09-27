package com.example.hy.audiovideotest.openGL;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLSurfaceView.Renderer实现类，渲染器
 * @ onSurfaceCreated()  在View的OpenGL环境被创建的时候调用。
 * @ onDrawFrame()       每一次View的重绘都会调用
 * @ onSurfaceChanged()  如果视图的几何形状发生变化（例如，当设备的屏幕方向改变时），则调用此方法。
 * Created by 陈健宇 at 2018/9/27
 */
public class MyRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Square mSquare;

    private volatile float mAngle;//暴露旋转角度

    private final float[] mMVPMatrix = new float[16];// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mProjectionMatrix = new float[16];//Projection Matrix
    private final float[] mViewMatrix = new float[16];//View Matrix
    private final float[] mRotationMatrix = new float[16];//Rotation Matrix
    private float[] mCombineMatrix = new float[16];


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize a triangle
        mTriangle = new Triangle();
        // initialize a square
        mSquare = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix(Projection Matrix)is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection(Projection Matrix) and view transformation(View matrix)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation transformation for the triangle(Rotation Matrix), from caculation
        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
       // Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        //Create a rotation transformation for the triangle(Rotation Matrix), from touch event
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mCombineMatrix, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw mTriangle
        mTriangle.draw(mCombineMatrix);
    }

    /**
     * 一个工具类，编译OpenGLShading Language (GLSL)代码
     * @param type fragmentshader类型或vertexshader类型
     * @param shaderCode 要编译的shader代码
     * @return 编译后的shader
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public float getmAngle() {
        return mAngle;
    }

    public void setmAngle(float mAngle) {
        this.mAngle = mAngle;
    }
}
