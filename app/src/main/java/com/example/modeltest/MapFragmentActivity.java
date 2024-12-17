package com.example.modeltest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private Handler blinkHandler = new Handler();
    private boolean isBlinking = false;
    private boolean isVehicleDetected = false; // 차량 탐지 여부

    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private FusedLocationProviderClient fusedLocationClient;

    private View overlayView;
    private FrameLayout rootLayout;

    // Sensor 관련 멤버 변수 선언
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float currentHeading = 0f;

    // 출발지 목적지 기능
    private EditText startPoint, endPoint;
    private Button findRouteButton;

    // 경고창 관련 변수
    private LinearLayout vehicleWarningLayout;

    // 마커 객체
    private Marker startMarker = new Marker(); // 출발지 마커
    private Marker endMarker = new Marker();   // 목적지 마커

    private static final String NAVER_CLIENT_ID = "dexbyijg2d";
    private static final String NAVER_CLIENT_SECRET = "oWbby1gXrrhtYftuyonY71axZ3K8NrgsbwdLVu2m";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        startPoint = findViewById(R.id.start_point);
        endPoint = findViewById(R.id.end_point);
        findRouteButton = findViewById(R.id.find_route_button);

        // 센서 매니저 초기화
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mapView.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findRouteButton.setOnClickListener(view -> findRoute());

        // 뷰 초기화
        rootLayout = findViewById(R.id.root_layout);
        if (rootLayout == null) {
            Log.e("MapFragmentActivity", "rootLayout is null. Check the XML layout ID.");
        }

        overlayView = findViewById(R.id.overlay_view);
        if (overlayView == null) {
            Log.e("MapFragmentActivity", "overlayView is null. Check the XML ID.");
        }


        // 경고창 초기화
        vehicleWarningLayout = findViewById(R.id.vehicle_warning_layout);

        // 차량 감지 테스트 버튼 설정
        Button testVehicleDetectionButton = findViewById(R.id.test_vehicle_detection);
        testVehicleDetectionButton.setOnClickListener(view -> {
            // 차량 탐지 여부 토글
            isVehicleDetected = !isVehicleDetected;

            if (isVehicleDetected) {
                startBlinkingOverlay(); // 깜빡이기 시작
                showVehicleWarning(); // 경고창 표시
                Toast.makeText(MapFragmentActivity.this, "차량이 감지되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                stopBlinkingOverlay(); // 깜빡이기 중지
                hideVehicleWarning(); // 경고창 숨김
                Toast.makeText(MapFragmentActivity.this, "차량이 감지되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);

        // 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }

        // 센서 리스너 등록
        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI);

        Marker marker = new Marker();
        marker.setPosition(new LatLng(37.5670135, 126.9783740));
        marker.setMap(naverMap);
    }

    // 깜빡이기 기능을 시작하거나 중지하는 메서드
    private void startBlinkingOverlay() {
        if (isBlinking) return; // 이미 깜빡이는 중이면 실행하지 않음
        isBlinking = true;

        blinkHandler.post(new Runnable() {
            boolean visible = true; // 뷰 상태를 토글하기 위한 플래그

            @Override
            public void run() {
                if (!isVehicleDetected) { // 차량 감지가 해제되면 중지
                    overlayView.setBackgroundColor(Color.TRANSPARENT);
                    isBlinking = false;
                    return;
                }

                if (visible) {
                    // 불투명한 빨간색 배경 (투명도 50%)
                    overlayView.setBackgroundColor(Color.parseColor("#88FFB3B3"));
                } else {
                    // 완전 투명 상태
                    overlayView.setBackgroundColor(Color.TRANSPARENT);
                }

                visible = !visible; // 상태를 토글
                blinkHandler.postDelayed(this, 500); // 0.5초 후 다시 실행
            }
        });
    }

    private void stopBlinkingOverlay() {
        isBlinking = false;
        blinkHandler.removeCallbacksAndMessages(null); // 핸들러 중지
        overlayView.setBackgroundColor(Color.TRANSPARENT); // 배경 초기화
    }

    // 경고창 표시
    private void showVehicleWarning() {
        vehicleWarningLayout.setVisibility(View.VISIBLE);
        overlayView.setBackgroundColor(Color.parseColor("#88FFB3B3")); // 반투명 배경
    }

    // 경고창 숨김
    private void hideVehicleWarning() {
        vehicleWarningLayout.setVisibility(View.GONE);
        overlayView.setBackgroundColor(Color.TRANSPARENT); // 투명 상태
    }

    // 센서 이벤트 리스너
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float azimuth = event.values[0]; // 북쪽을 기준으로 한 각도 (Heading)
            currentHeading = azimuth;

            // 바라보는 방향을 지도에 반영
            if (naverMap != null) {
                naverMap.getLocationOverlay().setBearing(currentHeading);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 정확도 변화에 대한 처리는 생략 가능
        }
    };

    private void checkVehicleDetection() {
        if (isVehicleDetected) {
            // 연한 빨간색으로 배경 변경 및 가시화
            overlayView.setBackgroundColor(Color.parseColor("#FFB3B3")); // 연한 빨간색
            overlayView.setVisibility(View.VISIBLE); // 가시화
        } else {
            // 투명 상태로 변경 및 숨김 처리
            overlayView.setBackgroundColor(Color.TRANSPARENT);
            overlayView.setVisibility(View.INVISIBLE); // 숨김 처리
        }
    }



    private void enableMyLocation() {
        naverMap.setLocationTrackingMode(LocationTrackingMode.Face);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // 현재 위치로 지도 이동
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new com.naver.maps.geometry.LatLng(location.getLatitude(), location.getLongitude()));
                    naverMap.moveCamera(cameraUpdate);

                    // 현재 위치 아이콘 설정
                    LocationOverlay locationOverlay = naverMap.getLocationOverlay();
                    locationOverlay.setVisible(true);
                    locationOverlay.setPosition(new com.naver.maps.geometry.LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        }

    private void findRoute() {
        String startAddress = startPoint.getText().toString();
        String endAddress = endPoint.getText().toString();

        if (startAddress.isEmpty() || endAddress.isEmpty()) {
            Toast.makeText(this, "출발지와 목적지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        getCoordinates(startAddress, true, startLatLng -> {
            getCoordinates(endAddress, false, endLatLng -> {
                requestDirection(startLatLng, endLatLng);
            });
        });
    }


    private void getCoordinates(String address, boolean isStartPoint, OnGeocodeListener listener) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + encodedAddress;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                    .addHeader("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MapFragmentActivity.this, "좌표 변환 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray addresses = json.getJSONArray("addresses");
                        if (addresses.length() > 0) {
                            JSONObject location = addresses.getJSONObject(0);
                            double lat = location.getDouble("y"); // 위도
                            double lng = location.getDouble("x"); // 경도

                            String message = "주소: " + address + "\n위도: " + lat + ", 경도: " + lng;
                            runOnUiThread(() -> {
                                Toast.makeText(MapFragmentActivity.this, message, Toast.LENGTH_LONG).show();
                                Log.d("Geocode Result", message);

                                // 마커 표시
                                LatLng position = new LatLng(lat, lng);
                                if (isStartPoint) {
                                    setMarker(startMarker, position, "출발지");
                                } else {
                                    setMarker(endMarker, position, "목적지");
                                }
                            });

                            listener.onSuccess(new LatLng(lat, lng));
                        } else {
                            runOnUiThread(() -> Toast.makeText(MapFragmentActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setMarker(Marker marker, LatLng position, String caption) {
        marker.setPosition(position);       // 마커 위치 설정
        marker.setCaptionText(caption);     // 마커 설명
        marker.setMap(naverMap);            // 마커를 지도에 추가
        naverMap.moveCamera(CameraUpdate.scrollTo(position)); // 카메라 이동
    }



    private void requestDirection(LatLng start, LatLng end) {
        String url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?"
                + "start=" + start.longitude + "," + start.latitude
                + "&goal=" + end.longitude + "," + end.latitude
                + "&option=traoptimal"; // 보행자 경로를 위한 옵션 설정

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MapFragmentActivity.this, "경로 요청 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray path = json.getJSONObject("route")
                            .getJSONArray("traoptimal") // 보행자 경로를 위한 키
                            .getJSONObject(0)
                            .getJSONArray("path");

                    ArrayList<LatLng> latLngList = new ArrayList<>();
                    for (int i = 0; i < path.length(); i++) {
                        JSONArray coords = path.getJSONArray(i);
                        latLngList.add(new LatLng(coords.getDouble(1), coords.getDouble(0))); // 위도, 경도 추가
                    }

                    runOnUiThread(() -> drawPath(latLngList));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MapFragmentActivity.this, "경로 처리 중 오류 발생", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void drawPath(ArrayList<LatLng> latLngList) {
        PathOverlay path = new PathOverlay();
        path.setCoords(latLngList); // 경로 좌표 설정
        path.setColor(Color.BLUE);  // 경로 색상 설정
        path.setMap(naverMap);      // 지도에 표시

        // 경로 시작 지점으로 카메라 이동
        if (!latLngList.isEmpty()) {
            naverMap.moveCamera(CameraUpdate.scrollTo(latLngList.get(0)));
        }
    }

    interface OnGeocodeListener {
        void onSuccess(LatLng latLng);
    }
}