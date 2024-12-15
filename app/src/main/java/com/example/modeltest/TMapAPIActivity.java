package com.example.modeltest;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.skt.tmap.TMapView;

public class TMapAPIActivity extends AppCompatActivity {

    private WebView webView;
    private TextView resultText;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmapapi);


        LinearLayout linearLayoutTmap = (LinearLayout) findViewById(R.id.linearLayoutTmap);
        TMapView tmapview = new TMapView(this);

        tmapview.setSKTMapApiKey("5TDD2nla4j5sImMMtvZM943Z5EVqaBu45elIp09A");
        linearLayoutTmap.addView(tmapview);


    }
}
