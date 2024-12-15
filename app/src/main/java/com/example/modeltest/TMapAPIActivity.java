package com.example.modeltest;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.widget.FrameLayout;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

public class TMapAPIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmapapi);

        TMapView tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("5TDD2nla4j5sImMMtvZM943Z5EVqaBu45elIp09A");

        FrameLayout tmapContainer = findViewById(R.id.tmap_container);
        tmapContainer.addView(tMapView);

        // TmapView 초기화 후 경로 탐색 및 마커 설정
        new Handler().postDelayed(() -> {
            TMapPoint startPoint = new TMapPoint(37.564991, 126.983937);
            TMapPoint endPoint = new TMapPoint(37.566158, 126.988940);

            TMapMarkerItem startMarker = new TMapMarkerItem();
            startMarker.setTMapPoint(startPoint);
            startMarker.setName("출발지");
            tMapView.addMarkerItem("start", startMarker);

            TMapMarkerItem endMarker = new TMapMarkerItem();
            endMarker.setTMapPoint(endPoint);
            endMarker.setName("도착지");
            tMapView.addMarkerItem("end", endMarker);

            TMapData tMapData = new TMapData();
            tMapData.findPathData(startPoint, endPoint, polyLine -> {
                tMapView.addTMapPath(polyLine);
            });
        }, 500); // 500ms 지연

    }
}