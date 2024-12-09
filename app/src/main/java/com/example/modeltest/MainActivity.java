package com.example.modeltest;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // 모델 로드
            TFLiteModelLoader modelLoader = new TFLiteModelLoader(this, "car_detection_model.tflite");
            tflite = modelLoader.getInterpreter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TFLite 모델 사용 예시
        float[][][] input = new float[1][40][44]; // 예제 입력 (MFCC)
        float[][] output = new float[1][1];    // 출력 공간

        // 모델 실행
        if (tflite != null) {
            tflite.run(input, output);

            // 결과 처리
            boolean isCarDetected = output[0][0] > 0.5; // 0.5를 기준으로 차량 소리 감지 여부 판단
            if (isCarDetected) {
                System.out.println("Car sound detected!");  // 이 부분을 UI로 변경해서 하면 될 것 같음!
            }
        }
    }
}