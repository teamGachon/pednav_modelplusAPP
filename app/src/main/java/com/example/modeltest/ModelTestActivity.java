package com.example.modeltest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelTestActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 16000; // 16 kHz
    private static final String MODEL_PATH = "app/src/main/assets/car_detection_model.tflite";

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Interpreter tflite;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_test);

        tvResult = findViewById(R.id.tvResult);
        Button btnStartDetection = findViewById(R.id.btnStartDetection);
        Button btnStopDetection = findViewById(R.id.btnStopDetection);

        // Check audio recording permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
        }

        // Load TensorFlow Lite model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            tvResult.setText("Error loading model");
            e.printStackTrace();
        }

        // Start detection
        btnStartDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetection();
            }
        });

        // Stop detection
        btnStopDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDetection();
            }
        });
    }

    private void startDetection() {
        if (isRecording) return;

        isRecording = true;
        tvResult.setText("Detection started...");

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        audioRecord.startRecording();

        new Thread(() -> {
            short[] buffer = new short[bufferSize / 2];
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    analyzeAudio(buffer);
                }
            }
        }).start();
    }

    private void stopDetection() {
        if (!isRecording) return;

        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        tvResult.setText("Detection stopped.");
    }

    private void analyzeAudio(short[] audioData) {
        // Convert audio data to float
        float[] input = new float[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            input[i] = audioData[i] / 32768.0f; // Normalize audio data
        }

        // Run inference using TensorFlow Lite
        float[][] output = new float[1][1]; // Adjust output shape based on the model
        tflite.run(input, output);

        // Update UI with detection result
        runOnUiThread(() -> {
            if (output[0][0] > 0.5) {
                tvResult.setText("Detection Result: Vehicle Detected!");
            } else {
                tvResult.setText("Detection Result: No Vehicle Detected.");
            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(getAssets().openFd(MODEL_PATH).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileInputStream.getChannel().position();
        long declaredLength = fileInputStream.getChannel().size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                tvResult.setText("Audio permission is required.");
            }
        }
    }
}
