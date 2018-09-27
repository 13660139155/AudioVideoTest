package com.example.hy.audiovideotest.audio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.hy.audiovideotest.R;
import com.example.hy.audiovideotest.media.MediaActivity;
import com.example.hy.audiovideotest.unit.MyUnit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.hy.audiovideotest.audio.GlobalConfig.AUDIO_FORMAT;
import static com.example.hy.audiovideotest.audio.GlobalConfig.CHANNEL_CONFIG_1;
import static com.example.hy.audiovideotest.audio.GlobalConfig.CHANNEL_CONFIG_2;
import static com.example.hy.audiovideotest.audio.GlobalConfig.SAMPLE_RATE_INHZ;

/**
 * Android视音频开发之音频采集（AudioRecord），音频播放（AudioTrack）
 * 一、使用AudioRecord采集音频PCM并保存到文件：
 * 步骤：
 * 1、创建一个AudioRecord对象
 * 2、初始化一个buffer
 * 3、开始录音
 * 4、创建一个数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中数据导入数据流。
 * 5、录音结束后，关闭数据流，释放资源，停止录音
 * 二、PCM文件转换为wav格式：
 * 步骤：
 * 在pcm文件的数据开头加入WAVE HEAD数据即可，也就是文件头，
 * 只有加上文件头部的数据，播放器才能正确的知道里面的内容到底是什么，进而能够正常的解析并播放里面的内容
 * 三、使用AudioTrack播放音频（播放pcm格式文件）：
 * 步骤：
 * 1、初始化一个buffer
 * 2、创建一个AudioTrack对象
 * 3、启动播放音频
 * 4、根据不同模式写入数据到AudioTrack中
 * 5、结束播放，释放资源
 */

public class AudioActivity extends AppCompatActivity {

    private static final String TAG = "rain";
    private Button btnControl, btnConvert, btnPlay;

    //需要申请的运行时权限
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private List<String> mPermissionList = new ArrayList<>(); //被用户拒绝的权限列表
    private final int MY_PERMISSIONS_REQUEST = 1024;

    private AudioRecord audioRecord = null;// 声明 AudioRecord 对象
    private int recordBufferSize = 0;// 声明recoordBufffer的大小字段
    private boolean isRecording;//是否正在录音

    private AudioTrack audioTrack;
    private byte[] audioData;
    private FileInputStream fileInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        btnControl = findViewById(R.id.btn_control);
        btnConvert = findViewById(R.id.btn_convert);
        btnPlay = findViewById(R.id.btn_play);

