package com.wl.function;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecordControl {

    private final String TAG = "AudioRecordControl";
    private AudioRecord audioRecord;
    private int audioSource;
    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;
    private int bufferSize;
    private int audioState;
    private boolean isRecording = false;
    private String pcmFile = null;
    private RecordRunnable recordRunnable;
    public AudioRecordControl(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSize = AudioRecord.getMinBufferSize(this.sampleRateInHz, this.channelConfig, this.audioFormat);
        Log.d(TAG, "AudioRecordControl audioSource: " + this.audioSource + " sampleRateInHz: " + this.sampleRateInHz);
        Log.d(TAG, "AudioRecordControl channelConfig: "+ this.channelConfig + " audioFormat: " + this.audioFormat + " bufferSize: " + this.bufferSize);
        audioRecord = new AudioRecord(this.audioSource, this.sampleRateInHz, this.channelConfig, this.audioFormat, this.bufferSize);
        audioState = audioRecord.getState();
        if(AudioRecord.STATE_INITIALIZED == audioState) {
            Log.d(TAG, "AudioRecordControl: init success");
        } else {
            Log.d(TAG, "AudioRecordControl: init fail, state: " + audioState);
        }
    }

    public void startRecord() {
        if(null == pcmFile) {
            Log.d(TAG, "startRecord: pcmFile == null, return");
            return;
        }
        if(AudioRecord.STATE_INITIALIZED == audioState) {
            Log.d(TAG, "startRecord success");
            if(null != audioRecord) {
                recordRunnable = new RecordRunnable(bufferSize, pcmFile);
                audioRecord.startRecording();
                audioState = audioRecord.getState();
                isRecording = true;
                new Thread(recordRunnable).start();
            } else {
                Log.d(TAG, "startRecord: audioRecord == null, return");
            }
        } else {
            Log.d(TAG, "startRecord: start fail, state: " + audioState);
        }
    }

    public void stopRecord() {
        Log.d(TAG, "stopRecord isRecording: " + isRecording);
        if(isRecording) {
            isRecording = false;
            if(null != audioRecord) {
                audioRecord.stop();
                audioRecord.release();
                audioState = audioRecord.getState();
                audioRecord = null;
            }
        }
    }

    public String getPcmFile() {
        return this.pcmFile;
    }

    public void setPcmFile(String pcmFile) {
        this.pcmFile = pcmFile;
    }

    public class RecordRunnable implements Runnable {

        private int buffreSize;
        private String pcmFile;
        public RecordRunnable(int bufferSize, String pcmFile) {
            this.buffreSize = bufferSize;
            this.pcmFile = pcmFile;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: RecordRunnable, record .....");
            byte[] data = new byte[bufferSize];
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(pcmFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (out == null) {
                return;
            }

            try {
                while (isRecording) {
                    int len = audioRecord.read(data, 0, bufferSize);
                    out.write(data, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pcmToWavFile(String inFileName, String outFileName) {
        Log.d(TAG, "pcmToWavFile outFileName: " + outFileName);
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        int longSampleRate = this.sampleRateInHz;
        long totalDataLen = totalAudioLen + 36;

        int channels = (AudioFormat.CHANNEL_IN_STEREO == this.channelConfig) ? 2 : 1;//你录制是单声道就是1 双声道就是2
        long byteRate = longSampleRate * channels * this.audioFormat;

        byte[] data = new byte[this.bufferSize];
        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);

            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "pcmToWavFile: complete");
    }

     /*
      *任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
      *FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的，
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
