package com.example.modeltest;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


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
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ModelTestActivity extends AppCompatActivity {

    private RealTimeSoundDetector soundDetector;
    private boolean isDetectionRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_test);

        TextView tvResult = findViewById(R.id.tvResult); // For showing detection results
        TextView onButton = findViewById(R.id.btnStartDetection);
        TextView offButton = findViewById(R.id.btnStopDetection);

        try {
            // Initialize RealTimeSoundDetector with model and result TextView
            soundDetector = new RealTimeSoundDetector(this, "car_detection_model.tflite", tvResult);
        } catch (IOException e) {
            Log.e("ModelTestActivity", "Failed to load model", e);
            Toast.makeText(this, "Failed to initialize sound detector", Toast.LENGTH_LONG).show();
            return;
        }

        // Set up the "ON" button
        onButton.setOnClickListener(new View.OnClickListener() {
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

        // Set up the "OFF" button
        offButton.setOnClickListener(new View.OnClickListener() {
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
        if (soundDetector != null) {
            soundDetector.stopDetection();
        }
    }
}