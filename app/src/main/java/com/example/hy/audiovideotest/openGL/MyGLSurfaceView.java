package com.example.hy.audiovideotest.openGL;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * GLSurfaceView 的实现类
 * Created by 陈健宇 at 2018/9/27
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private MyRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;


    public MyGLSurfaceView(Context context) {
        super(context);
        initCLSurface();
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCLSurface();
    }

    /**
     * GLSurfaceView初始化：
     * 设置OpenGL ES 的版本
     * 把渲染器设置给GLSurfaceView
     */
    private void initCLSurface() {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        //Create a GLSurfaceView.Renderer and set it
        mRenderer = new MyRenderer();
        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }
                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }
                mRenderer.setmAngle(mRenderer.getmAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
