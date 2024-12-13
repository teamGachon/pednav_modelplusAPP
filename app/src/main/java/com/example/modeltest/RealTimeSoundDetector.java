package com.example.modeltest;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class RealTimeSoundDetector {
    private TFLiteModelHandler modelHandler;
    private AudioRecorder audioRecorder;
    private Handler handler = new Handler();
    private boolean isDetecting = false;

    public RealTimeSoundDetector(Context context, String modelFileName) throws IOException {
        modelHandler = new TFLiteModelHandler(context, modelFileName);
        audioRecorder = new AudioRecorder();
    }

    public void startDetection() {
        audioRecorder.startRecording();
        isDetecting = true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isDetecting) return;

                // Get raw audio data
                short[] audioData = audioRecorder.readAudioData();

                // Convert to MFCC features
                float[][][][] mfccFeatures = extractMFCC(audioData);

                // Predict using TFLite model
                float prediction = modelHandler.predict(mfccFeatures);

                // Process prediction
                if (prediction > 0.5) {
                    Log.d("Detection", "Car detected!");
                } else {
                    Log.d("Detection", "No car detected.");
                }

                // Continue the loop
                handler.postDelayed(this, 500); // 500ms delay
            }
        });
    }

    public void stopDetection() {
        isDetecting = false;
        audioRecorder.stopRecording();
        modelHandler.close();
    }

    private float[][][][] extractMFCC(short[] audioData) {
        // Placeholder for MFCC extraction logic
        return new float[1][40][44][1];
    }
}