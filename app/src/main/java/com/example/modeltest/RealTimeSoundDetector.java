package com.example.modeltest;

import android.widget.TextView;
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
    private TextView resultTextView; // Reference to update the UI with results

    public RealTimeSoundDetector(Context context, String modelFileName, TextView resultTextView) throws IOException {
        modelHandler = new TFLiteModelHandler(context, modelFileName);
        audioRecorder = new AudioRecorder();
        this.resultTextView = resultTextView; // Initialize the TextView
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
                String resultMessage = (prediction > 0.5) ? "Car detected!" : "No car detected.";
                updateResult(resultMessage);

                // Continue the loop
                handler.postDelayed(this, 1000); // 1000ms delay
            }
        });
    }

    public void stopDetection() {
        isDetecting = false;
        audioRecorder.stopRecording();
        modelHandler.close();
    }

    private void updateResult(String message) {
        // Update the TextView on the main thread
        resultTextView.post(() -> resultTextView.setText("Detection Result: " + message));
    }

    private float[][][][] extractMFCC(short[] audioData) {
        int sampleRate = 16000;
        int bufferSize = audioData.length;

        float[] floatAudioData = new float[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            floatAudioData[i] = audioData[i] / 32768.0f;
        }

        MFCC mfccProcessor = new MFCC(40, sampleRate, bufferSize);
        mfccProcessor.process(floatAudioData);

        float[] mfcc = mfccProcessor.getMFCC();
        int mfccSize = mfcc.length;

        float[][][][] mfccFeatures = new float[1][40][44][1];
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < Math.min(44, mfccSize); j++) {
                mfccFeatures[0][i][j][0] = mfcc[j];
            }
        }
        return mfccFeatures;
    }
}