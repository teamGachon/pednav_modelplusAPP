package com.example.modeltest;



import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorder {
    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    );

    private AudioRecord audioRecord;

    @SuppressLint("MissingPermission")
    public AudioRecorder() {
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE
        );
    }

    public void startRecording() {
        audioRecord.startRecording();
    }

    public void stopRecording() {
        audioRecord.stop();
        audioRecord.release();
    }

    public short[] readAudioData() {
        short[] audioBuffer = new short[BUFFER_SIZE];
        audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
        return audioBuffer;
    }
}