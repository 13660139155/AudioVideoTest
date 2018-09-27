package com.example.hy.audiovideotest.openGL;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hy.audiovideotest.R;

/**
 * Android OpenGL ES 开发
 * 一、OpenGL ES环境搭建：
 * 步骤：
 * 1、manifest中添加声明：<uses-feature android:glEsVersion="0x00020000" android:required="true" />
 *                        <supports-gl-texture android:name="GL_OES_compressed_ETC1_RGB8_texture" />
 *                         <supports-gl-texture android:name="GL_OES_compressed_paletted_texture" />
 * 2、创建一个Activity 用于展示OpenGL ES 图形
 * 3、创建GLSurfaceView对象（可以绘制OpenGL图像的SurfaceView）
 * 4、创建GLSurfaceView.Renderer（控制着与它关联的GLSurfaceView 绘制的内容）
 * 二、OpenGL Es定义形状：
 * OpenGL ES允许你使用三维空间坐标系定义绘制的图像，GLSurfaceView框架的中心为坐标原点，
 * 在绘制一个图形之前必须要先定义它的坐标，在OpenGL中，这样做的典型方法是为坐标定义浮点数的顶点数组，
 * 为了获得最大的效率，可以将这些坐标写入ByteBuffer，并传递到OpenGL ES图形管道进行处理
 * 三、OpenGL Es绘制形状：
 * 步骤：
 * 1、定义形状类，并在Renderer的方法onSurfaceCreated()中初始化形状
 * 2、绘制形状，需要一个vertexshader来绘制一个形状和一个fragmentshader来为形状上色，这些形状必须被编译然后被添加到一个OpenGLES program中，program之后被用来绘制形状
 *              Vertex Shader - 用于渲染形状的顶点的OpenGLES 图形代码。
 *              Fragment Shader - 用于渲染形状的外观（颜色或纹理）的OpenGLES 代码。
 *              Program - 一个OpenGLES对象，包含了你想要用来绘制一个或多个形状的shader。
 * 四、OpenGL Es使用投影和相机视图：
 * OpenGL ES环境允许你以更接近于你眼睛看到的物理对象的方式来显示你绘制的对象，物理查看的模拟是通过对你所绘制的对象的坐标进行数学变换完成的
 *              Projection — 这个变换是基于他们所显示的GLSurfaceView的宽和高来调整绘制对象的坐标的。没有这个计算变换，通过OpenGL绘制的形状会在不同显示窗口变形。这个投影变化通常只会在OpenGL view的比例被确定或者在你渲染器的onSurfaceChanged()方法中被计算
 *              Camera View — 这个换是基于虚拟的相机的位置来调整绘制对象坐标的。需要着重注意的是，OpenGL ES并没有定义一个真实的相机对象，而是提供一个实用方法，通过变换绘制对象的显示来模拟一个相机。相机视图变换可能只会在你的GLSurfaceView被确定时被计算，或者基于用户操作或你应用程序的功能来动态改变。
 * 下面创建投影和相机视图并将其应用的到你的GLSurfaceView的绘制对象上
 * 步骤：
 * 1、定义投影，投影变化的数据是在你GLSurfaceView.Renderer类的onSurfaceChanged()方法中被计算的
 * 2，定义相机视图，通过Matrix.setLookAtM()方法计算相机视图变换，然后将其通过 Matrix.multiplyMM（）与之前计算出的投影矩阵结合到一起
 * 3、应用投影和相机变换,通过GLES20.glUniformMatrix4fv()应用到图形中
 * 五、OpenGL ES添加运动效果：
 * 例如旋转，步骤：
 * 1、在渲染器中，添加一个新的变换矩阵（旋转矩阵）
 * 2、在onDrawFrame()中把它与投影和相机视图变换矩阵合并到一起
 * 3、在GLSurfaceView中注释掉setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)，允许连续渲染
 * 六、OpenGL ES响应触摸事件：
 * 基于旋转，步骤：
 * 1、在GLSurfaceView中实现onTouchEvent()方法
 * 2、暴露旋转角度，当渲染代码是在独立于你应用程序的主用户界面线程的单独线程执行的时候，你必须声明这个共有变量是volatile类型的
 * 3、应用旋转，先注释掉上面产生角度的代码，然后在onDrawFrame()中Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
 * 4、在GLSurfaceView中注释掉setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)，允许连续渲染
 */
public class OpenGLES20Activity extends AppCompatActivity {

    private MyGLSurfaceView myGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Create a GLSurfaceView
        myGlSurfaceView = new MyGLSurfaceView(this);
        //set the GLSurfaceView as the ContentView for this Activity.
        setContentView(myGlSurfaceView);
    }
}
