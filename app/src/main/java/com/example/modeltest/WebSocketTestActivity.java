package com.example.modeltest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
    private static final String CHANNEL_ID = "WebSocketTestChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_test);

        // Notification 권한 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1002);
            }
        }

        // Foreground Service 시작
        startForegroundService();

        // 권한 확인 및 요청
        if (hasRecordAudioPermission()) {
            connectWebSocket("ws://192.9.203.19:8080/audio/send");
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
     * Foreground Service 시작
     */
    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "WebSocket Audio Logging",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS 권한이 필요합니다.");
                return;
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Audio Logging")
                .setContentText("오디오 데이터를 서버로 전송 중입니다.")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();

        // Foreground Service 실행
        Intent serviceIntent = new Intent(this, WebSocketTestActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }

        NotificationManagerCompat.from(this).notify(1, notification);
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

                    long endTime = System.nanoTime(); // T2: 결과 수신 및 화면 출력 시점 기록
                    long latency = (endTime - startTime[0]) / 1_000_000; // 밀리초 단위로 변환

                    Log.d(TAG, "차량 감지 여부: " + vehicleDetected);
                    Log.d(TAG, "모델 결과값: " + result);
                    Log.d(TAG, "전체 레이턴시: " + latency + " ms");

                    // 차량 감지 시 진동 추가
                    if (vehicleDetected) {
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)); // 500ms 진동
                            } else {
                                vibrator.vibrate(500); // Deprecated지만 하위 버전 지원
                            }
                            Log.d(TAG, "진동 발생: 사고 감지됨");
                        }
                    }

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

    long[] startTime = new long[1]; // 시작 시간 저장

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
                                if (webSocket != null) {
                                    startTime[0] = System.nanoTime(); // T1: 소리 수집 시작 시간 기록
                                    webSocket.send(okio.ByteString.of(buffer, 0, readBytes));
                                    Log.d(TAG, "-------------웹소켓으로 소리 데이터 전송 완료: " + readBytes + " 바이트-------------");
                                } else {
                                    Log.e(TAG, "웹소켓이 연결되지 않았습니다.");
                                }
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "오디오 로깅 중단됨: " + e.getMessage());
                            Thread.currentThread().interrupt();
                            stopAudioLogging();
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

    private void initAudioRecorder() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "오디오 녹음 권한이 부여되지 않아 초기화 불가.");
                return;
            }

            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioRecord 초기화 실패");
            }
            Log.d(TAG, "AudioRecord가 성공적으로 초기화되었습니다.");
        } catch (SecurityException e) {
            Log.e(TAG, "오디오 녹음 권한 오류: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e(TAG, "AudioRecord 초기화 실패: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "AudioRecord 초기화 중 알 수 없는 오류: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO 권한 승인됨.");
                connectWebSocket("ws://192.9.203.19:8080/audio/send");
                startAudioLogging();
            } else {
                Log.e(TAG, "RECORD_AUDIO 권한 거부됨.");
                finish(); // 권한이 없으면 앱 종료 또는 사용자에게 안내
            }
        } else if (requestCode == 1002) { // POST_NOTIFICATIONS 처리
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS 권한 승인됨.");
                startForegroundService();
            } else {
                Log.e(TAG, "POST_NOTIFICATIONS 권한 거부됨.");
            }
        }
    }


}
