package com.example.modeltest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketTestActivity extends AppCompatActivity {

    private WebSocket webSocket = null;
    private static final String TAG = "WebSocketTestActivity";
    private static final int REQUEST_CODE_RECORD_AUDIO = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_test);

        // 권한 확인 및 요청
        if (hasRecordAudioPermission()) {
            connectWebSocket("ws://172.25.87.5:8081/audio/send");
        } else {
            requestRecordAudioPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                Log.d(TAG, "WebSocket Connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received: " + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket Error: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket Closed: " + reason);
            }
        });
    }

    /**
     * 권한 요청 결과 처리
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO 권한 승인");
                connectWebSocket("ws://localhost:8081/audio/send");
            } else {
                Log.e(TAG, "RECORD_AUDIO 권한 실패");
            }
        }
    }
}
