package com.example.modeltest;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketTestActivity extends AppCompatActivity {

    private WebSocket webSocket = null;
    private AudioRecord audioRecord = null;
    private boolean isRecording = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final String TAG = "WebSocketTestActivity";
    private static final int SAMPLE_RATE = 44100;
    private static final int REQUEST_CODE_RECORD_AUDIO = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_test);

        // 권한 확인 및 요청
        if (hasRecordAudioPermission()) {
            connectWebSocket("ws://172.25.80.212:8080/audio/send");
            startAudioLogging();
        } else {
            requestRecordAudioPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioLogging();
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }

    /**
     * RECORD_AUDIO 권한 확인
     */
    private boolean hasRecordAudioPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * RECORD_AUDIO 권한 요청
     */
    private void requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE_RECORD_AUDIO
        );
    }

    /**
     * WebSocket 연결 설정
     */
    private void connectWebSocket(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "웹소켓 연결 성공");
            }


            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "서버로부터 메시지 수신: " + text);

                try {
                    JSONObject json = new JSONObject(text);
                    boolean vehicleDetected = json.optBoolean("vehicleDetected", false); // 기본값 false
                    double result = json.optDouble("result", 0.0); // 기본값 0.0

                    Log.d(TAG, "차량 감지 여부: " + vehicleDetected);
                    Log.d(TAG, "모델 결과값 (0~1): " + result);

                    runOnUiThread(() -> {
                        TextView vehicleDetectedView = findViewById(R.id.vehicleDetectedView);
                        TextView resultView = findViewById(R.id.resultView);
                        vehicleDetectedView.setText("차량 감지 여부: " + (vehicleDetected ? "감지됨" : "미감지"));
                        resultView.setText("모델 결과값: " + result);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "JSON 파싱 중 오류 발생: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "웹소켓 오류: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "웹소켓 연결 종료: " + reason);
            }
        });
    }

    private void startAudioLogging() {
        // 권한 확인
        if (!hasRecordAudioPermission()) {
            Log.e(TAG, "오디오 녹음 권한이 부여되지 않았습니다.");
            return;
        }

        try {
            initAudioRecorder(); // AudioRecord 초기화
            if (audioRecord != null) {
                audioRecord.startRecording(); // 녹음 시작
                isRecording = true;

                executorService.execute(() -> {
                    byte[] buffer = new byte[1024]; // 읽기 버퍼
                    while (isRecording) {
                        try {
                            // 5초 대기
                            Thread.sleep(5000);

                            int readBytes = audioRecord.read(buffer, 0, buffer.length);
                            if (readBytes > 0) {
                                // 읽은 데이터를 로그로 출력
                                //Log.d(TAG, "오디오 데이터 (Hex): " + bytesToHex(buffer, readBytes));

                                // 웹소켓으로 데이터 전송
                                if (webSocket != null) {
                                    // 바이너리 메시지로 데이터 전송
                                    webSocket.send(okio.ByteString.of(buffer, 0, readBytes));
                                    Log.d(TAG, "웹소켓으로 소리 데이터 전송 완료: " + readBytes + " 바이트");
                                } else {
                                    Log.e(TAG, "웹소켓이 연결되지 않았습니다.");
                                }
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "오디오 로깅 중단됨: " + e.getMessage());
                            Thread.currentThread().interrupt(); // 현재 스레드 상태 복원
                            stopAudioLogging(); // 녹음 중지
                            break;
                        } catch (SecurityException e) {
                            Log.e(TAG, "오디오 녹음 권한 부족: " + e.getMessage());
                            stopAudioLogging(); // 녹음 중지
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "오디오 로깅 중 예상치 못한 오류: " + e.getMessage());
                            stopAudioLogging(); // 녹음 중지
                            break;
                        }
                    }
                });
                Log.d(TAG, "오디오 로깅 시작됨.");
            }
        } catch (Exception e) {
            Log.e(TAG, "오디오 로깅 시작 실패: " + e.getMessage());
        }
    }


    /**
     * 오디오 캡처를 중지
     */
    private void stopAudioLogging() {
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "오디오 녹음 중지 중 오류 발생: " + e.getMessage());
            } finally {
                audioRecord = null;
            }
        }
        Log.d(TAG, "오디오 로깅 중지됨.");
    }

    /**
     * AudioRecord 초기화
     */
    private void initAudioRecorder() {
        try {
            if (!hasRecordAudioPermission()) {
                Log.e(TAG, "오디오 녹음 권한이 부여되지 않아 초기화 불가.");
                return;
            }

            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            Log.d(TAG, "AudioRecord가 성공적으로 초기화되었습니다.");
        } catch (SecurityException e) {
            Log.e(TAG, "오디오 초기화 중 권한 오류: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "AudioRecord 초기화 실패: " + e.getMessage());
        }
    }

    /**
     * 권한 요청 결과 처리
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO 권한 승인됨.");
                connectWebSocket("ws://172.25.80.212:8080/audio/send");
                startAudioLogging();
            } else {
                Log.e(TAG, "RECORD_AUDIO 권한 거부됨.");
            }
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
            hexString.append(' ');
        }
        return hexString.toString();
    }

}
