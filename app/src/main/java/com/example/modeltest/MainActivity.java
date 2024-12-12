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

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 16000; // 16 kHz
    private static final String MODEL_PATH = "car_sound_model.tflite";

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Interpreter tflite;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigate to ModelTestActivity
        TextView btnTestPage = findViewById(R.id.btnTestPage);
        btnTestPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ModelTestActivity.class);
                startActivity(intent);
            }
        });
//        tvStatus = findViewById(R.id.tvStatus);
//        Button btnStart = findViewById(R.id.btnStart);
//        Button btnStop = findViewById(R.id.btnStop);
//
//        // 권한 요청
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
//        }
//
//        // TensorFlow Lite 모델 로드
//        try {
//            tflite = new Interpreter(loadModelFile());
//        } catch (IOException e) {
//            Log.e("TFLite", "Error loading model", e);
//        }
//
//        // 시작 버튼 클릭 이벤트
//        btnStart.setOnClickListener(v -> startRecording());
//
//        // 중지 버튼 클릭 이벤트
//        btnStop.setOnClickListener(v -> stopRecording());
//    }
//
//    private void startRecording() {
//        if (isRecording) return;
//
//        tvStatus.setText("분석 중...");
//        isRecording = true;
//
//        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
//                android.media.AudioFormat.CHANNEL_IN_MONO,
//                android.media.AudioFormat.ENCODING_PCM_16BIT);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                SAMPLE_RATE,
//                android.media.AudioFormat.CHANNEL_IN_MONO,
//                android.media.AudioFormat.ENCODING_PCM_16BIT,
//                bufferSize);
//
//        audioRecord.startRecording();
//
//        // 새로운 스레드에서 녹음 데이터 처리
//        new Thread(() -> {
//            short[] buffer = new short[bufferSize / 2];
//            while (isRecording) {
//                int read = audioRecord.read(buffer, 0, buffer.length);
//                if (read > 0) {
//                    analyzeSound(buffer);
//                }
//            }
//        }).start();
//    }
//
//    private void stopRecording() {
//        if (!isRecording) return;
//
//        isRecording = false;
//        audioRecord.stop();
//        audioRecord.release();
//        audioRecord = null;
//        tvStatus.setText("분석이 중지되었습니다.");
//    }
//
//    private void analyzeSound(short[] audioData) {
//        // 입력 데이터를 float로 변환
//        float[] input = new float[audioData.length];
//        for (int i = 0; i < audioData.length; i++) {
//            input[i] = audioData[i] / 32768.0f; // Normalize
//        }
//
//        // TFLite 실행
//        float[][] output = new float[1][1]; // 예: [1, 1] 크기 출력
//        tflite.run(input, output);
//
//        // 결과 해석
//        runOnUiThread(() -> {
//            if (output[0][0] > 0.5) {
//                tvStatus.setText("차량 소리가 감지되었습니다.");
//            } else {
//                tvStatus.setText("차량 소리가 감지되지 않았습니다.");
//            }
//        });
//    }
//
//    private MappedByteBuffer loadModelFile() throws IOException {
//        try (FileInputStream fis = new FileInputStream(getAssets().openFd(MODEL_PATH).getFileDescriptor());
//             FileChannel fileChannel = fis.getChannel()) {
//            long startOffset = fis.getChannel().position();
//            long declaredLength = fis.getChannel().size();
//            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_AUDIO_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                tvStatus.setText("오디오 권한이 필요합니다.");
//            }
//        }
//    }
    }
}
