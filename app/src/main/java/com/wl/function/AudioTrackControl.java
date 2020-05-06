package com.wl.function;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioTrackControl {

    private final String TAG = "AudioTrackControl";

    private AudioTrack audioTrack;
    private boolean isPlaying;
    private int bufferSize;
    private String pcm_file = null;
    private PlayStreamRunnable playStreamRunnable;

    private int OUT_SAMPLE_RATE = 44100;

    public AudioTrackControl(String pcm_file) {
        Log.d(TAG, "AudioRecordControl: ");
        this.bufferSize = AudioTrack.getMinBufferSize(OUT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,  //播放类型：音乐
                OUT_SAMPLE_RATE,  //采样率
                AudioFormat.CHANNEL_OUT_STEREO, //双声道
                AudioFormat.ENCODING_PCM_16BIT,  //采样位数
                this.bufferSize, //缓冲区大小
                AudioTrack.MODE_STREAM);  //播放模式：数据流动态写入
        Log.d(TAG, "AudioRecordControl bufferSize: " + this.bufferSize);
        this.pcm_file = pcm_file;
        isPlaying = false;
    }

    public void startAudioTrack() {
        Log.d(TAG, "startAudioTrack: ");
        if(null == pcm_file) {
            Log.d(TAG, "startAudioTrack: pcm_file == null");
            return;
        }
        if(null == audioTrack) {
            Log.d(TAG, "startAudioTrack: audioTrack == null, return");
            return;
        }
        if(!isPlaying) {
            playStreamRunnable = new PlayStreamRunnable(pcm_file, this.bufferSize);
            isPlaying = true;
            audioTrack.play();
            new Thread(playStreamRunnable).start();
        }
    }

    public void stopAudioTrack() {
        Log.d(TAG, "stopAudioTrack: ");
        if(isPlaying) {
            isPlaying = false;
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    class PlayStreamRunnable implements Runnable {
        private int bufferSize;
        private String file_name;

        public PlayStreamRunnable(String file_name, int bufferSize) {
            Log.d(TAG, "PlayStreamRunnable file_name: " + file_name);
            Log.d(TAG, "PlayStreamRunnable bufferSize: " + bufferSize);
            this.file_name = file_name;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: play stream is start");
            byte[] data = file2Bytes(this.file_name);
            Log.d(TAG, "run size: " + data.length);
            if(data == null || data.length == 0) {
                Log.d(TAG, "run: data == null, retrun");
                return;
            }
            int len = data.length;
            int currentPosition = 0;
            while (isPlaying) {
                if (len > bufferSize) {
                    // 循环播放
                    audioTrack.write(data, currentPosition, bufferSize);
                    currentPosition += bufferSize;
                    if (currentPosition + bufferSize >= len) {
                        audioTrack.write(data, currentPosition, len - currentPosition);
                        currentPosition = 0;
                    }
                } else {
                    audioTrack.write(data, 0, len);
                }
            }
        }
    }

    private byte[] file2Bytes(String file_name) {
        Log.d(TAG, "file2Bytes: ");
        File file = new File(file_name);
        if(!file.exists()) {
            Log.d(TAG, "file2Bytes: file is not exist");
            return null;
        }
        BufferedInputStream in = null;
        byte[] content = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            byte[] temp = new byte[1024];
            int size = 0;
            while((size = in.read(temp)) != -1){
                out.write(temp, 0, size);
            }
            in.close();
            content = new byte[out.toByteArray().length];
            content = out.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
