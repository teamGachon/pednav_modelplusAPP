package com.example.modeltest;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class ModelTestActivity extends AppCompatActivity {

    private RealTimeSoundDetector soundDetector;
    private boolean isDetectionRunning = false; // Track the detection status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_test);

        TextView btnStartDetection = findViewById(R.id.btnStartDetection);
        TextView btnStopDetection = findViewById(R.id.btnStopDetection);

        try {
            // Initialize the detector with the TFLite model path
            soundDetector = new RealTimeSoundDetector(this, "car_detection_model.tflite");
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to load model", e);
            Toast.makeText(this, "Failed to initialize sound detector", Toast.LENGTH_LONG).show();
            return;
        }

        // Set up the "ON" button to start detection
        btnStartDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDetectionRunning && soundDetector != null) {
                    soundDetector.startDetection();
                    isDetectionRunning = true;
                    Toast.makeText(ModelTestActivity.this, "Sound detection started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ModelTestActivity.this, "Detection is already running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the "OFF" button to stop detection
        btnStopDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDetectionRunning && soundDetector != null) {
                    soundDetector.stopDetection();
                    isDetectionRunning = false;
                    Toast.makeText(ModelTestActivity.this, "Sound detection stopped", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ModelTestActivity.this, "Detection is not running", Toast.LENGTH_SHORT).show();
                }
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