package com.wl.audioplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wl.function.AudioRecordControl;
import com.wl.function.PermissionUtils;

import java.io.File;

public class AudioCaptureActivity extends AppCompatActivity {

    private final String TAG = "AudioCaptureActivity";
    private final static String PATH = Environment.getExternalStorageDirectory() + File.separator + "filefilm" + File.separator + "audio_capture.pcm";
    private final static String WAV = Environment.getExternalStorageDirectory() + File.separator + "filefilm" + File.separator + "audio_capture.wav";
    private AudioRecordControl audioRecordControl;

    private Button mAudioRecordStart;
    private Button mAudioRecordStop;
    private Button mAudioFormatConvert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_audio_capture);
        PermissionUtils.askPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 100, null);
        audioRecordControl = new AudioRecordControl(MediaRecorder.AudioSource.MIC, 44100,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecordControl.setPcmFile(PATH);
        mAudioRecordStart = findViewById(R.id.audio_record_start);
        mAudioRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordControl.startRecord();
            }
        });

        mAudioRecordStop = findViewById(R.id.audio_record_stop);
        mAudioRecordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordControl.stopRecord();
            }
        });

        mAudioFormatConvert = findViewById(R.id.audio_format_convert);
        mAudioFormatConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordControl.pcmToWavFile(PATH, WAV);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioRecordControl.stopRecord();
    }
}
