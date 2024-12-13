package com.example.modeltest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private RealTimeSoundDetector soundDetector;
    private boolean isDetectionRunning = false; // Track the detection status


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button onButton = findViewById(R.id.btnStartDetection); // "ON" button
        Button offButton = findViewById(R.id.btnStopDetection); // "OFF" button


        try {
            // Initialize the detector with the TFLite model path
            soundDetector = new RealTimeSoundDetector(this, "car_detection_model.tflite");
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to load model", e);
            Toast.makeText(this, "Failed to initialize sound detector", Toast.LENGTH_LONG).show();
            return;
        }

        // Set up the "ON" button to start detection
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDetectionRunning && soundDetector != null) {
                    soundDetector.startDetection();
                    isDetectionRunning = true;
                    Toast.makeText(MainActivity.this, "Sound detection started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Detection is already running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the "OFF" button to stop detection
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDetectionRunning && soundDetector != null) {
                    soundDetector.stopDetection();
                    isDetectionRunning = false;
                    Toast.makeText(MainActivity.this, "Sound detection stopped", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Detection is not running", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Navigate to ModelTestActivity
        TextView btnTestPage = findViewById(R.id.btnTestPage);
        btnTestPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ModelTestActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure the sound detector is stopped and cleaned up
        if (soundDetector != null) {
            soundDetector.stopDetection();
        }
    }
}
