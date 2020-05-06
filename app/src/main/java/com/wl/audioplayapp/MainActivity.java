package com.wl.audioplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mAudioCapture;
    private Button mAudioPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioCapture = findViewById(R.id.audio_capture);
        mAudioCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AudioCaptureActivity.class);
                startActivity(intent);
            }
        });

        mAudioPlay = findViewById(R.id.audio_play);
        mAudioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AudioPlayActivity.class);
                startActivity(intent);
            }
        });
    }
}