        //使用AudioRecord采集音频PCM并保存到文件
        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (button.getText().toString().equals(getString(R.string.start_record))) {
                    button.setText(getString(R.string.stop_record));
                    startRecord();
                } else {
                    button.setText(getString(R.string.start_record));
                    stopRecord();
                }
            }
        });
        //把pcm文件转换为可播放的wav格式
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PcmToWav pcmToWav = new PcmToWav(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_1, AUDIO_FORMAT);
                File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
                File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.wav");
                if (!wavFile.mkdirs()) {
                    Log.e(TAG, "wavFile Directory not created");
                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWav.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
            }
        });
        //使用AudioTack播放音频
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String string = btn.getText().toString();
                if (string.equals(getString(R.string.start_play))) {
                    btn.setText(getString(R.string.stop_play));
                    playInModeStream();
                    //playInModeStatic();
                } else {
                    btn.setText(getString(R.string.start_play));
                    stopPlay();
                }
            }
        });
    }

    /**
     * 使用AudioTrack.MODE_STATIC模式播放音频
     */
    @SuppressLint("StaticFieldLeak")
    private void playInModeStatic() {
        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区,后续就不必再传递数据了。
        // 这种模式适用于像铃声这种内存占用量较小，延时要求较高的文件。
        // 但它也有一个缺点，就是一次write的数据不能太多，否则系统无法分配足够的内存来存储全部数据
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = getResources().openRawResource(R.raw.ding);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for (int b; (b = in.read()) != -1; ) {
                            out.write(b);
                        }
                        Log.d(TAG, "Got the data");
                        audioData = out.toByteArray();
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.wtf(TAG, "Failed to read", e);
                }
                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void onPostExecute(Void v) {
                Log.i(TAG, "Creating track...audioData.length = " + audioData.length);
                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                audioTrack = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        new AudioFormat.Builder().setSampleRate(22050)
                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        audioData.length,
                        AudioTrack.MODE_STATIC,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                Log.d(TAG, "Writing audio data...");
                audioTrack.write(audioData, 0, audioData.length);
                Log.d(TAG, "Starting playback");
                audioTrack.play();
                Log.d(TAG, "Playing");
            }
        }.execute();
    }

    /**
     * 使用AudioTrack.MODE_STREAM模式播放音频
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playInModeStream() {
        //stream模式，通过write一次次把音频数据写到AudioTrack中。
        // 这和平时通过write系统调用往文件中写数据类似。
        // 但这种工作方式每次都需要把数据从用户提供的Buffer中拷贝到AudioTrack内部的Buffer中。
        // 这在一定程度上会使引入延时。
        /*
         * SAMPLE_RATE_INHZ 对应pcm音频的采样率
         * channelConfig 对应pcm音频的声道
         * AUDIO_FORMAT 对应pcm音频的格式
         */
        /* 1、初始化一个buffer */
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_2, AUDIO_FORMAT);
        /* 2、创建一个AudioRecord对象 */
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_CONFIG_2)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        /* 2、启动播放音频 */
        audioTrack.play();
        /* 4、MODE_STREAM模式，通过write一次次把音频数据写到AudioTrack中 */
        //存放pcm文件的地方
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileInputStream = new FileInputStream(file);
                    byte[] tempBuffer = new byte[minBufferSize];
                    while (fileInputStream.available() > 0) {
                        int readCount = fileInputStream.read(tempBuffer);
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue;
                        }
                        if (readCount != 0 && readCount != -1) {
                            audioTrack.write(tempBuffer, 0, readCount);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 停止播放音频
     */
    private void stopPlay(){
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST){
            if(grantResults.length > 0){
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, permissions[i] + " 权限被用户禁止！");
                        Toast.makeText(this, "You deny the permission!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                //do other
            }else {
                Toast.makeText(this, "something wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 开始录音
     */
    private void stopRecord() {
        /* 5、录音结束后，关闭数据流，释放资源，停止录音 */
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * 停止录音
     */
    private void startRecord() {
        /* 1、获取buffer的大小并创建AudioRecord */
        createAudioRecord();
        /* 2、 初始化一个buffer */
        final byte data[] = new byte[recordBufferSize];
        /* 3、开始录音 */
        audioRecord.startRecording();
        isRecording = true;
        /* 4、 创建一个数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中数据导入数据流。 */
        //保存pcm文件的地方
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }
        //一边读取一边导入，TODO: pcm数据无法直接播放，要保存为WAV格式
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                    if(os != null){
                        while (isRecording){
                            int read = audioRecord.read(data, 0, recordBufferSize);
                            // 如果读取音频数据没有出现错误，就将数据写入到文件
                            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                                os.write(data);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                } finally {
                    MyUnit.close(os);
                }
            }

        }).start();
    }

    /**
     * 获取buffer的大小并创建AudioRecord：
     */
    public void createAudioRecord(){
        //audioRecord能接受的最小的buffer大小
        recordBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_1, AUDIO_FORMAT);
        //创建AudioRecord
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG_1, AUDIO_FORMAT, recordBufferSize);
    }

    /**
     * 检查权限
     * @return true为权限已经全部被授权，false为权限没有全部被授权
     */
    private boolean checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
                return false;
            }
            return true;
        }
        return false;
    }
}
