package com.example.hy.audiovideotest.media;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.hardware.Camera;
import com.example.hy.audiovideotest.R;

import java.io.IOException;

/**
 * Android视音频开发之视频采集（Camera），解编码（MediaCodec），音视频合成mp4（MediaMuxer ）
 * 一、使用Camera进行视频采集
 * 步骤：
 * 1、打开摄像头，预览（使用SurfaceView）Camera数据
 * 2、配置数据回调的格式
 * 3、设置预览，SurfaceView或TextureView
 * 4、取到 NV21 的数据回调，通过setPreviewCallback方法监听预览的回调
 * 5、停止预览并释放资源
 * 二、使用MediaCodecAPI解编码（完成音频 AAC 硬编、硬解，完成视频 H.264 的硬编、硬解）
 *
 */
public class MediaActivity extends AppCompatActivity{

    Camera camera;//视频采集
    SurfaceView surfaceView;//视频预览
    SurfaceHolder surfaceHolder;//SurfaceView回调
    H264Encoder encoder;//编解码器
    Button btnMuxer;
    int width = 1280;
    int height = 720;
    int framerate = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        surfaceView =  findViewById(R.id.sv_1);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.w("AudioActivity", "enter surfaceCreated method");
                // 目前设定的是，当surface创建后，就打开摄像头开始预览
                /* 1、Camera，打开摄像头 */
                camera = Camera.open();
                camera.setDisplayOrientation(90);//摄像头旋转90度
                /* 2、Camera，配置数据回调的格式：
                 * Google支持的 Camera Preview Callback的YUV常用格式有两种：一个是NV21，一个是YV12， Android一般默认使用NV21 */
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(1280, 720);
                camera.setParameters(parameters);//配置参数
                /* 3、设置预览 */
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /* 4、Camera，取到NV21的数据回调 */
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    /* 里面的byte[]数据就是NV21格式的数据 */
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.w("AudioActivity", "enter surfaceDestroyed method");
                        if (encoder != null) {
                            encoder.putData(data);
                        }
                    }
                });

                encoder = new H264Encoder(width, height, framerate);
                encoder.startEncoder();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.w("AudioActivity", "enter surfaceChanged method");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.w("AudioActivity", "enter surfaceDestroyed method");
                /* 5、Camera，停止预览并释放资源 */
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera = null;
                }
                if (encoder != null) {
                    encoder.stopEncoder();
                }
            }
        });

        btnMuxer = findViewById(R.id.btn_muxer);
        btnMuxer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaActivity.this, MediaMuxerActivity.class);
                startActivity(intent);
                finish();
            }
        });
        if (supportH264Codec()) {
            Log.e("AudioActivity", "support H264 hard codec");
        } else {
            Log.e("AudioActivity", "not support H264 hard codec");
        }
    }


    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
