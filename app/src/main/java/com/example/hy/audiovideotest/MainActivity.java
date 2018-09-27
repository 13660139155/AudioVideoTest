package com.example.hy.audiovideotest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.hy.audiovideotest.audio.AudioActivity;
import com.example.hy.audiovideotest.media.MediaActivity;
import com.example.hy.audiovideotest.openGL.OpenGLES20Activity;

public class MainActivity extends AppCompatActivity {

    Button btnAudio, btnMedia, btnOpenGL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAudio = findViewById(R.id.btn_audio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioActivity.class);
                startActivity(intent);
            }
        });

        btnMedia = findViewById(R.id.btn_media);
        btnMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                startActivity(intent);
            }
        });

        btnOpenGL = findViewById(R.id.btn_openGL);
        btnOpenGL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OpenGLES20Activity.class);
                startActivity(intent);
            }
        });
    }
}
