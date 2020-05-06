package com.wl.audioplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wl.function.AudioTrackControl;

import java.io.File;

public class AudioPlayActivity extends AppCompatActivity {

    private final String TAG = "AudioPlayActivity";
    private final static String PATH = Environment.getExternalStorageDirectory() + File.separator + "filefilm" + File.separator + "audio_capture.pcm";
    private AudioTrackControl audioTrackControl;

    private Button mAudioPlayStart;
    private Button mAudioPlayStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_audio_play);

        audioTrackControl = new AudioTrackControl(PATH);

        mAudioPlayStart = findViewById(R.id.audio_play_start);
        mAudioPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioTrackControl.startAudioTrack();
            }
        });

        mAudioPlayStop = findViewById(R.id.audio_play_stop);
        mAudioPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioTrackControl.stopAudioTrack();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        audioTrackControl.stopAudioTrack();
    }
}
